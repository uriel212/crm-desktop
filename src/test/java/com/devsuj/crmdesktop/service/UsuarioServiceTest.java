package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.ApiException;
import com.devsuj.crmdesktop.client.UsuarioApiEnMemoria;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioServiceTest {

    private final UsuarioService service = new UsuarioService(UsuarioApiEnMemoria.conDatosDeEjemplo());

    private Usuario buscar(String texto) {
        return service.buscar(texto, null, null, 0).contenido().getFirst();
    }

    @Test
    void losResponsablesSonVendedoresYGerentesActivos() {
        assertTrue(service.responsables().stream().allMatch(u -> u.activo() && u.rol() != Rol.ADMIN));
        assertEquals(4, service.responsables().size()); // Luis, Carla, Jorge y Sofía (Mario está inactivo)
    }

    @Test
    void alCrearExigeContrasenaDe8Caracteres() {
        var datos = new UsuarioFormulario("Pedro", "pedro@crmventas.test", "corta", Rol.VENDEDOR, null);

        ValidacionException error = assertThrows(ValidacionException.class, () -> service.guardar(null, datos));

        assertTrue(error.erroresPorCampo().containsKey(UsuarioService.PASSWORD));
    }

    @Test
    void soloElVendedorConservaElGerente() {
        Usuario luis = buscar("luis");
        var datos = new UsuarioFormulario("Pedro", "pedro@crmventas.test", "Password123", Rol.GERENTE, luis);

        assertNull(service.guardar(null, datos).gerenteId());
    }

    @Test
    void desactivarUnVendedorConClientesPideConfirmacion() {
        Usuario carla = buscar("carla");

        ApiException error = assertThrows(ApiException.class, () -> service.cambiarEstado(carla, false, false));

        assertEquals(5, UsuarioService.clientesAsignados(error).orElseThrow());
        assertFalse(service.cambiarEstado(carla, false, true).activo());
    }

    @Test
    void cambiarLaContrasenaPropiaPideLaActualYQueCoincida() {
        Usuario ana = buscar("ana");

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> service.cambiarPassword(ana, true, "", "NuevaClave1", "OtraClave1"));

        assertTrue(error.erroresPorCampo().containsKey(UsuarioService.PASSWORD_ACTUAL));
        assertTrue(error.erroresPorCampo().containsKey(UsuarioService.CONFIRMACION));
    }
}
