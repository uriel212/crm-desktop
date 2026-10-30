package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;

/**
 * Meta mensual de un VENDEDOR con su avance ({@code MetaVentaResponse}).
 *
 * @param porcentajeAvance 71.43 = 71.43 %; puede pasar de 100
 */
public record MetaVenta(Integer id, Integer usuarioId, String usuarioNombre, int anio, int mes, BigDecimal montoObjetivo,
                        BigDecimal montoVendido, long numeroVentas, BigDecimal porcentajeAvance, BigDecimal faltante,
                        boolean cumplida) {
}
