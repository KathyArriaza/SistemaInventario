package udb.ads.be.inventario.service;

import udb.ads.be.inventario.entity.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaService {
    List<Categoria> listarTodas();
    Optional<Categoria> porId(Integer id);
    Categoria guardar(Categoria categoria);
    void eliminar(Integer id);
}
