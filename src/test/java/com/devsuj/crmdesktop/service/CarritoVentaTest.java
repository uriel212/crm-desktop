package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.DetalleVentaRequest;
import com.devsuj.crmdesktop.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarritoVentaTest {

    static Producto producto(int id, int stock, boolean activo) {
        return new Producto(id, 1, "Laptops", "Producto " + id, null, new BigDecimal("100.00"), null,
                stock, 1, activo, false, false);
    }

    @Test
    void sumaLaCantidadSiElProductoSeRepite() {
        CarritoVenta carrito = new CarritoVenta();
        Producto laptop = producto(1, 10, true);

        carrito.agregar(laptop, "2");
        carrito.agregar(laptop, "3");

        assertEquals(List.of(new DetalleVentaRequest(1, 5)), carrito.aDetalles());
    }

    @Test
    void noPermiteMasQueLasExistencias() {
        CarritoVenta carrito = new CarritoVenta();

        ValidacionException error = assertThrows(ValidacionException.class,
                () -> carrito.agregar(producto(1, 2, true), "3"));

        assertTrue(error.erroresPorCampo().get(CarritoVenta.CANTIDAD).contains("Solo hay 2"));
    }

    @Test
    void rechazaProductosDesactivadosYCantidadesInvalidas() {
        CarritoVenta carrito = new CarritoVenta();

        assertThrows(ValidacionException.class, () -> carrito.agregar(producto(1, 5, false), "1"));
        assertThrows(ValidacionException.class, () -> carrito.agregar(producto(1, 5, true), "0"));
        assertThrows(ValidacionException.class, () -> carrito.agregar(null, "1"));
        assertTrue(carrito.estaVacio());
    }

    @Test
    void quitarEliminaElRenglon() {
        CarritoVenta carrito = new CarritoVenta();
        carrito.agregar(producto(1, 5, true), "1");

        carrito.quitar(1);

        assertTrue(carrito.estaVacio());
    }
}
