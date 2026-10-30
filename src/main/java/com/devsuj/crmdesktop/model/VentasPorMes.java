package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/** Un mes de {@code GET /api/reportes/ventas/por-mes} (0 en meses sin ventas). */
public record VentasPorMes(int anio, int mes, long numeroVentas, BigDecimal total) {
}
