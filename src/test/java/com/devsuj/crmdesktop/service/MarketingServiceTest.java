package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.CampanaApi;
import com.devsuj.crmdesktop.client.PromocionApi;
import com.devsuj.crmdesktop.model.AlcancePromocion;
import com.devsuj.crmdesktop.model.CanalCampana;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.CampanaRequest;
import com.devsuj.crmdesktop.model.Destinatario;
import com.devsuj.crmdesktop.model.EstadoCampana;
import com.devsuj.crmdesktop.model.GeneracionDestinatarios;
import com.devsuj.crmdesktop.model.MetricasCampana;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Promocion;
import com.devsuj.crmdesktop.model.PromocionRequest;
import com.devsuj.crmdesktop.model.ResultadoCampana;
import com.devsuj.crmdesktop.model.SegmentoCampana;
import com.devsuj.crmdesktop.model.TipoPromocion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Campañas y promociones con APIs falsas que guardan lo que se les envía. */
class MarketingServiceTest {

    static class CampanaApiFalsa implements CampanaApi {
        final List<Object> llamadas = new ArrayList<>();

        @Override
        public Pagina<Campana> buscar(EstadoCampana e, CanalCampana c, String q, int p, int t) {
            return new Pagina<>(List.of(campana(EstadoCampana.ACTIVA), campana(EstadoCampana.CANCELADA)), p, t, 2, 1);
        }

        @Override
        public Campana crear(CampanaRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public Campana actualizar(int id, CampanaRequest datos) {
            llamadas.add(datos);
            return null;
        }

        @Override
        public Campana cambiarEstado(int id, EstadoCampana estado) {
            llamadas.add(estado);
            return null;
        }

        @Override
        public Pagina<Destinatario> destinatarios(int id, ResultadoCampana r, int p, int t) {
            return new Pagina<>(List.of(), p, t, 0, 0);
        }

        @Override
        public GeneracionDestinatarios generarDestinatarios(int id) {
            llamadas.add("generar");
            return new GeneracionDestinatarios(0, 0);
        }

        @Override
        public Destinatario agregarDestinatario(int id, int clienteId) {
            return null;
        }

        @Override
        public void quitarDestinatario(int id, int clienteId) {
            llamadas.add("quitar");
        }

        @Override
        public Destinatario cambiarResultado(int id, int clienteId, ResultadoCampana resultado) {
            llamadas.add(resultado);
            return null;
        }

        @Override
        public MetricasCampana metricas(int id) {
            return null;
        }
    }

    static class PromocionApiFalsa implements PromocionApi {
        final List<PromocionRequest> enviadas = new ArrayList<>();

        @Override
        public Pagina<Promocion> buscar(Boolean a, AlcancePromocion al, Integer c, Integer p, int pag, int t) {
            return new Pagina<>(List.of(), pag, t, 0, 0);
        }

        @Override
        public List<Promocion> vigentes() {
            return List.of();
        }

        @Override
        public Promocion crear(PromocionRequest datos) {
            enviadas.add(datos);
            return null;
        }

        @Override
        public Promocion actualizar(int id, PromocionRequest datos) {
            enviadas.add(datos);
            return null;
        }

        @Override
        public Promocion cambiarEstado(int id, boolean activa) {
            return null;
        }

        @Override
        public void eliminar(int id) {
        }
    }

    private static final LocalDate HOY = LocalDate.of(2026, 10, 22);

    static Campana campana(EstadoCampana estado) {
        return new Campana(estado.ordinal() + 1, "Campaña " + estado, CanalCampana.CORREO, "Asunto", "Mensaje",
                SegmentoCampana.TODOS, HOY, HOY.plusDays(30), BigDecimal.TEN, estado, 0);
    }

    private final CampanaApiFalsa campanas = new CampanaApiFalsa();
    private final CampanaService campanaService = new CampanaService(campanas);
    private final PromocionApiFalsa promociones = new PromocionApiFalsa();
    private final PromocionService promocionService = new PromocionService(promociones);

