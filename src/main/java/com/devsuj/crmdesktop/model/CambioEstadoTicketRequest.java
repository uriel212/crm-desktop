package com.devsuj.crmdesktop.model;

/** Cuerpo de {@code PATCH /api/tickets/{id}/estado}. */
public record CambioEstadoTicketRequest(EstadoTicket estado) {
}
