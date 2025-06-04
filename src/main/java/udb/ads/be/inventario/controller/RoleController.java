package udb.ads.be.inventario.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udb.ads.be.inventario.entity.Role;
import udb.ads.be.inventario.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public List<Role> listarTodos() {
        return roleService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> porId(@PathVariable Integer id) {
        return roleService.porId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Role guardar(@RequestBody Role role) {
        return roleService.guardar(role);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> actualizar(@PathVariable Integer id, @RequestBody Role role) {
        return roleService.porId(id)
                .map(r -> {
                    r.setNombre(role.getNombre());
                    return ResponseEntity.ok(roleService.guardar(r));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (roleService.porId(id).isPresent()) {
            roleService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
