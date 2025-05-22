package udb.ads.be.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udb.ads.be.inventario.entity.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
}
