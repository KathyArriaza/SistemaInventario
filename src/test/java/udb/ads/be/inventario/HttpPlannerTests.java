package udb.ads.be.inventario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.inventario.agent.*;
import com.proyecto.inventario.config.InventoryConfig.Settings;
import com.proyecto.inventario.inventory.InventoryPort;
import com.proyecto.inventario.skills.StockQuerySkill;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class HttpPlannerTests {
    @Test void sendsAuthorizedCatalogAndReadsFunctionCall() throws Exception {
        var mapper = new ObjectMapper();
        var captured = new AtomicReference<String>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/chat", exchange -> {
            try (exchange) {
                captured.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
                var reply = """
                        {"choices":[{"message":{"tool_calls":[{"type":"function","function":{"name":"stock_query","arguments":"{}"}}]}}]}
                        """.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type","application/json");
                exchange.sendResponseHeaders(200,reply.length);
                exchange.getResponseBody().write(reply);
            }
        });
        server.start();
        try {
            var url = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/chat");
            var settings = new Settings(null,2,5,90,"",Map.of(),url,"test","modelo-prueba",0);
            var planner = new HttpPlanner(RestClient.builder(),settings,mapper);
            var skill = new StockQuerySkill(mock(InventoryPort.class),mapper);
            var template = new AgentFactory.Template("auditor","Auditor","Preciso","Consulta",List.of("stock_query"));
            var plan = planner.plan(new AgentFactory.CustomAgent(template,Map.of("stock_query",skill)),"¿Qué hay?");
            assertEquals("stock_query",plan.skill());
            assertTrue(plan.input().isObject());
            var sent = mapper.readTree(captured.get());
            assertEquals(1,sent.get("tools").size());
            assertEquals("stock_query",sent.at("/tools/0/function/name").asText());
            assertEquals("¿Qué hay?",sent.at("/messages/1/content").asText());
        } finally {
            server.stop(0);
        }
    }
}
