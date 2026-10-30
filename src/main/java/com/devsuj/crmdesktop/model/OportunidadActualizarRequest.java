package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo de {@code PUT /api/oportunidades/{id}} (solo oportunidades abiertas). */
public record OportunidadActualizarRequest(String titulo, BigDecimal montoEstimado, LocalDate fechaCierreEstimada) {
}
