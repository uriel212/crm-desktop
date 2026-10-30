package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Venta completa ({@code GET /api/ventas/{id}} y respuesta de {@code POST /api/ventas}).
 *
 * @param descuento     descuento sobre el total (promoción de alcance VENTA)
 * @param puntosGanados puntos de fidelización que ganó el cliente (1 por cada Q100)
 */
public record Venta(Integer id, Integer clienteId, String clienteNombre, Integer usuarioId, String usuarioNombre,
                    Integer oportunidadId, LocalDateTime fecha, BigDecimal subtotal, BigDecimal descuento,
                    Integer promocionId, String promocionNombre, BigDecimal total, Integer puntosGanados,
                    List<DetalleVenta> detalles) {

    public Venta {
        detalles = detalles == null ? List.of() : List.copyOf(detalles);
    }
}
