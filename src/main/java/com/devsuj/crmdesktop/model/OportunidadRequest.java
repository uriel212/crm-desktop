package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cuerpo de {@code POST /api/oportunidades} (empieza en PROSPECTO).
 *
 * @param usuarioId responsable (solo GERENTE/ADMIN; {@code null} = quien la registra)
 */
public record OportunidadRequest(
        Integer clienteId,
        String titulo,
        BigDecimal montoEstimado,
        LocalDate fechaCierreEstimada,
        Integer usuarioId,
        Integer oportunidadAnteriorId) {
}
