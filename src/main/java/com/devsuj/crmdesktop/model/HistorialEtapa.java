package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/**
 * Un paso de la línea de tiempo de una oportunidad ({@code HistorialEtapaResponse} de
 * {@code GET /api/oportunidades/{id}/historial}).
 *
 * @param etapaAnterior etapa de la que venía; {@code null} = creación
 * @param fechaFin      cuándo salió de la etapa; {@code null} si es la actual
 * @param diasEnEtapa   días completos en la etapa (hasta hoy si es la actual); {@code null} en el cierre
 * @param actual        es la etapa en la que está ahora
 */
public record HistorialEtapa(
        Integer id,
        EtapaOportunidad etapaAnterior,
        EtapaOportunidad etapa,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Long diasEnEtapa,
        Integer usuarioId,
        String usuarioNombre,
        String comentario,
        boolean actual) {
}
