package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.Sesion;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.LoginResponse;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermisosTest {

    private static Permisos de(Rol rol, int id) {
        Sesion sesion = new Sesion();
        sesion.iniciar(new LoginResponse("t", "Bearer", Instant.now().plusSeconds(60),
                new Usuario(id, "U", "u@test.com", rol, null, null, true, null)));
        return new Permisos(sesion);
    }

    private static Cliente clienteDe(Integer vendedorId) {
        return new Cliente(1, "C", null, "c@test.com", null, null, EstadoCliente.ACTIVO, vendedorId, null, null, null);
    }

    @Test
    void unVendedorSoloGestionaSusClientesYNoVeUsuarios() {
        Permisos vendedor = de(Rol.VENDEDOR, 3);

        assertTrue(vendedor.puedeGestionarCliente(clienteDe(3)));
        assertFalse(vendedor.puedeGestionarCliente(clienteDe(4)));
        assertFalse(vendedor.puedeVerUsuarios());
        assertFalse(vendedor.puedeAsignarVendedores());
        assertFalse(vendedor.puedeGestionarCatalogo());
    }

    @Test
    void contratosYTicketsSeGestionanSegunElVendedorDelCliente() {
        Permisos vendedor = de(Rol.VENDEDOR, 3);

        assertTrue(vendedor.puedeGestionarClienteDe(3));
        assertFalse(vendedor.puedeGestionarClienteDe(4));
        assertFalse(vendedor.puedeGestionarClienteDe(null));
        assertTrue(de(Rol.GERENTE, 2).puedeGestionarClienteDe(null));
    }

    @Test
    void unGerenteAsignaVendedoresPeroNoAdministraUsuarios() {
        Permisos gerente = de(Rol.GERENTE, 2);

        assertTrue(gerente.puedeGestionarCliente(clienteDe(3)));
        assertTrue(gerente.puedeVerUsuarios());
        assertTrue(gerente.puedeAsignarVendedores());
        assertFalse(gerente.puedeAdministrarUsuarios());
    }

    @Test
    void unAdminPuedeTodo() {
        Permisos admin = de(Rol.ADMIN, 1);

        assertTrue(admin.puedeAdministrarUsuarios());
        assertTrue(admin.puedeEliminarCategorias());
        assertTrue(admin.puedeGestionarCliente(clienteDe(null)));
    }

    @Test
    void sinSesionNoSePuedeNada() {
        Permisos nadie = new Permisos(new Sesion());

        assertFalse(nadie.puedeGestionarCliente(clienteDe(3)));
        assertFalse(nadie.gestionaEquipo());
    }
}
