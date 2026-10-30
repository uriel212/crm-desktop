package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.FidelizacionApi;
import com.devsuj.crmdesktop.model.CanjeRequest;
import com.devsuj.crmdesktop.model.CanjeResponse;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.MovimientoPuntos;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.SaldoPuntos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FidelizacionServiceTest {

    static class FidelizacionApiFalsa implements FidelizacionApi {
        final List<CanjeRequest> canjes = new ArrayList<>();

        @Override
        public SaldoPuntos saldo(int clienteId) {
            return new SaldoPuntos(clienteId, "Ana", 100, 20, 80);
        }

        @Override
        public Pagina<MovimientoPuntos> movimientos(int clienteId, int pagina, int tamano) {
            return new Pagina<>(List.of(), pagina, tamano, 0, 0);
        }

        @Override
        public CanjeResponse canjear(int clienteId, CanjeRequest datos) {
            canjes.add(datos);
            return new CanjeResponse(null, 0);
        }
    }

    private final FidelizacionApiFalsa api = new FidelizacionApiFalsa();
    private final FidelizacionService service = new FidelizacionService(api);
    private static final Cliente ANA = new Cliente(7, "Ana", null, "a@test.com", null, null, EstadoCliente.ACTIVO,
            3, null, null, null);

    private static Producto conPuntos(Integer puntos) {
        return new Producto(9, 1, "Accesorios", "Mouse", null, new BigDecimal("199.00"), puntos, 5, 1, true, false,
                puntos != null);
    }

    @Test
    void calculaLosPuntosNecesariosParaMostrar() {
        assertEquals(600L, FidelizacionService.puntosNecesarios(conPuntos(200), "3").orElseThrow());
        assertTrue(FidelizacionService.puntosNecesarios(conPuntos(200), "x").isEmpty());
        assertTrue(FidelizacionService.puntosNecesarios(conPuntos(null), "1").isEmpty());
    }

    @Test
    void canjeaProductoYCantidad() {
        service.canjear(ANA, conPuntos(200), "2");

        assertEquals(new CanjeRequest(9, 2), api.canjes.getFirst());
    }

    @Test
    void noCanjeaProductosSinPreciosEnPuntosNiMasDe100() {
        assertThrows(ValidacionException.class, () -> service.canjear(ANA, conPuntos(null), "1"));
        ValidacionException demasiado = assertThrows(ValidacionException.class,
                () -> service.canjear(ANA, conPuntos(10), "101"));
        assertTrue(demasiado.erroresPorCampo().containsKey(FidelizacionService.CANTIDAD));
        assertTrue(api.canjes.isEmpty());
    }

    @Test
    void unClienteInactivoNoCanjea() {
        Cliente inactivo = new Cliente(8, "Beto", null, "b@test.com", null, null, EstadoCliente.INACTIVO,
                3, null, null, null);

        assertThrows(IllegalStateException.class, () -> service.canjear(inactivo, conPuntos(10), "1"));
    }
}
