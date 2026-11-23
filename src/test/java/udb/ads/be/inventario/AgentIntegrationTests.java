package udb.ads.be.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.inventario.agent.Planner;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"inventory.profile-keys.auditor=test-a","inventory.profile-keys.operativo=test-o"})
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class AgentIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean Planner planner;
    static final String BASE = "/api/agents/";
    static final String OP = "e8541ece-9e5e-40bb-89e1-ac82644121a4";

    ResultActions postJson(String path, String key, Object body) throws Exception {
        return mvc.perform(post(BASE + path).header("X-Agent-Key",key).contentType("application/json")
                .content(mapper.writeValueAsBytes(body)));
    }

    Map<String,Object> salida(int cantidad) {
        return Map.of("sku","CAB-01","sucursal",1,"tipo","SALIDA","operacion",OP,"cantidad",cantidad);
    }

    @Test void catalogAndAccess() throws Exception {
        mvc.perform(get(BASE+"auditor/skills").header("X-Agent-Key","test-a"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        postJson("operativo/skills/inventory_movement","test-a",salida(3)).andExpect(status().isForbidden());
        postJson("auditor/skills/inventory_movement","test-a",salida(3)).andExpect(status().isForbidden());
        postJson("auditor/skills/stock_query","test-a",Map.of("cantidad",-1)).andExpect(status().isBadRequest());
    }

    @Test void alertsAndCategory() throws Exception {
        postJson("auditor/skills/stock_query","test-a",Map.of("categoria","Oficina"))
                .andExpect(jsonPath("$.resultado.valorTotal").value(60.0));
        postJson("auditor/skills/low_stock","test-a",Map.of()).andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado[0].nivel").value("CRITICO"))
                .andExpect(jsonPath("$.resultado[0].rotacion").value("SIN_ROTACION"))
                .andExpect(jsonPath("$.resultado[1].nivel").value("BAJO"));
    }

    @Test void confirmationRetryAndRollback() throws Exception {
        var plan = new Planner.Plan(null,"inventory_movement",mapper.valueToTree(salida(3)));
        when(planner.plan(any(),anyString())).thenReturn(plan);
        var message = Map.of("mensaje","Solicito salida de tres cables");
        postJson("auditor/chat","test-a",message).andExpect(status().isForbidden());
        postJson("operativo/chat","test-o",message).andExpect(jsonPath("$.estado").value("pendiente_confirmacion"));
        String path = "operativo/skills/inventory_movement";
        postJson(path,"test-o",salida(13)).andExpect(status().isConflict());
        postJson(path,"test-o",salida(0)).andExpect(status().isBadRequest());
        for (int i = 0; i < 2; i++) {
            postJson(path,"test-o",salida(3)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultado.anterior").value(12)).andExpect(jsonPath("$.resultado.saldo").value(9));
        }
        postJson(path,"test-o",salida(2)).andExpect(status().isConflict());
        postJson("auditor/skills/stock_query","test-a",Map.of("sku","CAB-01"))
                .andExpect(jsonPath("$.resultado.unidades").value(9));
    }
}
