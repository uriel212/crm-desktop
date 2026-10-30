package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.AsignarTicketRequest;
import com.devsuj.crmdesktop.model.CambioEstadoTicketRequest;
import com.devsuj.crmdesktop.model.EstadoTicket;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.PrioridadTicket;
import com.devsuj.crmdesktop.model.Ticket;
import com.devsuj.crmdesktop.model.TicketActualizarRequest;
import com.devsuj.crmdesktop.model.TicketRequest;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@link TicketApi} que llama a la API REST. */
public class TicketApiHttp implements TicketApi {

    private static final String RUTA = "/tickets";
    private static final TypeReference<Pagina<Ticket>> PAGINA = new TypeReference<>() {};
    private static final TypeReference<Ticket> TICKET = new TypeReference<>() {};

    private final ApiHttp api;

    public TicketApiHttp(ApiHttp api) {
        this.api = api;
    }

    @Override
    public Pagina<Ticket> buscar(EstadoTicket estado, PrioridadTicket prioridad, Integer clienteId, Integer usuarioId,
                                 Boolean sinAsignar, int pagina, int tamano) {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("estado", estado);
        parametros.put("prioridad", prioridad);
        parametros.put("clienteId", clienteId);
        parametros.put("usuarioId", usuarioId);
        parametros.put("sinAsignar", sinAsignar);
        parametros.put("page", pagina);
        parametros.put("size", tamano);
        return api.get(RUTA, parametros, PAGINA);
    }

    @Override
    public Ticket crear(TicketRequest datos) {
        return api.post(RUTA, datos, TICKET);
    }

    @Override
    public Ticket actualizar(int id, TicketActualizarRequest datos) {
        return api.put(RUTA + "/" + id, datos, TICKET);
    }

    @Override
    public Ticket asignar(int id, Integer usuarioId) {
        return api.patch(RUTA + "/" + id + "/asignacion", new AsignarTicketRequest(usuarioId), TICKET);
    }

    @Override
    public Ticket cambiarEstado(int id, EstadoTicket estado) {
        return api.patch(RUTA + "/" + id + "/estado", new CambioEstadoTicketRequest(estado), TICKET);
    }

    @Override
    public Ticket seguimiento(int id, TicketActualizarRequest datos) {
        return api.post(RUTA + "/" + id + "/seguimiento", datos, TICKET);
    }
}
