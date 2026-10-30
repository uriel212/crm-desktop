package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/**
 * Rendimiento de un VENDEDOR en el mes ({@code GET /api/reportes/rendimiento-vendedores}).
 *
 * @param metaObjetivo {@code null} si no tiene meta ese mes
 * @param avanceMeta   porcentaje; {@code null} si no tiene meta
 */
public record RendimientoVendedor(Integer usuarioId, String usuarioNombre, long numeroVentas, BigDecimal totalVendido,
                                  BigDecimal metaObjetivo, BigDecimal avanceMeta, long oportunidadesGanadas,
                                  long oportunidadesPerdidas, BigDecimal tasaConversion) {
}
