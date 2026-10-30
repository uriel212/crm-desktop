package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/** Cuerpo de {@code PUT /api/interacciones/{id}}: corrige o reprograma (el estado va por PATCH). */
public record InteraccionActualizarRequest(TipoInteraccion tipo, LocalDateTime fecha, String asunto,
                                           String descripcion) {
}
