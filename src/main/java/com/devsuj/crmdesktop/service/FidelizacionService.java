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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Fidelización: saldo, estado de cuenta y canje de puntos por productos. Sin JavaFX.
 *
 * <p>Los puntos se ganan solos al registrar una venta (1 por cada Q100). El canje lo valida la API
 * (saldo, stock, producto canjeable); aquí solo se revisa lo básico antes de enviar.</p>
 */
public class FidelizacionService {

    public static final int TAMANO_PAGINA = 15;
    public static final int MAXIMA_CANTIDAD = 100;
    public static final String PRODUCTO = "productoId";
    public static final String CANTIDAD = "cantidad";

    private final FidelizacionApi api;

    public FidelizacionService(FidelizacionApi api) {
        this.api = api;
    }

    public SaldoPuntos saldo(Cliente cliente) {
        return api.saldo(cliente.id());
    }

    public Pagina<MovimientoPuntos> movimientos(Cliente cliente, int pagina) {
        return api.movimientos(cliente.id(), pagina, TAMANO_PAGINA);
    }

    /** Puntos que costaría el canje (solo para mostrar; la API hace el cálculo definitivo). */
    public static Optional<Long> puntosNecesarios(Producto producto, String cantidad) {
        if (producto == null || producto.precioPuntos() == null) {
            return Optional.empty();
        }
        return Reglas.entero(cantidad).filter(c -> c > 0).map(c -> (long) c * producto.precioPuntos());
    }

    public CanjeResponse canjear(Cliente cliente, Producto producto, String cantidad) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (cliente.estado() == EstadoCliente.INACTIVO) {
            throw new IllegalStateException(cliente.nombre() + " está inactivo; no puede canjear puntos.");
        }
        if (producto == null) {
            errores.put(PRODUCTO, "Elige el producto.");
        } else if (!producto.canjeable() || producto.precioPuntos() == null) {
            errores.put(PRODUCTO, producto.nombre() + " no tiene precio en puntos.");
        }
        Reglas.revisar(errores, CANTIDAD, () -> Reglas.enteroValido(cantidad, true, 1, "Escribe la cantidad.")
                .or(() -> Reglas.entero(cantidad).filter(c -> c > MAXIMA_CANTIDAD)
                        .map(c -> "Máximo " + MAXIMA_CANTIDAD + " unidades por canje.")));
        Reglas.exigir(errores);
        return api.canjear(cliente.id(), new CanjeRequest(producto.id(), Reglas.entero(cantidad).orElseThrow()));
    }
}
