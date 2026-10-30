package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * KPIs del tablero ({@code GET /api/reportes/dashboard}), según el alcance del usuario.
 * {@code metaMes}, {@code vendidoMes} y {@code avanceMetaMes} son siempre del mes en curso; el resto, del periodo.
 */
public record Dashboard(LocalDate desde, LocalDate hasta, BigDecimal totalVendido, long numeroVentas,
                        BigDecimal ticketPromedio, BigDecimal metaMes, BigDecimal vendidoMes, BigDecimal avanceMetaMes,
                        long clientesActivos, long clientesProspecto, long clientesNuevos, long oportunidadesAbiertas,
                        BigDecimal montoPipeline, long oportunidadesGanadas, long oportunidadesPerdidas,
                        BigDecimal tasaConversion, long ticketsAbiertos, long productosStockBajo) {
}
