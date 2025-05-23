package udb.ads.be.inventario.service;

import udb.ads.be.inventario.entity.Sucursal;

import java.util.List;
import java.util.Optional;

public interface SucursalService {
    List<Sucursal> listarTodas();
    Optional<Sucursal> porId(Integer id);
    Sucursal guardar(Sucursal sucursal);
    void eliminar(Integer id);
}
