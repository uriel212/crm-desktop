package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code PUT /api/tickets/{id}} y de {@code POST /api/tickets/{id}/seguimiento}. */
public record TicketActualizarRequest(String asunto, String descripcion, PrioridadTicket prioridad) {
}
