package com.proyecto.inventario.skills;

import com.fasterxml.jackson.databind.*;
import com.proyecto.inventario.inventory.InventoryError;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;

public abstract class SkillContract implements InventorySkill {
    private final Descriptor descriptor;

    protected SkillContract(ObjectMapper mapper, String resource) throws IOException {
        try (var input = new ClassPathResource("skills/" + resource + ".json").getInputStream()) {
            descriptor = mapper.readValue(input, Descriptor.class);
        }
    }

    public Descriptor descriptor() { return descriptor; }

    @Override
    public final Object execute(JsonNode input) {
        validate(input);
        return apply(input);
    }

    public void validate(JsonNode input) {
        var schema = descriptor.parameters();
        if (input == null || !input.isObject()) throw InventoryError.invalid("Se requiere un objeto JSON");
        for (var field : schema.path("required")) {
            if (!input.hasNonNull(field.asText())) throw InventoryError.invalid("Falta " + field.asText());
        }
        input.fields().forEachRemaining(entry -> {
            String field = entry.getKey();
            var rule = schema.path("properties").get(field);
            var value = entry.getValue();
            if (rule == null) throw InventoryError.invalid("Campo desconocido: " + field);
            boolean valid = switch (rule.path("type").asText()) {
                case "integer" -> value.isIntegralNumber() && value.canConvertToInt()
                        && value.intValue() >= rule.path("minimum").asInt() && value.intValue() <= rule.path("maximum").asInt();
                case "string" -> value.isTextual() && !value.asText().isBlank();
                default -> false;
            };
            if (!valid || (rule.has("enum") && !contains(rule.get("enum"), value)))
                throw InventoryError.invalid("Valor inválido: " + field);
            if ("uuid".equals(rule.path("format").asText())) {
                try {
                    if (!UUID.fromString(value.asText()).toString().equalsIgnoreCase(value.asText())) throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) { throw InventoryError.invalid("UUID inválido"); }
            }
        });
    }

    private static boolean contains(JsonNode options, JsonNode value) {
        for (var option : options) if (option.equals(value)) return true;
        return false;
    }

    protected abstract Object apply(JsonNode input);
}
