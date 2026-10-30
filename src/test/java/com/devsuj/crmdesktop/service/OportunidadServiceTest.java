package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.OportunidadApi;
import com.devsuj.crmdesktop.model.EtapaOportunidad;
import com.devsuj.crmdesktop.model.HistorialEtapa;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.model.OportunidadActualizarRequest;
import com.devsuj.crmdesktop.model.OportunidadRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.service.OportunidadService.EstadoPaso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OportunidadServiceTest {

    static class OportunidadApiFalsa implements OportunidadApi {
        final List<Object> llamadas = new ArrayList<>();

        @Override
        public Pagina<Oportunidad> buscar(EtapaOportunidad e, Integer u, Integer c, Boolean a, int p, int t) {
            return new Pagina<>(List.of(), p, t, 0, 0);
        }

        @Override
        public List<HistorialEtapa> historial(int id) {
            return List.of();
        }

        @Override
        public Oportunidad crear(OportunidadRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public Oportunidad actualizar(int id, OportunidadActualizarRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public Oportunidad cambiarEtapa(int id, EtapaOportunidad etapa, String comentario) {
            llamadas.add(etapa);
            return null;
        }
    }

    private final OportunidadApiFalsa api = new OportunidadApiFalsa();
    private final OportunidadService service = new OportunidadService(api);

    private static Oportunidad en(EtapaOportunidad etapa) {
        return new Oportunidad(1, 7, "Ana", 3, "Carla", "Laptops", BigDecimal.TEN, etapa, null, null, null);
    }

    private static HistorialEtapa paso(EtapaOportunidad anterior, EtapaOportunidad etapa, Long dias) {
        return new HistorialEtapa(1, anterior, etapa, null, null, dias, 3, "Carla", null, false);
    }

    @Test
    void elDiagramaMarcaLasEtapasRecorridasLaActualYLasPendientes() {
        List<HistorialEtapa> historial = List.of(paso(null, EtapaOportunidad.PROSPECTO, 7L),
                paso(EtapaOportunidad.PROSPECTO, EtapaOportunidad.CALIFICADA, 3L));

        Map<EtapaOportunidad, EstadoPaso> diagrama = OportunidadService.diagrama(EtapaOportunidad.CALIFICADA, historial);

        assertEquals(EstadoPaso.RECORRIDA, diagrama.get(EtapaOportunidad.PROSPECTO));
        assertEquals(EstadoPaso.ACTUAL, diagrama.get(EtapaOportunidad.CALIFICADA));
        assertEquals(EstadoPaso.PENDIENTE, diagrama.get(EtapaOportunidad.GANADA));
        assertEquals(10, OportunidadService.diasTotales(historial));
    }

    @Test
    void nuncaSeMueveAGanadaYParaPerderPideElMotivo() {
        ValidacionException ganada = assertThrows(ValidacionException.class,
                () -> service.cambiarEtapa(en(EtapaOportunidad.PROPUESTA), EtapaOportunidad.GANADA, null));
        ValidacionException perdida = assertThrows(ValidacionException.class,
                () -> service.cambiarEtapa(en(EtapaOportunidad.PROPUESTA), EtapaOportunidad.PERDIDA, " "));

        assertTrue(ganada.erroresPorCampo().containsKey(OportunidadService.ETAPA));
        assertTrue(perdida.erroresPorCampo().containsKey(OportunidadService.COMENTARIO));
        assertTrue(api.llamadas.isEmpty());
    }

    @Test
    void unaOportunidadCerradaNoSeEdita() {
        var datos = new OportunidadFormulario(null, "Nuevo título", "100", null, null);

        assertThrows(IllegalStateException.class, () -> service.guardar(en(EtapaOportunidad.PERDIDA), null, datos));
    }

    @Test
    void unNuevoIntentoQuedaLigadoALaPerdida() {
        var cliente = new com.devsuj.crmdesktop.model.Cliente(7, "Ana", null, "a@test.com", null, null,
                com.devsuj.crmdesktop.model.EstadoCliente.ACTIVO, 3, null, null, null);
        var datos = new OportunidadFormulario(cliente, " Reintento ", "1,500.00", null, null);

        service.guardar(null, en(EtapaOportunidad.PERDIDA), datos);

        OportunidadRequest enviada = (OportunidadRequest) api.llamadas.getFirst();
        assertEquals(1, enviada.oportunidadAnteriorId());
        assertEquals("Reintento", enviada.titulo());
        assertEquals(new BigDecimal("1500.00"), enviada.montoEstimado());
    }
}
