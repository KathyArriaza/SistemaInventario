package com.proyecto.inventario.agent;

import com.fasterxml.jackson.databind.*;
import com.proyecto.inventario.config.InventoryConfig.Settings;
import com.proyecto.inventario.inventory.InventoryError;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class HttpPlanner implements Planner {
    private final RestClient client;
    private final Settings settings;
    private final ObjectMapper mapper;

    public HttpPlanner(RestClient.Builder builder, Settings settings, ObjectMapper mapper) {
        this.settings = settings;
        this.mapper = mapper;
        var transport = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        transport.setReadTimeout(Duration.ofSeconds(30));
        this.client = builder.requestFactory(transport).build();
    }

    public Plan plan(AgentFactory.CustomAgent agent, String message) {
        if (settings.llmKey().isBlank()) throw new InventoryError(InventoryError.Kind.UNCONFIGURED, "Configure LLM_API_KEY");
        var t = agent.template();
        var tools = agent.skills().values().stream().map(s -> Map.of("type","function","function",s.descriptor())).toList();
        var messages = List.of(Map.of("role","system","content",String.join("\n",t.rol(),t.tono(),t.reglas())),
                Map.of("role","user","content",message));
        try {
            JsonNode response = client.post().uri(settings.llmUrl()).headers(h -> h.setBearerAuth(settings.llmKey()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model",settings.model(),"temperature",settings.temperature(),"messages",messages,"tools",tools))
                    .retrieve().body(JsonNode.class);
            var reply = Objects.requireNonNull(response).at("/choices/0/message");
            var calls = reply.path("tool_calls");
            if (calls.isMissingNode() || calls.isEmpty()) {
                if (!reply.path("content").isTextual() || reply.path("content").asText().isBlank())
                    throw new IllegalArgumentException();
                return new Plan(reply.get("content").asText(), null, null);
            }
            if (!calls.isArray() || calls.size() != 1) throw new IllegalArgumentException();
            var function = calls.get(0).path("function");
            var args = mapper.readTree(function.path("arguments").asText());
            if (args == null || !args.isObject() || !function.path("name").isTextual()) throw new IllegalArgumentException();
            return new Plan(null, function.get("name").asText(), args);
        } catch (RestClientException | java.io.IOException | IllegalArgumentException | NullPointerException e) {
            throw new InventoryError(InventoryError.Kind.PROVIDER, "Respuesta del proveedor LLM inválida o no disponible");
        }
    }
}
