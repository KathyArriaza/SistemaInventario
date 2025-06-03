package udb.ads.be.inventario.service;

import udb.ads.be.inventario.entity.Inventario;

import java.util.List;
import java.util.Optional;

public interface InventarioService {
    List<Inventario> listarTodos();
    Optional<Inventario> porId(Integer id);
    Inventario guardar(Inventario inventario);
    void eliminar(Integer id);
    Optional<Inventario> actualizar(Integer id, Inventario inventario);
}
