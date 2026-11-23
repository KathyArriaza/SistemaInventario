package com.proyecto.inventario.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InventoryPort {
    record Stock(String sku, String nombre, String categoria, int sucursal, int cantidad,
                 BigDecimal precio, LocalDate ultimaActividad) {
        public BigDecimal valor() { return precio.multiply(BigDecimal.valueOf(cantidad)); }
    }
    enum Tipo { ENTRADA, SALIDA }
    record Movement(UUID operacion, String sku, int sucursal, Tipo tipo, int cantidad) {}
    record Receipt(UUID operacion, String sku, int sucursal, Tipo tipo, int cantidad,
                   int anterior, int saldo) {}
    List<Stock> stocks();
    Receipt move(Movement movement);
}
