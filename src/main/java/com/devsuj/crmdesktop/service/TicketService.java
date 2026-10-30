package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.TicketApi;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoTicket;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.PrioridadTicket;
import com.devsuj.crmdesktop.model.Ticket;
import com.devsuj.crmdesktop.model.TicketActualizarRequest;
import com.devsuj.crmdesktop.model.TicketRequest;
import com.devsuj.crmdesktop.model.Usuario;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tickets de soporte: abrir, editar, asignar, cambiar de estado y crear seguimientos. Sin JavaFX.
 *
 * <p>Reglas del backend: ABIERTO ↔ EN_PROCESO → CERRADO (también ABIERTO → CERRADO); EN_PROCESO exige
 * responsable; un CERRADO no se edita ni se reabre: se crea un seguimiento ligado.</p>
 */
public class TicketService {

    public static final int TAMANO_PAGINA = 20;
    public static final String CLIENTE = "clienteId";
    public static final String ASUNTO = "asunto";
    public static final String DESCRIPCION = "descripcion";
    public static final String PRIORIDAD = "prioridad";

    private final TicketApi api;

    public TicketService(TicketApi api) {
        this.api = api;
    }

    public Pagina<Ticket> buscar(EstadoTicket estado, PrioridadTicket prioridad, Integer usuarioId, boolean sinAsignar,
                                 int pagina) {
        return api.buscar(estado, prioridad, null, usuarioId, sinAsignar ? Boolean.TRUE : null, pagina, TAMANO_PAGINA);
    }

    /** Estados a los que puede pasar (EN_PROCESO solo si tiene responsable). */
    public static List<EstadoTicket> destinosPermitidos(Ticket ticket) {
        return Arrays.stream(EstadoTicket.values())
                .filter(ticket.estado()::puedeCambiarA)
                .filter(e -> e != EstadoTicket.EN_PROCESO || ticket.usuarioId() != null)
                .toList();
    }

    private static Map<String, String> validar(String asunto, String descripcion, PrioridadTicket prioridad) {
        Map<String, String> errores = new LinkedHashMap<>();
        Reglas.revisar(errores, ASUNTO, () -> Reglas.obligatorio(asunto, "Describe el problema en pocas palabras.")
                .or(() -> Reglas.maximo(asunto, 150)));
        Reglas.revisar(errores, DESCRIPCION, () -> Reglas.maximo(descripcion, 5000));
        Reglas.revisar(errores, PRIORIDAD, () -> Reglas.obligatorio(prioridad, "Elige la prioridad."));
        return errores;
    }

    /**
     * Abre un ticket ({@code original == null}) o edita uno que no esté cerrado.
     *
     * @param responsable solo al abrir; {@code null} = sin asignar (un VENDEDOR solo se asigna a sí mismo)
     */
    public Ticket guardar(Ticket original, Cliente cliente, String asunto, String descripcion,
                          PrioridadTicket prioridad, Usuario responsable) {
        if (original != null && original.estado() == EstadoTicket.CERRADO) {
            throw new IllegalStateException("Un ticket cerrado no se edita: crea un seguimiento.");
        }
        Map<String, String> errores = validar(asunto, descripcion, prioridad);
        if (original == null && cliente == null) {
            errores.putIfAbsent(CLIENTE, "Elige el cliente.");
        }
        Reglas.exigir(errores);
        if (original != null) {
            return api.actualizar(original.id(), new TicketActualizarRequest(asunto.strip(), Reglas.limpiar(descripcion), prioridad));
        }
        return api.crear(new TicketRequest(cliente.id(), asunto.strip(), Reglas.limpiar(descripcion), prioridad,
                responsable == null ? null : responsable.id()));
    }

    public Ticket asignar(Ticket ticket, Usuario responsable) {
        if (ticket.estado() == EstadoTicket.CERRADO) {
            throw new IllegalStateException("Un ticket cerrado no se reasigna.");
        }
        return api.asignar(ticket.id(), responsable == null ? null : responsable.id());
    }

    public Ticket cambiarEstado(Ticket ticket, EstadoTicket destino) {
        if (!destinosPermitidos(ticket).contains(destino)) {
            throw new ValidacionException(Map.of("estado", destino == EstadoTicket.EN_PROCESO && ticket.usuarioId() == null
                    ? "Asigna un responsable antes de ponerlo en proceso."
                    : "Ese cambio de estado no está permitido."));
        }
        return api.cambiarEstado(ticket.id(), destino);
    }

    public Ticket seguimiento(Ticket cerrado, String asunto, String descripcion, PrioridadTicket prioridad) {
        if (cerrado.estado() != EstadoTicket.CERRADO) {
            throw new IllegalStateException("El seguimiento se crea sobre un ticket cerrado.");
        }
        Reglas.exigir(validar(asunto, descripcion, prioridad));
        return api.seguimiento(cerrado.id(), new TicketActualizarRequest(asunto.strip(), Reglas.limpiar(descripcion), prioridad));
    }
}
