package udb.ads.be.inventario.service;

import udb.ads.be.inventario.entity.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<Producto> listarTodos();
    Producto guardar(Producto p);
    Optional<Producto> porId(Integer id);
    List<Producto> porCategoria(String nombreCategoria);
    void eliminar(Integer id);
}
