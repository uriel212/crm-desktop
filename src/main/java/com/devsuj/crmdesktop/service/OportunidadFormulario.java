package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Usuario;

import java.time.LocalDate;

/**
 * Lo que el usuario escribió en el formulario de oportunidad (sin validar).
 *
 * @param cliente     solo al crear (al editar no cambia)
 * @param monto       texto del campo de monto, por ejemplo "25,000.00"
 * @param responsable solo GERENTE/ADMIN al crear; {@code null} = quien la registra
 */
public record OportunidadFormulario(Cliente cliente, String titulo, String monto, LocalDate fechaCierreEstimada,
                                    Usuario responsable) {
}
