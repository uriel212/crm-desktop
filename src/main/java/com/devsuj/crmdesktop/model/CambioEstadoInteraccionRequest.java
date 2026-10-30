package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code PATCH /api/interacciones/{id}/estado}: REALIZADA (con resultado) o CANCELADA. */
public record CambioEstadoInteraccionRequest(EstadoInteraccion estado, String resultado) {
}
