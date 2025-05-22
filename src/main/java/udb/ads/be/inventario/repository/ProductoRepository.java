package udb.ads.be.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udb.ads.be.inventario.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByCategoriaNombre(String nombreCategoria);
}
