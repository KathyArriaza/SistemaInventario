package udb.ads.be.inventario.service;

import udb.ads.be.inventario.entity.Empleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoService {
    List<Empleado> listarTodos();
    Optional<Empleado> porId(Integer id);
    Empleado guardar(Empleado empleado);
    void eliminar(Integer id);
}
