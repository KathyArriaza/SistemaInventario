package com.proyecto.inventario.skills;

import com.fasterxml.jackson.databind.*;
import com.proyecto.inventario.inventory.InventoryPort;
import com.proyecto.inventario.inventory.InventoryPort.Stock;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StockQuerySkill extends SkillContract {
    private final InventoryPort inventory;

    public StockQuerySkill(InventoryPort inventory, ObjectMapper mapper) throws IOException {
        super(mapper, "stock_query");
        this.inventory = inventory;
    }

    record Result(List<Stock> posiciones, long unidades, BigDecimal valorTotal) {}

    protected Result apply(JsonNode input) {
        var rows = inventory.stocks().stream()
                .filter(s -> !input.has("sku") || input.get("sku").asText().equalsIgnoreCase(s.sku()))
                .filter(s -> !input.has("categoria") || input.get("categoria").asText().equalsIgnoreCase(s.categoria()))
                .toList();
        return new Result(rows, rows.stream().mapToLong(Stock::cantidad).sum(),
                rows.stream().map(Stock::valor).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
