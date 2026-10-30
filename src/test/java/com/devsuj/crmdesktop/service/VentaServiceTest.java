package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.VentaApi;
import com.devsuj.crmdesktop.model.CalculoVenta;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Venta;
import com.devsuj.crmdesktop.model.VentaRequest;
import com.devsuj.crmdesktop.model.VentaResumen;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** VentaService con una API falsa que guarda lo que se le envía. */
class VentaServiceTest {

    static class VentaApiFalsa implements VentaApi {
        final List<VentaRequest> enviadas = new ArrayList<>();

        @Override
        public Pagina<VentaResumen> buscar(Integer c, Integer u, LocalDate d, LocalDate h, int p, int t) {
            return new Pagina<>(List.of(), p, t, 0, 0);
        }

        @Override
        public Venta obtener(int id) {
            return null;
        }

        @Override
        public CalculoVenta calcular(VentaRequest datos) {
            enviadas.add(datos);
            return new CalculoVenta(List.of(), BigDecimal.TEN, BigDecimal.ZERO, null, null, BigDecimal.TEN);
        }

        @Override
        public Venta registrar(VentaRequest datos) {
            enviadas.add(datos);
            return new Venta(1, datos.clienteId(), "C", null, null, datos.oportunidadId(), null, BigDecimal.TEN,
                    BigDecimal.ZERO, null, null, BigDecimal.TEN, 0, List.of());
        }
    }

    private final VentaApiFalsa api = new VentaApiFalsa();
    private final VentaService service = new VentaService(api);

    private static Cliente cliente(EstadoCliente estado) {
        return new Cliente(7, "Ana", null, "a@test.com", null, null, estado, 3, null, null, null);
    }

    @Test
    void noCotizaUnCarritoVacio() {
        assertNull(service.cotizar(new CarritoVenta()));
        assertTrue(api.enviadas.isEmpty());
    }

    @Test
    void registraSoloProductoYCantidadSinPrecios() {
        CarritoVenta carrito = new CarritoVenta();
        carrito.agregar(CarritoVentaTest.producto(4, 10, true), "2");

        service.registrar(cliente(EstadoCliente.ACTIVO), null, null, carrito);

        VentaRequest enviada = api.enviadas.getFirst();
        assertEquals(7, enviada.clienteId());
        assertNull(enviada.oportunidadId());
        assertEquals(4, enviada.detalles().getFirst().productoId());
        assertEquals(2, enviada.detalles().getFirst().cantidad());
    }

    @Test
    void noVendeAUnClienteInactivoNiSinProductos() {
        ValidacionException error = assertThrows(ValidacionException.class,
                () -> service.registrar(cliente(EstadoCliente.INACTIVO), null, null, new CarritoVenta()));

        assertTrue(error.erroresPorCampo().containsKey(VentaService.CLIENTE));
        assertTrue(error.erroresPorCampo().containsKey(CarritoVenta.PRODUCTO));
        assertTrue(api.enviadas.isEmpty());
    }
}