    @Test
    void validaLasFechasYElPresupuestoDeUnaCampana() {
        var datos = new CampanaFormulario("QA", CanalCampana.SMS, "Asunto", "Mensaje", SegmentoCampana.ACTIVOS,
                HOY, HOY.minusDays(1), "-5");

        ValidacionException error = assertThrows(ValidacionException.class, () -> campanaService.guardar(null, datos));

        assertTrue(error.erroresPorCampo().containsKey(CampanaService.FECHA_FIN));
        assertTrue(error.erroresPorCampo().containsKey(CampanaService.PRESUPUESTO));
        assertTrue(campanas.llamadas.isEmpty());
    }

    @Test
    void unaCampanaCerradaNoSeEditaNiGeneraDestinatarios() {
        Campana cancelada = campana(EstadoCampana.CANCELADA);
        var datos = new CampanaFormulario("QA", CanalCampana.SMS, "A", "M", SegmentoCampana.TODOS, HOY, HOY, "0");

        assertThrows(IllegalStateException.class, () -> campanaService.guardar(cancelada, datos));
        assertThrows(IllegalStateException.class, () -> campanaService.generarDestinatarios(cancelada));
        assertEquals(1, campanaService.abiertas().size());
    }

    @Test
    void elResultadoSoloAvanzaYConLaCampanaActiva() {
        Destinatario abierto = new Destinatario(1, 7, "Ana", "a@test.com", 3, "Carla", ResultadoCampana.ABIERTO);

        assertThrows(IllegalStateException.class,
                () -> campanaService.avanzarResultado(campana(EstadoCampana.ACTIVA), abierto, ResultadoCampana.ENVIADO));
        assertThrows(IllegalStateException.class,
                () -> campanaService.avanzarResultado(campana(EstadoCampana.PLANEADA), abierto, ResultadoCampana.RESPONDIO));
        campanaService.avanzarResultado(campana(EstadoCampana.ACTIVA), abierto, ResultadoCampana.RESPONDIO);

        assertEquals(List.of(ResultadoCampana.RESPONDIO), campanas.llamadas);
    }

    @Test
    void soloSeQuitanDestinatariosConLaCampanaPlaneada() {
        Destinatario d = new Destinatario(1, 7, "Ana", null, null, null, ResultadoCampana.ENVIADO);

        assertThrows(IllegalStateException.class, () -> campanaService.quitarDestinatario(campana(EstadoCampana.ACTIVA), d));
        campanaService.quitarDestinatario(campana(EstadoCampana.PLANEADA), d);

        assertEquals(List.of("quitar"), campanas.llamadas);
    }

    @Test
    void unPorcentajeNoPasaDe100YPorProductoPideProductos() {
        var datos = new PromocionFormulario(null, "QA", TipoPromocion.PORCENTAJE, AlcancePromocion.PRODUCTO,
                "150", null, HOY, HOY, List.of());

        ValidacionException error = assertThrows(ValidacionException.class, () -> promocionService.guardar(null, datos));

        assertTrue(error.erroresPorCampo().containsKey(PromocionService.VALOR));
        assertTrue(error.erroresPorCampo().containsKey(PromocionService.PRODUCTOS));
    }

    @Test
    void sobreElTotalNoEnviaProductosYSiElMontoMinimo() {
        var producto = CarritoVentaTest.producto(4, 1, true);
        var datos = new PromocionFormulario(null, " Compra grande ", TipoPromocion.MONTO_FIJO, AlcancePromocion.VENTA,
                "500", "10,000", HOY, HOY.plusDays(10), List.of(producto));

        promocionService.guardar(null, datos);

        PromocionRequest enviada = promociones.enviadas.getFirst();
        assertEquals("Compra grande", enviada.nombre());
        assertTrue(enviada.productoIds().isEmpty());
        assertEquals(new BigDecimal("10000"), enviada.montoMinimo());
        assertNull(enviada.campanaId());
    }
}
