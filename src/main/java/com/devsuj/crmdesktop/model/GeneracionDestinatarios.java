package com.devsuj.crmdesktop.model;

/** Respuesta de {@code POST /api/campanas/{id}/destinatarios/generar}. */
public record GeneracionDestinatarios(int agregados, long totalDestinatarios) {
}
