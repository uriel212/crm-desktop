package com.devsuj.crmdesktop.model;

import java.time.LocalDateTime;

/**
 * Ticket de soporte ({@code TicketResponse}).
 *
 * @param usuarioId           responsable; {@code null} = sin asignar
 * @param ticketRelacionadoId ticket CERRADO del que este es seguimiento
 * @param clienteVendedorId  vendedor asignado al cliente ({@code null} si no tiene): sirve para saber de
 *                           antemano quién puede gestionarlo
 */
public record Ticket(Integer id, Integer clienteId, String clienteNombre, Integer usuarioId, String usuarioNombre,
                     String asunto, String descripcion, PrioridadTicket prioridad, EstadoTicket estado,
                     LocalDateTime creadoEn, LocalDateTime cerradoEn, Integer ticketRelacionadoId,
                     Integer clienteVendedorId) {
}
