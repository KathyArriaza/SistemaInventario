package com.proyecto.inventario.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.inventario.config.InventoryConfig.Settings;
import com.proyecto.inventario.inventory.InventoryError;
import com.proyecto.inventario.skills.InventorySkill;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.io.support.ResourcePatternResolver;

import org.springframework.stereotype.Component;

@Component
public class AgentFactory {
    public record Template(String id, String rol, String tono, String reglas, List<String> skills) {}
    public record CustomAgent(Template template, Map<String, InventorySkill> skills) {
        public InventorySkill require(String name) {
            var skill = skills.get(name);
            if (skill == null) throw new InventoryError(InventoryError.Kind.FORBIDDEN, "Skill fuera del perfil");
            return skill;
        }
    }
    private final Map<String,CustomAgent> profiles = new HashMap<>();
    private final Settings settings;

    public AgentFactory(Settings settings, ResourcePatternResolver resources, ObjectMapper mapper,
                        List<InventorySkill> beans) throws IOException {
        this.settings = settings;
        var catalog = beans.stream().collect(Collectors.toMap(s -> s.descriptor().name(), Function.identity()));
        for (var file : resources.getResources(settings.templates())) {
            try (var in = file.getInputStream()) {
                Template t = mapper.readValue(in, Template.class);
                Map<String,InventorySkill> enabled = new LinkedHashMap<>();
                for (String name : t.skills()) enabled.put(name, Objects.requireNonNull(catalog.get(name), name));
                if (profiles.putIfAbsent(t.id(), new CustomAgent(t, Map.copyOf(enabled))) != null)
                    throw new IllegalStateException("Perfil duplicado: " + t.id());
            }
        }
        if (profiles.isEmpty()) throw new IllegalStateException("Faltan plantillas");
    }

    public CustomAgent open(String type, String key) {
        var profile = profiles.get(type);
        if (profile == null) throw InventoryError.missing("Perfil desconocido");
        String expected = settings.profileKeys().get(type);
        if (expected == null || expected.isBlank() || key == null ||
                !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8)))
            throw new InventoryError(InventoryError.Kind.FORBIDDEN, "Clave incorrecta para el perfil");
        return profile;
    }
}
