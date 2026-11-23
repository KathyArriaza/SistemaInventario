package com.proyecto.inventario.skills;

import com.fasterxml.jackson.databind.JsonNode;

public interface InventorySkill {
    Descriptor descriptor();
    void validate(JsonNode input);
    default boolean requiresConfirmation() { return false; }
    Object execute(JsonNode input);
    record Descriptor(String name, String description, JsonNode parameters) {}
}
