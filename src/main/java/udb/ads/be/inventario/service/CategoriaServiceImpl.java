package udb.ads.be.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Categoria;
import udb.ads.be.inventario.repository.CategoriaRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService{
    private final CategoriaRepository repo;

    @Override
    public List<Categoria> listarTodas() {
        return repo.findAll();
    }

    @Override
    public Optional<Categoria> porId(Integer id) {
        return repo.findById(id);
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        return repo.save(categoria);
    }

    @Override
    public void eliminar(Integer id) {
        repo.deleteById(id);
    }
}
