package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code POST /api/productos/{id}/movimientos}: ajuste manual ENTRADA o SALIDA. */
public record MovimientoInventarioRequest(TipoMovimientoInventario tipo, Integer cantidad, String motivo) {
}
