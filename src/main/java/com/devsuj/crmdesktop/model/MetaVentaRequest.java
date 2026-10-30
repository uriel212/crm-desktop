package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/** Cuerpo de {@code POST/PUT /api/metas}. */
public record MetaVentaRequest(Integer usuarioId, Integer anio, Integer mes, BigDecimal montoObjetivo) {
}
