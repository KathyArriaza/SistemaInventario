package com.proyecto.inventario.config;

import java.net.URI;
import java.time.Clock;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(InventoryConfig.Settings.class)
public class InventoryConfig {
    @ConfigurationProperties("inventory")
    public record Settings(Resource seed, int critical, int low, int inactiveDays, String templates,
                           java.util.Map<String,String> profileKeys, URI llmUrl, String llmKey,
                           String model, double temperature) {
        public Settings {
            if (critical < 0 || low < critical || inactiveDays < 1)
                throw new IllegalArgumentException("Revise los umbrales de inventario");
            var keys = profileKeys.values().stream().filter(k -> !k.isBlank()).toList();
            if (keys.stream().distinct().count() != keys.size())
                throw new IllegalArgumentException("Las claves deben ser distintas");
        }
    }

    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
