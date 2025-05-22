package udb.ads.be.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import udb.ads.be.inventario.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}
