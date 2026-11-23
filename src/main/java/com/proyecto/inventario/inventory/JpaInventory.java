package com.proyecto.inventario.inventory;

import java.util.List;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import udb.ads.be.inventario.entity.*;
import udb.ads.be.inventario.repository.*;

@Repository
@RequiredArgsConstructor
public class JpaInventory implements InventoryPort {
    private final InventarioRepository inventory;
    private final MovimientoRepository history;
    private final EntityManager entities;
    private final java.time.Clock clock;

    @Transactional(readOnly = true)
    public List<Stock> stocks() {
        return inventory.snapshot().stream().map(i -> {
            Producto p = i.getProducto();
            var time = i.getUltimoMovimiento();
            return new Stock(p.getCodigo(), p.getNombre(), p.getCategoria() == null ? null : p.getCategoria().getNombre(),
                    i.getSucursal().getIdSucursal(), i.getCantidad(), p.getPrecio(), time == null ? null : time.toLocalDate());
        }).toList();
    }

    @Transactional
    public Receipt move(Movement m) {
        if (m.cantidad() <= 0) throw InventoryError.invalid("Cantidad debe ser positiva");
        var row = inventory.lockStock(m.sku(), m.sucursal())
                .orElseThrow(() -> InventoryError.missing("SKU sin inventario en la sucursal"));
        var prior = history.findById(m.operacion().toString());
        if (prior.isPresent()) {
            Receipt r = prior.get().receipt();
            if (r.sku().equals(m.sku()) && r.sucursal() == m.sucursal() && r.tipo() == m.tipo() && r.cantidad() == m.cantidad())
                return r;
            throw InventoryError.conflict("Identificador ya utilizado para otro movimiento");
        }
        int before = row.getCantidad();
        long after = (long) before + (m.tipo() == Tipo.ENTRADA ? m.cantidad() : -m.cantidad());
        if (after < 0 || after > Integer.MAX_VALUE) throw InventoryError.conflict("Saldo fuera de rango");
        var entry = new Movimiento(m, before, (int) after);
        row.setCantidad((int) after);
        row.setUltimoMovimiento(java.time.LocalDateTime.now(clock));
        entities.persist(entry);
        entities.flush();
        return entry.receipt();
    }
}
