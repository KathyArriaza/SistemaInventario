package udb.ads.be.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Producto;
import udb.ads.be.inventario.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService{
    private final ProductoRepository repo;

    @Override
    public List<Producto> listarTodos() {
        return repo.findAll();
    }

    @Override
    public Producto guardar(Producto p) {
        return repo.save(p);
    }

    @Override
    public Optional<Producto> porId(Integer id) {
        return repo.findById(id);
    }

    @Override
    public List<Producto> porCategoria(String nombreCategoria) {
        return repo.findByCategoriaNombre(nombreCategoria);
    }
}
