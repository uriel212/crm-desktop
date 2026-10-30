package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.Sesion;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;

/**
 * Qué puede hacer el usuario actual según su rol. Las pantallas lo usan para ocultar o
 * deshabilitar acciones; la API vuelve a comprobarlo (403) porque la interfaz no es una barrera
 * de seguridad, solo evita que el usuario intente algo que no le está permitido.
 *
 * <p>Reglas (ver {@code crm-api/docs/endpoints.md}): VENDEDOR gestiona lo suyo, GERENTE lo suyo y lo
 * de su equipo, ADMIN todo.</p>
 */
public class Permisos {

    private final Sesion sesion;

    public Permisos(Sesion sesion) {
        this.sesion = sesion;
    }

    public Usuario usuario() {
        return sesion.usuario();
    }

    public boolean esAdmin() {
        return tieneRol(Rol.ADMIN);
    }

    public boolean esVendedor() {
        return tieneRol(Rol.VENDEDOR);
    }

    /** GERENTE o ADMIN: ven usuarios, asignan vendedores y gestionan el catálogo. */
    public boolean gestionaEquipo() {
        return tieneRol(Rol.GERENTE) || tieneRol(Rol.ADMIN);
    }

    public boolean puedeVerUsuarios() {
        return gestionaEquipo();
    }

    public boolean puedeAdministrarUsuarios() {
        return esAdmin();
    }

    public boolean puedeAsignarVendedores() {
        return gestionaEquipo();
    }

    public boolean puedeGestionarCatalogo() {
        return gestionaEquipo();
    }

    public boolean puedeEliminarCategorias() {
        return esAdmin();
    }

    /**
     * Editar, cambiar estado, registrar oportunidades o interacciones de un cliente.
     * Un VENDEDOR solo los suyos. Un GERENTE: los suyos y los de su equipo (aquí se permite y la API
     * responde 403 si el cliente es de otro equipo, porque el frontend no conoce el equipo de cada vendedor).
     */
    public boolean puedeGestionarCliente(Cliente cliente) {
        if (cliente == null || usuario() == null) {
            return false;
        }
        return gestionaEquipo() || esResponsable(cliente.vendedorId());
    }

    /**
     * Modificar algo que tiene un responsable (oportunidad, interacción): el propio responsable, o
     * GERENTE/ADMIN (la API confirma que el gerente sea del mismo equipo).
     */
    public boolean puedeGestionarDe(Integer responsableId) {
        return usuario() != null && (gestionaEquipo() || esResponsable(responsableId));
    }

    /**
     * Gestionar algo de un cliente del que solo se conoce su vendedor (contratos, tickets): el vendedor
     * asignado, o GERENTE/ADMIN (la API confirma que el gerente sea del mismo equipo).
     *
     * @param clienteVendedorId vendedor asignado al cliente; {@code null} si no tiene
     */
    public boolean puedeGestionarClienteDe(Integer clienteVendedorId) {
        return puedeGestionarDe(clienteVendedorId);
    }

    /** {@code true} si el usuario actual es el responsable indicado. */
    public boolean esResponsable(Integer responsableId) {
        Usuario actual = usuario();
        return actual != null && actual.id().equals(responsableId);
    }

    private boolean tieneRol(Rol rol) {
        Usuario actual = usuario();
        return actual != null && actual.rol() == rol;
    }
}
