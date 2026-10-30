package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code PATCH /api/tickets/{id}/asignacion} ({@code null} = sin asignar). */
public record AsignarTicketRequest(Integer usuarioId) {
}
