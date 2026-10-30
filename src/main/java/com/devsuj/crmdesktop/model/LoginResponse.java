package com.devsuj.crmdesktop.model;

import java.time.Instant;

/**
 * Respuesta de {@code POST /api/auth/login}.
 *
 * @param token    JWT que se envía en cada petición como {@code Authorization: Bearer <token>}
 * @param tipo     siempre "Bearer"
 * @param expiraEn momento (UTC) en que el token deja de servir
 */
public record LoginResponse(String token, String tipo, Instant expiraEn, Usuario usuario) {
}
