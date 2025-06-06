package udb.ads.be.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Empleado;
import udb.ads.be.inventario.entity.Role;
import udb.ads.be.inventario.repository.EmpleadoRepository;
import udb.ads.be.inventario.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {
    private final EmpleadoRepository repo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Empleado> listarTodos() {
        return repo.findAll();
    }

    @Override
    public Optional<Empleado> porId(Integer id) {
        return repo.findById(id);
    }

    @Override
    public Empleado guardar(Empleado empleado) {
        if (empleado.getRole() != null && empleado.getRole().getIdRol() != null) {
            Role roleCompleto = roleRepo.findById(empleado.getRole().getIdRol())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            empleado.setRole(roleCompleto);
        }

        // Encriptar la contraseña solo si es nueva o está en texto plano
        if (empleado.getContraseña() != null && !empleado.getContraseña().startsWith("$2a$")) {
            empleado.setContraseña(passwordEncoder.encode(empleado.getContraseña()));
        }
        return repo.save(empleado);
    }

    @Override
    public void eliminar(Integer id) {
        repo.deleteById(id);
    }
}
