package udb.ads.be.inventario.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name="inventarios", uniqueConstraints=@UniqueConstraint(columnNames={"idProducto","idSucursal"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Inventario {
    @Id
    @GeneratedValue(strategy=IDENTITY)
    private Integer idInventario;

    @ManyToOne(fetch=LAZY)
    @JoinColumn(name="idProducto", nullable=false)
    private Producto producto;

    @ManyToOne(fetch=LAZY)
    @JoinColumn(name="idSucursal", nullable=false)
    private Sucursal sucursal;

    @Column(nullable=false) private Integer cantidad;

    @Column(nullable=false, updatable=false,
            columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime creada;

    @UpdateTimestamp
    private LocalDateTime ultimaActualizacion;
}
