package udb.ads.be.inventario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.proyecto.inventario.inventory.InventoryPort.*;

@Entity
@Table(name = "movimientos_agente")
@Getter
@NoArgsConstructor
public class Movimiento {
    @Id private String operacion;
    @Column(nullable = false) private String sku;
    private int sucursal;
    @Enumerated(EnumType.STRING) private Tipo tipo;
    private int cantidad;
    private int anterior;
    private int saldo;

    public Movimiento(Movement m, int anterior, int saldo) {
        this.operacion = m.operacion().toString();
        this.sku = m.sku();
        this.sucursal = m.sucursal();
        this.tipo = m.tipo();
        this.cantidad = m.cantidad();
        this.anterior = anterior;
        this.saldo = saldo;
    }

    public Receipt receipt() {
        return new Receipt(java.util.UUID.fromString(operacion), sku, sucursal, tipo, cantidad, anterior, saldo);
    }
}
