package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/**
 * Cuerpo de {@code POST/PUT /api/productos}.
 *
 * @param stockInicial solo al crear: existencias iniciales (quedan como ENTRADA en el kardex)
 */
public record ProductoRequest(
        Integer categoriaId,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer precioPuntos,
        Integer stockMinimo,
        Integer stockInicial) {
}
