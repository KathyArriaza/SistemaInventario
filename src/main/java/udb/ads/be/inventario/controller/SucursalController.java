package udb.ads.be.inventario.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udb.ads.be.inventario.entity.Sucursal;
import udb.ads.be.inventario.service.SucursalService;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {
    private final SucursalService sucursalService;

    @GetMapping
    public List<Sucursal> listarTodas() {
        return sucursalService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sucursal> obtenerPorId(@PathVariable Integer id) {
        return sucursalService.porId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Sucursal crear(@RequestBody Sucursal sucursal) {
        return sucursalService.guardar(sucursal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sucursal> actualizar(@PathVariable Integer id, @RequestBody Sucursal sucursal) {
        return sucursalService.porId(id)
                .map(c -> {
                    sucursal.setIdSucursal(id);
                    return ResponseEntity.ok(sucursalService.guardar(sucursal));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (sucursalService.porId(id).isPresent()) {
            sucursalService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
