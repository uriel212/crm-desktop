package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/**
 * Renglón del estado de cuenta de puntos. Los puntos siempre son positivos; el tipo dice si suman o restan.
 *
 * @param ventaId    solo en GANADO
 * @param productoId solo en CANJEADO (igual que {@code productoNombre} y {@code cantidad})
 */
public record MovimientoPuntos(Integer id, TipoMovimientoPuntos tipo, int puntos, String motivo, LocalDateTime fecha,
                               Integer ventaId, Integer productoId, String productoNombre, Integer cantidad,
                               Integer usuarioId, String usuarioNombre) {
}
