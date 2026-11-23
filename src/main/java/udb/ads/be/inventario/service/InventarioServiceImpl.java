package udb.ads.be.inventario.service;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import udb.ads.be.inventario.entity.Inventario;
import udb.ads.be.inventario.repository.InventarioRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {
    private final Clock clock;
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
    @Transactional
    public Inventario guardar(Inventario inventario) {
        return aplicar(new Inventario(), inventario);
    }

    @Override
    @Transactional
    public Optional<Inventario> actualizar(Integer id, Inventario inventario) {
        return inventarioRepository.lockById(id).map(existing -> aplicar(existing, inventario));
    }

    private Inventario aplicar(Inventario destino, Inventario datos) {
        if (datos.getCantidad() == null || datos.getCantidad() < 0)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cantidad inválida");
        boolean cambio = !datos.getCantidad().equals(destino.getCantidad());
        if (datos.getProducto() != null && datos.getProducto().getIdProducto() != null) {
            var producto = productoService.porId(datos.getProducto().getIdProducto())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Producto no encontrado"));
            cambio |= destino.getProducto() == null || !producto.getIdProducto().equals(destino.getProducto().getIdProducto());
            destino.setProducto(producto);
        }
        if (datos.getSucursal() != null && datos.getSucursal().getIdSucursal() != null) {
            var sucursal = sucursalService.porId(datos.getSucursal().getIdSucursal())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Sucursal no encontrada"));
            cambio |= destino.getSucursal() == null || !sucursal.getIdSucursal().equals(destino.getSucursal().getIdSucursal());
            destino.setSucursal(sucursal);
        }
        if (destino.getProducto() == null || destino.getSucursal() == null)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Se requiere producto y sucursal");
        if (cambio) destino.setUltimoMovimiento(LocalDateTime.now(clock));
        destino.setCantidad(datos.getCantidad());
        return inventarioRepository.save(destino);
    }

    @Override
    public void eliminar(Integer id) {
        inventarioRepository.deleteById(id);
    }
}
