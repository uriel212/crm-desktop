package com.devsuj.crmdesktop.model;

/** Tickets pendientes (no cerrados) de una prioridad, en el reporte de soporte. */
public record TicketsPorPrioridad(PrioridadTicket prioridad, long cantidad) {
}
