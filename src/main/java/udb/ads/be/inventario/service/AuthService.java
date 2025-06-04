package udb.ads.be.inventario.service;

import udb.ads.be.inventario.entity.Empleado;

public interface AuthService {
    Empleado login(String email, String contraseña);
}
