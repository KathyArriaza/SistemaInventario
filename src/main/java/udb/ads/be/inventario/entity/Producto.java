package udb.ads.be.inventario.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name="productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Producto {
    @Id
    @GeneratedValue(strategy=IDENTITY)
    private Integer idProducto;
    @Column(nullable=false, unique=true, length=50) private String codigo;
    @Column(nullable=false, length=100) private String nombre;
    private String descripcion;
    @Column(nullable=false) private BigDecimal precio;

    @ManyToOne(fetch=LAZY)
    @JoinColumn(name="idCategoria")
    private Categoria categoria;

    private String imagenUrl;
}
