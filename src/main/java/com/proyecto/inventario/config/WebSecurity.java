package com.proyecto.inventario.config;

import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurity {
    // Los agentes usan exclusivamente la clave de cabecera validada al abrir el perfil.
    @Bean @Order(1)
    SecurityFilterChain agents(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/agents/**").csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll()).build();
    }

    @Bean
    SecurityFilterChain originalApi(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults()).httpBasic(Customizer.withDefaults()).build();
    }
}
