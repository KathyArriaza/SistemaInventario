package com.proyecto.inventario.skills;

import com.fasterxml.jackson.databind.*;
import com.proyecto.inventario.config.InventoryConfig.Settings;
import com.proyecto.inventario.inventory.InventoryPort;
import java.io.IOException;
import java.time.*;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LowStockAlertSkill extends SkillContract {
    private final InventoryPort inventory;
    private final Settings settings;
    private final Clock clock;

    public LowStockAlertSkill(InventoryPort inventory, Settings settings, Clock clock, ObjectMapper mapper) throws IOException {
        super(mapper, "low_stock");
        this.inventory = inventory;
        this.settings = settings;
        this.clock = clock;
    }

    record Alert(String sku, int sucursal, int cantidad, String nivel, String rotacion) {}

    protected List<Alert> apply(JsonNode input) {
        LocalDate cutoff = LocalDate.now(clock).minusDays(settings.inactiveDays());
        return inventory.stocks().stream().map(s -> {
            String level = s.cantidad() <= settings.critical() ? "CRITICO" : s.cantidad() <= settings.low() ? "BAJO" : "NORMAL";
            String activity = s.ultimaActividad() == null ? "DESCONOCIDA" :
                    s.ultimaActividad().isAfter(cutoff) ? "RECIENTE" : "SIN_ROTACION";
            return new Alert(s.sku(), s.sucursal(), s.cantidad(), level, activity);
        }).filter(a -> !a.nivel().equals("NORMAL") || !a.rotacion().equals("RECIENTE")).toList();
    }
}
