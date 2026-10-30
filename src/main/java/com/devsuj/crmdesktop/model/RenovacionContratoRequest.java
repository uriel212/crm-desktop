package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo de {@code POST /api/contratos/{id}/renovacion} (título opcional). */
public record RenovacionContratoRequest(String titulo, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal monto) {
}
