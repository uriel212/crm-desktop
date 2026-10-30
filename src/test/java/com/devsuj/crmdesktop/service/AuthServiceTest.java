package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.ApiException;
import com.devsuj.crmdesktop.client.AuthApiEnMemoria;
import com.devsuj.crmdesktop.client.Sesion;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private final Sesion sesion = new Sesion();
    private final AuthService service = new AuthService(new AuthApiEnMemoria(), sesion);

    @Test
    void conCredencialesCorrectasGuardaElTokenYElUsuario() {
        Usuario usuario = service.iniciarSesion("  luis.ramirez@crmventas.test ", "Password123");

        assertEquals(Rol.GERENTE, usuario.rol());
        assertTrue(sesion.token().isPresent());
        assertEquals(usuario, service.usuarioActual());
    }

    @Test
    void validaAntesDeLlamarALaApi() {
        ValidacionException error = assertThrows(ValidacionException.class, () -> service.iniciarSesion("", ""));

        assertTrue(error.erroresPorCampo().containsKey(AuthService.EMAIL));
        assertTrue(error.erroresPorCampo().containsKey(AuthService.PASSWORD));
    }

    @Test
    void conContrasenaIncorrectaPropagaEl401SinAbrirSesion() {
        ApiException error = assertThrows(ApiException.class,
                () -> service.iniciarSesion("ana.torres@crmventas.test", "otra"));

        assertEquals(401, error.status());
        assertFalse(sesion.iniciada());
    }

    @Test
    void cerrarSesionBorraElToken() {
        service.iniciarSesion("ana.torres@crmventas.test", "Password123");

        service.cerrarSesion();

        assertTrue(sesion.token().isEmpty());
    }
}
