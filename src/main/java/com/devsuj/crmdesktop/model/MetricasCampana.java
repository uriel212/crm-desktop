package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/**
 * Efectividad de una campaña ({@code GET /api/campanas/{id}/metricas}). Tasas como 71.43.
 *
 * @param abiertos          ABIERTO + RESPONDIO
 * @param costoPorRespuesta {@code null} si nadie respondió
 */
public record MetricasCampana(Integer campanaId, String campanaNombre, EstadoCampana estado, long destinatarios,
                              long abiertos, long respondieron, BigDecimal tasaApertura, BigDecimal tasaRespuesta,
                              BigDecimal presupuesto, BigDecimal costoPorRespuesta) {
}
