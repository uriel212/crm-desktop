package com.devsuj.crmdesktop.model;

import java.time.LocalDate;

/** Cuerpo de {@code PATCH /api/contratos/{id}/extension}: nueva fecha de fin, posterior a la actual. */
public record ExtensionContratoRequest(LocalDate fechaFin) {
}
