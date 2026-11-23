package com.proyecto.inventario.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.inventario.config.InventoryConfig.Settings;
import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import udb.ads.be.inventario.entity.*;
import udb.ads.be.inventario.repository.*;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoSeed implements CommandLineRunner {
    record Row(String sku, String nombre, String categoria, String sucursal, BigDecimal precio, int cantidad, int diasDesdeMovimiento) {}
    private final ObjectMapper mapper;
    private final Settings settings;
    private final java.time.Clock clock;
    private final CategoriaRepository categories;
    private final ProductoRepository products;
    private final SucursalRepository branches;
    private final InventarioRepository inventory;

    @Transactional
    public void run(String... args) throws Exception {
        Map<String,Categoria> catalog = new HashMap<>();
        Map<String,Sucursal> locations = new HashMap<>();
        Map<String,Producto> items = new HashMap<>();
        try (var input = settings.seed().getInputStream()) {
            for (Row row : mapper.readValue(input, Row[].class)) {
                var category = catalog.computeIfAbsent(row.categoria(), n -> categories.save(Categoria.builder().nombre(n).build()));
                var branch = locations.computeIfAbsent(row.sucursal(), n -> branches.save(Sucursal.builder().nombre(n).build()));
                var product = items.computeIfAbsent(row.sku(), sku -> products.save(Producto.builder().codigo(sku)
                        .nombre(row.nombre()).precio(row.precio()).categoria(category).build()));
                inventory.save(Inventario.builder().producto(product).sucursal(branch).cantidad(row.cantidad())
                        .ultimoMovimiento(java.time.LocalDateTime.now(clock).minusDays(row.diasDesdeMovimiento())).build());
            }
        }
    }
}
