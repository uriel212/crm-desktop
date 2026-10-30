package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.model.UsuarioActualizarRequest;
import com.devsuj.crmdesktop.model.UsuarioCrearRequest;

/** Operaciones de {@code /api/usuarios} (listar: GERENTE y ADMIN; modificar: solo ADMIN). */
public interface UsuarioApi {

    /** {@code GET /api/usuarios?rol=&activo=&q=&page=&size=} */
    Pagina<Usuario> buscar(String texto, Rol rol, Boolean activo, int pagina, int tamano);

    /** {@code POST /api/usuarios} */
    Usuario crear(UsuarioCrearRequest datos);

    /** {@code PUT /api/usuarios/{id}} */
    Usuario actualizar(int id, UsuarioActualizarRequest datos);

    /**
     * {@code PATCH /api/usuarios/{id}/estado}. Si el vendedor tiene clientes y no se confirma la
     * reasignación, la API responde 409 con {@code clientesAsignados}.
     */
    Usuario cambiarEstado(int id, boolean activo, boolean reasignarClientes);

    /** {@code PATCH /api/usuarios/{id}/password} (204 sin cuerpo) */
    void cambiarPassword(int id, String passwordActual, String passwordNueva);
}
