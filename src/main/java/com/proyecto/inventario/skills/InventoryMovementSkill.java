package com.proyecto.inventario.skills;

import com.fasterxml.jackson.databind.*;
import com.proyecto.inventario.inventory.InventoryPort;
import com.proyecto.inventario.inventory.InventoryPort.*;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementSkill extends SkillContract {
    private final InventoryPort inventory;
    public InventoryMovementSkill(InventoryPort inventory, ObjectMapper mapper) throws IOException {
        super(mapper, "inventory_movement");
        this.inventory = inventory;
    }

    @Override public boolean requiresConfirmation() { return true; }

    protected Receipt apply(JsonNode input) {
        var command = new Movement(UUID.fromString(input.get("operacion").textValue()), input.get("sku").textValue(),
                input.get("sucursal").intValue(), Tipo.valueOf(input.get("tipo").textValue()), input.get("cantidad").intValue());
        return inventory.move(command);
    }
}
