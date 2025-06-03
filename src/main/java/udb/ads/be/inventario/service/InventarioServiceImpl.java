package udb.ads.be.inventario.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Inventario;
import udb.ads.be.inventario.entity.Producto;
import udb.ads.be.inventario.entity.Sucursal;
import udb.ads.be.inventario.service.ProductoService;
import udb.ads.be.inventario.service.SucursalService;
import udb.ads.be.inventario.repository.InventarioRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {
    private final ProductoService productoService;
    private final SucursalService sucursalService;

    private final InventarioRepository inventarioRepository;

    @Override
    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    @Override
    public Optional<Inventario> porId(Integer id) {
        return inventarioRepository.findById(id);
    }

    @Override
    public Inventario guardar(Inventario inventario) {
        if (inventario.getProducto() != null && inventario.getProducto().getIdProducto() != null) {
            Producto producto = productoService.porId(inventario.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            inventario.setProducto(producto);
        }
        if (inventario.getSucursal() != null && inventario.getSucursal().getIdSucursal() != null) {
            Sucursal sucursal = sucursalService.porId(inventario.getSucursal().getIdSucursal())
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
            inventario.setSucursal(sucursal);
        }
        return inventarioRepository.save(inventario);
    }

    @Override
    public Optional<Inventario> actualizar(Integer id, Inventario inventario) {
        return inventarioRepository.findById(id).map(existing -> {
            existing.setCantidad(inventario.getCantidad());

            if (inventario.getProducto() != null && inventario.getProducto().getIdProducto() != null) {
                Producto producto = productoService.porId(inventario.getProducto().getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                existing.setProducto(producto);
            }
            if (inventario.getSucursal() != null && inventario.getSucursal().getIdSucursal() != null) {
                Sucursal sucursal = sucursalService.porId(inventario.getSucursal().getIdSucursal())
                        .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
                existing.setSucursal(sucursal);
            }
            return inventarioRepository.save(existing);
        });


    }
    @Override
        public void eliminar(Integer id) {
            inventarioRepository.deleteById(id);
        }

}
