package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Cuerpo de {@code POST/PUT /api/promociones}.
 *
 * @param productoIds solo para alcance PRODUCTO; vacío para VENTA
 * @param montoMinimo solo para alcance VENTA (opcional)
 */
public record PromocionRequest(Integer campanaId, String nombre, TipoPromocion tipo, AlcancePromocion alcance,
                               BigDecimal valor, BigDecimal montoMinimo, LocalDate fechaInicio, LocalDate fechaFin,
                               List<Integer> productoIds) {
}
