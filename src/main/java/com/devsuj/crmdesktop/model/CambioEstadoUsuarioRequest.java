package com.devsuj.crmdesktop.model;

/**
 * Cuerpo de {@code PATCH /api/usuarios/{id}/estado}.
 *
 * @param reasignarClientes confirma que, al desactivar, los clientes del vendedor pasen a su gerente
 */
public record CambioEstadoUsuarioRequest(Boolean activo, Boolean reasignarClientes) {
}
