package udb.ads.be.inventario.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udb.ads.be.inventario.entity.Empleado;
import udb.ads.be.inventario.service.EmpleadoService;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {
    private final EmpleadoService empleadoService;

    @GetMapping
    public List<Empleado> listarTodos() {
        return empleadoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> porId(@PathVariable Integer id) {
        return empleadoService.porId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Empleado guardar(@RequestBody Empleado empleado) {
        return empleadoService.guardar(empleado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizar(@PathVariable Integer id, @RequestBody Empleado empleado) {
        return empleadoService.porId(id)
                .map(e -> {
                    e.setNombre(empleado.getNombre());
                    e.setApellido(empleado.getApellido());
                    e.setEmail(empleado.getEmail());
                    e.setContraseña(empleado.getContraseña());
                    e.setRole(empleado.getRole());
                    return ResponseEntity.ok(empleadoService.guardar(e));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (empleadoService.porId(id).isPresent()) {
            empleadoService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
