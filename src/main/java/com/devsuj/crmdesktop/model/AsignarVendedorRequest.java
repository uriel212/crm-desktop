package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code PATCH /api/clientes/{id}/vendedor}. {@code null} = el cliente queda sin responsable. */
public record AsignarVendedorRequest(Integer vendedorId) {
}
