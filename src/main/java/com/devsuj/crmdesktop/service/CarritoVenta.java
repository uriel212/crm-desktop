package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.model.DetalleVentaRequest;
import com.devsuj.crmdesktop.model.Producto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Los renglones de una venta mientras se arma (producto y cantidad). Sin JavaFX y sin precios:
 * los montos los calcula siempre la API ({@code POST /api/ventas/calculo}).
 *
 * <p>Aplica las reglas de la API antes de enviar: sin productos repetidos (se suman), cantidad de 1
 * a 10000, máximo 50 renglones, solo productos activos y con existencias suficientes.</p>
 */
public class CarritoVenta {

    public static final int MAXIMO_RENGLONES = 50;
    public static final int MAXIMA_CANTIDAD = 10_000;
    public static final String PRODUCTO = "productoId";
    public static final String CANTIDAD = "cantidad";

    /** Un renglón del carrito. */
    public record Renglon(Producto producto, int cantidad) {
    }

    private final Map<Integer, Renglon> renglones = new LinkedHashMap<>();

    /**
     * Agrega un producto; si ya estaba, suma la cantidad.
     *
     * @throws ValidacionException si el producto o la cantidad no son válidos
     */
    public void agregar(Producto producto, String cantidadTexto) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (producto == null) {
            errores.put(PRODUCTO, "Elige un producto.");
        } else if (!producto.activo()) {
            errores.put(PRODUCTO, producto.nombre() + " está desactivado y no se puede vender.");
        } else if (!renglones.containsKey(producto.id()) && renglones.size() >= MAXIMO_RENGLONES) {
            errores.put(PRODUCTO, "Una venta admite máximo " + MAXIMO_RENGLONES + " productos distintos.");
        }
        Reglas.revisar(errores, CANTIDAD, () -> Reglas.enteroValido(cantidadTexto, true, 1, "Escribe la cantidad."));
        Reglas.exigir(errores);

        int anterior = renglones.containsKey(producto.id()) ? renglones.get(producto.id()).cantidad() : 0;
        int total = anterior + Reglas.entero(cantidadTexto).orElseThrow();
        if (total > MAXIMA_CANTIDAD) {
            throw new ValidacionException(Map.of(CANTIDAD, "Máximo " + MAXIMA_CANTIDAD + " unidades por producto."));
        }
        if (total > producto.stock()) {
            throw new ValidacionException(Map.of(CANTIDAD, "Solo hay " + producto.stock() + " de "
                    + producto.nombre() + " en existencia" + (anterior > 0 ? " (ya llevas " + anterior + ")." : ".")));
        }
        renglones.put(producto.id(), new Renglon(producto, total));
    }

    public void quitar(int productoId) {
        renglones.remove(productoId);
    }

    public void vaciar() {
        renglones.clear();
    }

    public boolean estaVacio() {
        return renglones.isEmpty();
    }

    public List<Renglon> renglones() {
        return new ArrayList<>(renglones.values());
    }

    /** Lo que viaja a la API: solo producto y cantidad. */
    public List<DetalleVentaRequest> aDetalles() {
        return renglones.values().stream()
                .map(r -> new DetalleVentaRequest(r.producto().id(), r.cantidad()))
                .toList();
    }
}
