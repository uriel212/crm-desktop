package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/** Renglón de {@code GET /api/reportes/ventas/por-vendedor}. */
public record VentasPorVendedor(Integer usuarioId, String usuarioNombre, long numeroVentas, BigDecimal total,
                                BigDecimal ticketPromedio) {
}
