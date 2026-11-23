package udb.ads.be.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import udb.ads.be.inventario.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, String> {}
