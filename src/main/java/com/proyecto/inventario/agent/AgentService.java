package com.proyecto.inventario.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.inventario.inventory.InventoryError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentService {
    public record Reply(String estado, String skill, Object resultado) {}
    private final AgentFactory factory;
    private final Planner planner;

    public Reply chat(String profile, String key, String message) {
        var agent = factory.open(profile, key);
        if (message == null || message.isBlank()) throw InventoryError.invalid("Mensaje vacío");
        var plan = planner.plan(agent, message);
        if (plan.skill() == null) return new Reply("mensaje", null, plan.text());
        var skill = agent.require(plan.skill());
        if (skill.requiresConfirmation()) {
            skill.validate(plan.input());
            return new Reply("pendiente_confirmacion", plan.skill(), plan.input());
        }
        return new Reply("ejecutado", plan.skill(), skill.execute(plan.input()));
    }

    public Reply execute(String profile, String key, String name, JsonNode input) {
        var skill = factory.open(profile, key).require(name);
        return new Reply("ejecutado", name, skill.execute(input));
    }
}
