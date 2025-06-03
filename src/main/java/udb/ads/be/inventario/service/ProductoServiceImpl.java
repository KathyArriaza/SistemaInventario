package udb.ads.be.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Categoria;
import udb.ads.be.inventario.entity.Producto;
import udb.ads.be.inventario.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService{
    private final ProductoRepository repo;
    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    @Override
    public List<Producto> listarTodos() {
        return repo.findAll();
    }

    @Override
    public Producto guardar(Producto producto) {
        if (producto.getCategoria() != null && producto.getCategoria().getIdCategoria() != null) {
            Categoria categoria = categoriaService.porId(producto.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }
        return productoRepository.save(producto);
    }

    @Override
    public Optional<Producto> porId(Integer id) {
        return repo.findById(id);
    }

    @Override
    public void eliminar(Integer id) {
        repo.deleteById(id);
    }
    @Override
    public List<Producto> porCategoria(String nombreCategoria) {
        return repo.findByCategoriaNombre(nombreCategoria);
    }
}
