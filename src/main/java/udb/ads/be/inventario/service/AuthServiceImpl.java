package udb.ads.be.inventario.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Empleado;
import udb.ads.be.inventario.repository.EmpleadoRepository;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final EmpleadoRepository empleadoRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public Empleado login(String email, String contraseña) {
        Empleado empleado = empleadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email no encontrado"));
        if (!passwordEncoder.matches(contraseña, empleado.getContraseña())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return empleado;
    }
}
