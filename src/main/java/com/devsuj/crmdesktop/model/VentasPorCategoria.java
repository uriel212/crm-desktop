package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/** Renglón de {@code GET /api/reportes/ventas/por-categoria}. */
public record VentasPorCategoria(Integer categoriaId, String categoriaNombre, long unidades, BigDecimal total) {
}
