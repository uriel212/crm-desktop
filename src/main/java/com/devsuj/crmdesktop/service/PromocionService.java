package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.PromocionApi;
import com.devsuj.crmdesktop.model.AlcancePromocion;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.Promocion;
import com.devsuj.crmdesktop.model.PromocionRequest;
import com.devsuj.crmdesktop.model.TipoPromocion;
import com.devsuj.crmdesktop.util.Formatos;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Promociones: alta, edición, activar/desactivar y borrar. Las aplica solo el servidor al vender. Sin JavaFX.
 *
 * <p>Reglas (las mismas que valida la API): PORCENTAJE ≤ 100; alcance PRODUCTO lleva productos y no
 * monto mínimo; alcance VENTA no lleva productos y puede tener monto mínimo; fechas en orden.</p>
 */
public class PromocionService {

    public static final int TAMANO_PAGINA = 20;
    public static final String NOMBRE = "nombre";
    public static final String TIPO = "tipo";
    public static final String ALCANCE = "alcance";
    public static final String VALOR = "valor";
    public static final String MONTO_MINIMO = "montoMinimo";
    public static final String FECHA_INICIO = "fechaInicio";
    public static final String FECHA_FIN = "fechaFin";
    public static final String PRODUCTOS = "productoIds";

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final PromocionApi api;

    public PromocionService(PromocionApi api) {
        this.api = api;
    }

    public Pagina<Promocion> buscar(Boolean activa, AlcancePromocion alcance, int pagina) {
        return api.buscar(activa, alcance, null, null, pagina, TAMANO_PAGINA);
    }

    public List<Promocion> vigentes() {
        return api.vigentes();
    }

    /** "15 %" o "Q500.00" (por unidad en alcance PRODUCTO). */
    public static String descripcionValor(Promocion p) {
        if (p.valor() == null) {
            return "";
        }
        if (p.tipo() == TipoPromocion.PORCENTAJE) {
            return Formatos.porcentaje(p.valor());
        }
        return Formatos.moneda(p.valor()) + (p.alcance() == AlcancePromocion.PRODUCTO ? " por unidad" : "");
    }

    public Promocion guardar(Promocion original, PromocionFormulario datos) {
        Map<String, String> errores = new LinkedHashMap<>();
        Reglas.revisar(errores, NOMBRE, () -> Reglas.obligatorio(datos.nombre(), "Escribe el nombre de la promoción.")
                .or(() -> Reglas.maximo(datos.nombre(), 120)));
        Reglas.revisar(errores, TIPO, () -> Reglas.obligatorio(datos.tipo(), "Elige el tipo de descuento."));
        Reglas.revisar(errores, ALCANCE, () -> Reglas.obligatorio(datos.alcance(), "Elige el alcance."));
        Reglas.revisar(errores, VALOR, () -> Reglas.montoValido(datos.valor(), "Escribe el valor del descuento.", 8)
                .or(() -> Reglas.monto(datos.valor()).filter(v -> v.signum() == 0).map(v -> "Debe ser mayor que cero.")));
        if (datos.tipo() == TipoPromocion.PORCENTAJE && !errores.containsKey(VALOR)
                && Reglas.monto(datos.valor()).orElse(BigDecimal.ZERO).compareTo(CIEN) > 0) {
            errores.put(VALOR, "Un porcentaje no puede pasar de 100.");
        }
        List<Producto> productos = datos.productos() == null ? List.of() : datos.productos();
        if (datos.alcance() == AlcancePromocion.PRODUCTO && productos.isEmpty()) {
            errores.put(PRODUCTOS, "Marca al menos un producto.");
        }
        if (datos.alcance() == AlcancePromocion.VENTA) {
            Reglas.revisar(errores, MONTO_MINIMO, () -> datos.montoMinimo() == null || datos.montoMinimo().isBlank()
                    ? Optional.empty()
                    : Reglas.montoValido(datos.montoMinimo(), "", 10));
        }
        Reglas.rangoFechas(errores, FECHA_INICIO, datos.fechaInicio(), FECHA_FIN, datos.fechaFin());
        Reglas.exigir(errores);

        boolean porProducto = datos.alcance() == AlcancePromocion.PRODUCTO;
        PromocionRequest request = new PromocionRequest(
                datos.campana() == null ? null : datos.campana().id(),
                datos.nombre().strip(), datos.tipo(), datos.alcance(), Reglas.monto(datos.valor()).orElseThrow(),
                porProducto ? null : Reglas.monto(datos.montoMinimo()).orElse(null),
                datos.fechaInicio(), datos.fechaFin(),
                porProducto ? productos.stream().map(Producto::id).toList() : List.of());
        return original == null ? api.crear(request) : api.actualizar(original.id(), request);
    }

    public Promocion cambiarEstado(Promocion promocion, boolean activa) {
        return api.cambiarEstado(promocion.id(), activa);
    }

    public void eliminar(Promocion promocion) {
        api.eliminar(promocion.id());
    }
}
