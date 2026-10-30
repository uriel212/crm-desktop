package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.ContratoApi;
import com.devsuj.crmdesktop.client.TicketApi;
import com.devsuj.crmdesktop.model.Contrato;
import com.devsuj.crmdesktop.model.ContratoRequest;
import com.devsuj.crmdesktop.model.EstadoContrato;
import com.devsuj.crmdesktop.model.EstadoTicket;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.PrioridadTicket;
import com.devsuj.crmdesktop.model.RenovacionContratoRequest;
import com.devsuj.crmdesktop.model.Ticket;
import com.devsuj.crmdesktop.model.TicketActualizarRequest;
import com.devsuj.crmdesktop.model.TicketRequest;
import com.devsuj.crmdesktop.model.VencimientoLote;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contratos y tickets con APIs falsas. */
class PostventaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 22);
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-22T16:00:00Z"), ZoneId.of("America/Guatemala"));

    static class ContratoApiFalsa implements ContratoApi {
        final List<Object> llamadas = new ArrayList<>();

        @Override
        public Pagina<Contrato> buscar(Integer c, EstadoContrato e, Integer d, int p, int t) {
            llamadas.add(d);
            return new Pagina<>(List.of(), p, t, 0, 0);
        }

        @Override
        public Contrato crear(ContratoRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public Contrato actualizar(int id, ContratoRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public Contrato cambiarEstado(int id, EstadoContrato estado) {
            llamadas.add(estado);
            return null;
        }

        @Override
        public Contrato extender(int id, LocalDate fin) {
            llamadas.add(fin);
            return null;
        }

        @Override
        public Contrato renovar(int id, RenovacionContratoRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public VencimientoLote marcarVencidos() {
            return new VencimientoLote(0, List.of());
        }
    }

    static class TicketApiFalsa implements TicketApi {
        final List<Object> llamadas = new ArrayList<>();

        @Override
        public Pagina<Ticket> buscar(EstadoTicket e, PrioridadTicket p, Integer c, Integer u, Boolean s, int pag, int t) {
            llamadas.add(s);
            return new Pagina<>(List.of(), pag, t, 0, 0);
        }

        @Override
        public Ticket crear(TicketRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public Ticket actualizar(int id, TicketActualizarRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public Ticket asignar(int id, Integer usuarioId) {
            return null;
        }

        @Override
        public Ticket cambiarEstado(int id, EstadoTicket estado) {
            llamadas.add(estado);
            return null;
        }

        @Override
        public Ticket seguimiento(int id, TicketActualizarRequest datos) {
            llamadas.add(datos);
            return null;
        }
    }

    private final ContratoApiFalsa contratos = new ContratoApiFalsa();
    private final ContratoService contratoService = new ContratoService(contratos, RELOJ);
    private final TicketApiFalsa tickets = new TicketApiFalsa();
    private final TicketService ticketService = new TicketService(tickets);

    private static Contrato contrato(EstadoContrato estado, LocalDate fin) {
        return new Contrato(1, 7, "Ana", null, null, "Soporte", HOY.minusYears(1), fin, BigDecimal.TEN, estado, null, 3);
    }

    private static Ticket ticket(EstadoTicket estado, Integer responsable) {
        return new Ticket(1, 7, "Ana", responsable, null, "Falla", null, PrioridadTicket.MEDIA, estado, null, null, null, 3);
    }

    @Test
    void unContratoVigenteSoloSeMarcaVencidoSiYaPasoSuFecha() {
        assertEquals(List.of(EstadoContrato.CANCELADO),
                contratoService.destinosPermitidos(contrato(EstadoContrato.VIGENTE, HOY.plusDays(5))));
        assertTrue(contratoService.destinosPermitidos(contrato(EstadoContrato.VIGENTE, HOY.minusDays(1)))
                .contains(EstadoContrato.VENCIDO));
        assertTrue(contratoService.destinosPermitidos(contrato(EstadoContrato.CANCELADO, HOY)).isEmpty());
    }

    @Test
    void soloSeEditaEnBorradorYSoloSeExtiendeHaciaAdelante() {
        var datos = new ContratoFormulario(null, "", "Nuevo", HOY, HOY.plusDays(1), "100");
        Contrato vigente = contrato(EstadoContrato.VIGENTE, HOY.plusDays(10));

        assertThrows(IllegalStateException.class, () -> contratoService.guardar(vigente, datos));
        assertThrows(ValidacionException.class, () -> contratoService.extender(vigente, HOY.plusDays(10)));
        contratoService.extender(vigente, HOY.plusDays(40));

        assertEquals(List.of(HOY.plusDays(40)), contratos.llamadas);
    }

    @Test
    void elFiltroPorVencerUsa30Dias() {
        contratoService.buscar(null, true, 0);

        assertEquals(30, contratos.llamadas.getFirst());
    }

    @Test
    void soloSeRenuevaUnVigenteOVencido() {
        assertThrows(IllegalStateException.class, () -> contratoService.renovar(
                contrato(EstadoContrato.BORRADOR, HOY), null, HOY, HOY.plusYears(1), "100"));
        contratoService.renovar(contrato(EstadoContrato.VENCIDO, HOY.minusDays(1)), " ", HOY, HOY.plusYears(1), "1,500");

        RenovacionContratoRequest enviada = (RenovacionContratoRequest) contratos.llamadas.getFirst();
        assertEquals(null, enviada.titulo());
        assertEquals(new BigDecimal("1500"), enviada.monto());
    }

    @Test
    void sinResponsableUnTicketNoPasaAEnProceso() {
        assertEquals(List.of(EstadoTicket.CERRADO), TicketService.destinosPermitidos(ticket(EstadoTicket.ABIERTO, null)));
        assertThrows(ValidacionException.class,
                () -> ticketService.cambiarEstado(ticket(EstadoTicket.ABIERTO, null), EstadoTicket.EN_PROCESO));
        ticketService.cambiarEstado(ticket(EstadoTicket.ABIERTO, 3), EstadoTicket.EN_PROCESO);

        assertEquals(List.of(EstadoTicket.EN_PROCESO), tickets.llamadas);
    }

    @Test
    void unTicketCerradoNoSeEditaPeroAdmiteSeguimiento() {
        Ticket cerrado = ticket(EstadoTicket.CERRADO, 3);

        assertThrows(IllegalStateException.class,
                () -> ticketService.guardar(cerrado, null, "x", null, PrioridadTicket.BAJA, null));
        assertThrows(IllegalStateException.class,
                () -> ticketService.seguimiento(ticket(EstadoTicket.ABIERTO, 3), "x", null, PrioridadTicket.BAJA));
        ticketService.seguimiento(cerrado, "Sigue fallando", null, PrioridadTicket.ALTA);

        assertEquals(PrioridadTicket.ALTA, ((TicketActualizarRequest) tickets.llamadas.getFirst()).prioridad());
    }
}
