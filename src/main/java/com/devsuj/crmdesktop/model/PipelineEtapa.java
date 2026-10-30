package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/** Una etapa de {@code GET /api/reportes/oportunidades/pipeline} (las 6, en orden). */
public record PipelineEtapa(EtapaOportunidad etapa, long cantidad, BigDecimal montoEstimado) {
}
