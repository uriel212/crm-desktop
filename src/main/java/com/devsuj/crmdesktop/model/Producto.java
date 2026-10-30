package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/**
 * Producto del catálogo ({@code ProductoResponse}). El stock solo cambia con movimientos de inventario.
 *
 * @param precio       en quetzales
 * @param precioPuntos precio en puntos de fidelización; {@code null} = no canjeable
 * @param stockBajo    {@code stock <= stockMinimo}: hay que resurtir
 */
public record Producto(
        Integer id,
        Integer categoriaId,
        String categoriaNombre,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer precioPuntos,
        int stock,
        int stockMinimo,
        boolean activo,
        boolean stockBajo,
        boolean canjeable) {
}
