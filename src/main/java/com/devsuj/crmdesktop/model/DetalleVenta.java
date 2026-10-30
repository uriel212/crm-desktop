package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/**
 * Renglón de una venta o de una cotización. Los precios y descuentos los calcula la API.
 *
 * @param precioUnitario precio del producto al momento de la venta (queda congelado)
 * @param subtotal       cantidad × precioUnitario − descuento
 */
public record DetalleVenta(Integer productoId, String productoNombre, int cantidad, BigDecimal precioUnitario,
                           BigDecimal descuento, BigDecimal subtotal, Integer promocionId, String promocionNombre) {
}
