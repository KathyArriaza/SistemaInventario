package udb.ads.be.inventario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import static jakarta.persistence.GenerationType.IDENTITY;


@Entity
@Table(name="sucursales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sucursal {
        @Id
        @GeneratedValue(strategy=IDENTITY)
        private Integer idSucursal;
        @Column(nullable=false, length=100) private String nombre;
        private String direccion;
        private String telefono;


}
