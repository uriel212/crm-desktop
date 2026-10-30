package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.EstadoTicket;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.PrioridadTicket;
import com.devsuj.crmdesktop.model.Ticket;
import com.devsuj.crmdesktop.model.TicketActualizarRequest;
import com.devsuj.crmdesktop.model.TicketRequest;

/** Operaciones de {@code /api/tickets}. Un ticket CERRADO no se reabre: se crea un seguimiento. */
public interface TicketApi {

    /** {@code GET /api/tickets?estado=&prioridad=&clienteId=&usuarioId=&sinAsignar=&page=&size=} */
    Pagina<Ticket> buscar(EstadoTicket estado, PrioridadTicket prioridad, Integer clienteId, Integer usuarioId,
                          Boolean sinAsignar, int pagina, int tamano);

    Ticket crear(TicketRequest datos);

    Ticket actualizar(int id, TicketActualizarRequest datos);

    /** Solo GERENTE y ADMIN. {@code null} = sin asignar. */
    Ticket asignar(int id, Integer usuarioId);

    Ticket cambiarEstado(int id, EstadoTicket estado);

    /** Ticket nuevo ABIERTO ligado a uno CERRADO (201). */
    Ticket seguimiento(int id, TicketActualizarRequest datos);
}
