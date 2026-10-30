package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/**
 * Interacción o actividad con un cliente ({@code InteraccionResponse}).
 *
 * @param usuarioId quien la registró o la tiene agendada
 * @param resultado lo que pasó (se escribe al marcarla REALIZADA)
 * @param vencida   PROGRAMADA cuya fecha ya pasó
 */
public record Interaccion(
        Integer id,
        Integer clienteId,
        String clienteNombre,
        Integer usuarioId,
        String usuarioNombre,
        TipoInteraccion tipo,
        EstadoInteraccion estado,
        LocalDateTime fecha,
        String asunto,
        String descripcion,
        String resultado,
        boolean vencida) {
}
