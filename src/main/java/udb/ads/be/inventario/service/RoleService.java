package udb.ads.be.inventario.service;

import udb.ads.be.inventario.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<Role> listarTodos();
    Optional<Role> porId(Integer id);
    Role guardar(Role role);
    void eliminar(Integer id);
}
