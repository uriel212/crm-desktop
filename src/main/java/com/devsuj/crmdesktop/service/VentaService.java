package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.VentaApi;
import com.devsuj.crmdesktop.model.CalculoVenta;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.model.Venta;
import com.devsuj.crmdesktop.model.VentaRequest;
import com.devsuj.crmdesktop.model.VentaResumen;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lógica de ventas: consultar, cotizar y registrar. Sin JavaFX.
 *
 * <p>El frontend NUNCA envía precios ni descuentos: arma el carrito (producto y cantidad), pide la
 * cotización a la API y la muestra; al confirmar, la API vuelve a calcular y guarda. Al registrar,
 * la API descuenta el stock, gana la oportunidad ligada y suma los puntos del cliente.</p>
 */
public class VentaService {

    public static final int TAMANO_PAGINA = 20;
    public static final String CLIENTE = "clienteId";

    private final VentaApi api;

    public VentaService(VentaApi api) {
        this.api = api;
    }

    public Pagina<VentaResumen> buscar(Integer clienteId, Integer usuarioId, LocalDate desde, LocalDate hasta,
                                       int pagina) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalStateException("La fecha \"desde\" no puede ser posterior a \"hasta\".");
        }
        return api.buscar(clienteId, usuarioId, desde, hasta, pagina, TAMANO_PAGINA);
    }

    public Venta obtener(VentaResumen resumen) {
        return api.obtener(resumen.id());
    }

    /** Cotización con las promociones vigentes hoy. @return {@code null} si el carrito está vacío */
    public CalculoVenta cotizar(CarritoVenta carrito) {
        if (carrito.estaVacio()) {
            return null;
        }
        return api.calcular(new VentaRequest(null, null, null, carrito.aDetalles()));
    }

    /**
     * @param oportunidad oportunidad abierta del cliente que se gana con esta venta; opcional
     * @param responsable solo GERENTE/ADMIN; {@code null} = quien registra
     */
    public Venta registrar(Cliente cliente, Oportunidad oportunidad, Usuario responsable, CarritoVenta carrito) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (cliente == null) {
            errores.put(CLIENTE, "Elige el cliente.");
        } else if (cliente.estado() == EstadoCliente.INACTIVO) {
            errores.put(CLIENTE, cliente.nombre() + " está inactivo; reactívalo antes de venderle.");
        }
        if (carrito.estaVacio()) {
            errores.put(CarritoVenta.PRODUCTO, "Agrega al menos un producto.");
        }
        if (oportunidad != null && cliente != null && !oportunidad.clienteId().equals(cliente.id())) {
            errores.put("oportunidadId", "La oportunidad es de otro cliente.");
        }
        Reglas.exigir(errores);
        return api.registrar(new VentaRequest(cliente.id(), oportunidad == null ? null : oportunidad.id(),
                responsable == null ? null : responsable.id(), carrito.aDetalles()));
    }
}
