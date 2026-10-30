package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Oportunidad del pipeline tal como la devuelve la API ({@code OportunidadResponse}).
 *
 * @param usuarioId             responsable de la oportunidad
 * @param oportunidadAnteriorId oportunidad PERDIDA de la que esta es un nuevo intento; {@code null} si no
 */
public record Oportunidad(
        Integer id,
        Integer clienteId,
        String clienteNombre,
        Integer usuarioId,
        String usuarioNombre,
        String titulo,
        BigDecimal montoEstimado,
        EtapaOportunidad etapa,
        LocalDate fechaCierreEstimada,
        LocalDateTime creadoEn,
        Integer oportunidadAnteriorId) {
}
