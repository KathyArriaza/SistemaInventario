package udb.ads.be.inventario.entity.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String contraseña;
}
