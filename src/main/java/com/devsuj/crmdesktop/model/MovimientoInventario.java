package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/**
 * Renglón del kardex ({@code MovimientoInventarioResponse}).
 *
 * @param stockResultante existencias después del movimiento
 * @param ventaId         venta que originó la salida (tipo VENTA); {@code null} si no
 */
public record MovimientoInventario(
        Integer id,
        Integer productoId,
        String productoNombre,
        TipoMovimientoInventario tipo,
        int cantidad,
        int stockResultante,
        String motivo,
        Integer usuarioId,
        String usuarioNombre,
        Integer ventaId,
        LocalDateTime fecha) {
}
