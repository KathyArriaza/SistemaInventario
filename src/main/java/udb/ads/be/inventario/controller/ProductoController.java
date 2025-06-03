package udb.ads.be.inventario.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import udb.ads.be.inventario.entity.Producto;
import udb.ads.be.inventario.service.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;


    @GetMapping
    public List<Producto> listarTodos() {
        return productoService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Integer id) {
        return productoService.porId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto) {
        return productoService.guardar(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Integer id, @RequestBody Producto producto) {
        return productoService.porId(id)
                .map(p -> {
                    producto.setIdProducto(id);
                    return ResponseEntity.ok(productoService.guardar(producto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (productoService.porId(id).isPresent()) {
            productoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
