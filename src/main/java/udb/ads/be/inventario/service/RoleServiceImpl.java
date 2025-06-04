package udb.ads.be.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Role;
import udb.ads.be.inventario.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService{
    private final RoleRepository repo;

    @Override
    public List<Role> listarTodos() {
        return repo.findAll();
    }

    @Override
    public Optional<Role> porId(Integer id) {
        return repo.findById(id);
    }

    @Override
    public Role guardar(Role role) {
        return repo.save(role);
    }

    @Override
    public void eliminar(Integer id) {
        repo.deleteById(id);
    }
}
