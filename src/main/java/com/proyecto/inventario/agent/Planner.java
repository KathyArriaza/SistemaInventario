package com.proyecto.inventario.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.inventario.agent.AgentFactory.CustomAgent;

public interface Planner {
    record Plan(String text, String skill, JsonNode input) {}
    Plan plan(CustomAgent agent, String message);
}
