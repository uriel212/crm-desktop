package com.devsuj.crmdesktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato de un cliente ({@code ContratoResponse}).
 *
 * @param contratoAnteriorId contrato del que este es renovación; {@code null} si no
 * @param diasParaVencer     solo en VIGENTE (negativo si ya pasó la fecha); {@code null} en otros estados
 * @param clienteVendedorId  vendedor asignado al cliente ({@code null} si no tiene): sirve para saber de
 *                           antemano quién puede gestionarlo
 */
public record Contrato(Integer id, Integer clienteId, String clienteNombre, Integer ventaId, Integer contratoAnteriorId,
                       String titulo, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal monto, EstadoContrato estado,
                       Long diasParaVencer, Integer clienteVendedorId) {
}
