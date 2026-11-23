package udb.ads.be.inventario.repository;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import udb.ads.be.inventario.entity.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    @Query("select i from Inventario i join fetch i.producto p left join fetch p.categoria join fetch i.sucursal order by p.codigo, i.sucursal.idSucursal")
    List<Inventario> snapshot();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventario i where i.idInventario = :id")
    Optional<Inventario> lockById(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventario i where i.producto.codigo = :sku and i.sucursal.idSucursal = :branch")
    Optional<Inventario> lockStock(@Param("sku") String sku, @Param("branch") int branch);
}
