package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.InteraccionApi;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.EstadoInteraccion;
import com.devsuj.crmdesktop.model.Interaccion;
import com.devsuj.crmdesktop.model.InteraccionActualizarRequest;
import com.devsuj.crmdesktop.model.InteraccionRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.TipoInteraccion;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteraccionServiceTest {

    /** 21/10/2026 10:00 en Guatemala (16:00 UTC). */
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-21T16:00:00Z"), ZoneId.of("America/Guatemala"));
    private static final LocalDate HOY = LocalDate.of(2026, 10, 21);
    private static final Cliente ANA = new Cliente(7, "Ana", null, "a@test.com", null, null, EstadoCliente.ACTIVO,
            3, null, null, null);

    static class InteraccionApiFalsa implements InteraccionApi {
        final List<InteraccionRequest> creadas = new ArrayList<>();
        final List<EstadoInteraccion> cierres = new ArrayList<>();

        @Override
        public Pagina<Interaccion> buscar(Integer u, Integer c, TipoInteraccion t, EstadoInteraccion e,
                                          LocalDate d, LocalDate h, int p, int tam) {
            return new Pagina<>(List.of(), p, tam, 0, 0);
        }

        @Override
        public List<Interaccion> proximas(Integer usuarioId, int dias) {
            return List.of();
        }

        @Override
        public Interaccion crear(int clienteId, InteraccionRequest datos) {
            creadas.add(datos);
            return null;
        }

        @Override
        public Interaccion actualizar(int id, InteraccionActualizarRequest datos) {
            return null;
        }

        @Override
        public Interaccion cambiarEstado(int id, EstadoInteraccion estado, String resultado) {
            cierres.add(estado);
            return null;
        }
    }

    private final InteraccionApiFalsa api = new InteraccionApiFalsa();
    private final InteraccionService service = new InteraccionService(api, RELOJ);

    private static InteraccionFormulario datos(EstadoInteraccion estado, LocalDate fecha, String hora) {
        return new InteraccionFormulario(ANA, TipoInteraccion.LLAMADA, estado, fecha, hora, "Seguimiento", null);
    }

    @Test
    void unaActividadProgramadaDebeSerFuturaYConHora() {
        ValidacionException pasada = assertThrows(ValidacionException.class,
                () -> service.guardar(null, datos(EstadoInteraccion.PROGRAMADA, HOY, "09:00")));
        ValidacionException sinHora = assertThrows(ValidacionException.class,
                () -> service.guardar(null, datos(EstadoInteraccion.PROGRAMADA, HOY.plusDays(1), "")));

        assertTrue(pasada.erroresPorCampo().containsKey(InteraccionService.FECHA));
        assertTrue(sinHora.erroresPorCampo().containsKey(InteraccionService.HORA));
    }

    @Test
    void agendaConFechaYHora() {
        service.guardar(null, datos(EstadoInteraccion.PROGRAMADA, HOY.plusDays(1), "9:30"));

        assertEquals(LocalDateTime.of(2026, 10, 22, 9, 30), api.creadas.getFirst().fecha());
    }

    @Test
    void loRealizadoNoPuedeSerFuturoYSinFechaLaPoneLaApi() {
        assertThrows(ValidacionException.class,
                () -> service.guardar(null, datos(EstadoInteraccion.REALIZADA, HOY.plusDays(1), "09:00")));

        service.guardar(null, datos(EstadoInteraccion.REALIZADA, null, null));

        assertNull(api.creadas.getFirst().fecha());
    }

    @Test
    void soloSeCierraUnaActividadProgramada() {
        Interaccion realizada = new Interaccion(1, 7, "Ana", 3, "Carla", TipoInteraccion.NOTA,
                EstadoInteraccion.REALIZADA, null, "x", null, null, false);

        assertThrows(IllegalStateException.class,
                () -> service.cerrar(realizada, EstadoInteraccion.CANCELADA, null));
        assertTrue(api.cierres.isEmpty());
    }
}
