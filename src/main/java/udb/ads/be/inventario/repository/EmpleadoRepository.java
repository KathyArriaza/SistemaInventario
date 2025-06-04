package udb.ads.be.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udb.ads.be.inventario.entity.Empleado;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    Optional<Empleado> findByEmail(String email);
}
