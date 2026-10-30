package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.LoginRequest;
import com.devsuj.crmdesktop.model.LoginResponse;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/**
 * {@link AuthApi} "falsa" para las pruebas y el modo demostración: acepta los usuarios del seed
 * con la contraseña {@value #PASSWORD_DEMO}.
 */
public class AuthApiEnMemoria implements AuthApi {

    public static final String PASSWORD_DEMO = "Password123";

    private static final LocalDateTime ALTA = LocalDateTime.of(2025, 9, 1, 9, 0);
    private static final List<Usuario> USUARIOS = List.of(
            new Usuario(1, "Ana Torres", "ana.torres@crmventas.test", Rol.ADMIN, null, null, true, ALTA),
            new Usuario(2, "Luis Ramírez", "luis.ramirez@crmventas.test", Rol.GERENTE, null, null, true, ALTA),
            new Usuario(3, "Carla Mendoza", "carla.mendoza@crmventas.test", Rol.VENDEDOR, 2, "Luis Ramírez", true, ALTA));

    private Usuario actual;

    @Override
    public synchronized LoginResponse login(LoginRequest credenciales) {
        String email = credenciales.email() == null ? "" : credenciales.email().strip().toLowerCase(Locale.ROOT);
        Usuario usuario = USUARIOS.stream()
                .filter(u -> u.email().equals(email) && PASSWORD_DEMO.equals(credenciales.password()))
                .findFirst()
                .orElseThrow(() -> new ApiException(401, "Email o contraseña incorrectos."));
        actual = usuario;
        return new LoginResponse("token-demo-" + usuario.id(), "Bearer",
                Instant.now().plus(8, ChronoUnit.HOURS), usuario);
    }

    @Override
    public synchronized Usuario yo() {
        if (actual == null) {
            throw new ApiException(401, "Debes iniciar sesión.");
        }
        return actual;
    }
}
