package udb.ads.be.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Categoria;
import udb.ads.be.inventario.entity.Sucursal;
import udb.ads.be.inventario.repository.SucursalRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SucursalServiceImpl implements SucursalService {
    private final SucursalRepository repo;

    @Override
    public List<Sucursal> listarTodas() {
        return repo.findAll();
    }

    @Override
    public Optional<Sucursal> porId(Integer id) {
        return repo.findById(id);
    }

    @Override
    public Sucursal guardar(Sucursal sucursal) {
        return repo.save(sucursal);
    }

    @Override
    public void eliminar(Integer id) {
        repo.deleteById(id);
    }
}
