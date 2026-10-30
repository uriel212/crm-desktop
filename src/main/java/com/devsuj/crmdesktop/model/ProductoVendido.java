package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/** Renglón de {@code GET /api/reportes/productos/mas-vendidos}. */
public record ProductoVendido(Integer productoId, String productoNombre, long unidades, BigDecimal total) {
}
