package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.Cliente;

import java.time.LocalDate;

/**
 * Lo que el usuario escribió en el formulario de contrato (sin validar).
 *
 * @param cliente solo al crear (al editar no cambia)
 * @param ventaId venta de la que nace el contrato, como texto (opcional)
 */
public record ContratoFormulario(Cliente cliente, String ventaId, String titulo, LocalDate fechaInicio,
                                 LocalDate fechaFin, String monto) {
}
