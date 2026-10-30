package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code POST /api/clientes/{clienteId}/puntos/canjes}. */
public record CanjeRequest(Integer productoId, Integer cantidad) {
}
