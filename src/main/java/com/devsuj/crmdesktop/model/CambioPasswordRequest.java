package com.devsuj.crmdesktop.model;

/**
 * Cuerpo de {@code PATCH /api/usuarios/{id}/password}.
 *
 * @param passwordActual obligatoria cuando el usuario cambia SU propia contraseña
 */
public record CambioPasswordRequest(String passwordActual, String passwordNueva) {
}
