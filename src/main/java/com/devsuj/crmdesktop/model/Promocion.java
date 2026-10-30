package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Promoción ({@code PromocionResponse}). Las aplica solo el servidor al vender.
 *
 * @param campanaId con campaña, solo aplica mientras la campaña está ACTIVA
 * @param vigente   aplicaría hoy (activa, en fechas y con su campaña activa si tiene)
 */
public record Promocion(Integer id, Integer campanaId, String campanaNombre, String nombre, TipoPromocion tipo,
                        AlcancePromocion alcance, BigDecimal valor, BigDecimal montoMinimo, LocalDate fechaInicio,
                        LocalDate fechaFin, boolean activa, boolean vigente, List<ProductoDePromocion> productos) {

    public Promocion {
        productos = productos == null ? List.of() : List.copyOf(productos);
    }
}
