package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code POST /api/tickets}. {@code usuarioId} null = sin asignar. */
public record TicketRequest(Integer clienteId, String asunto, String descripcion, PrioridadTicket prioridad, Integer usuarioId) {
}
