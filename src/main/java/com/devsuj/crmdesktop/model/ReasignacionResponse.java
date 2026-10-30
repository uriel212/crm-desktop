package com.devsuj.crmdesktop.model;

/** Respuesta de {@code POST /api/clientes/reasignaciones}. */
public record ReasignacionResponse(int reasignados, Integer vendedorId, String vendedorNombre) {
}
