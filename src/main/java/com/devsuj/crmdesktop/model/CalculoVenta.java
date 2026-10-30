package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.util.List;

/** Cotización de {@code POST /api/ventas/calculo}: los mismos montos que tendría la venta, sin guardarla. */
public record CalculoVenta(List<DetalleVenta> detalles, BigDecimal subtotal, BigDecimal descuento, Integer promocionId,
                           String promocionNombre, BigDecimal total) {

    public CalculoVenta {
        detalles = detalles == null ? List.of() : List.copyOf(detalles);
    }
}
