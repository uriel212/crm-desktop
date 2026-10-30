package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo de {@code POST/PUT /api/contratos} (nace BORRADOR; al editar el cliente no cambia). */
public record ContratoRequest(Integer clienteId, Integer ventaId, String titulo, LocalDate fechaInicio, LocalDate fechaFin,
                              BigDecimal monto) {
}
