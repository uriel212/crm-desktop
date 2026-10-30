package com.devsuj.crmdesktop.model;

/**
 * Cuerpo para crear ({@code POST /api/clientes}) o actualizar ({@code PUT /api/clientes/{id}})
 * un cliente ({@code ClienteRequest}, plan del backend 1.11). El estado NO va aquí: se cambia
 * con {@code PATCH /api/clientes/{id}/estado}.
 */
public record ClienteRequest(
        String nombre,
        String empresa,
        String email,
        String telefono,
        String direccion,
        Integer vendedorId) {
}
