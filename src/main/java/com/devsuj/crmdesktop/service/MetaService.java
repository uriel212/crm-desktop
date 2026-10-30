package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.MetaApi;
import com.devsuj.crmdesktop.model.MetaVenta;
import com.devsuj.crmdesktop.model.MetaVentaRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.util.Formatos;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Metas mensuales de venta (solo los VENDEDOR tienen metas). Sin JavaFX. */
public class MetaService {

    public static final int TAMANO_PAGINA = 20;
    public static final String VENDEDOR = "usuarioId";
    public static final String ANIO = "anio";
    public static final String MES = "mes";
    public static final String MONTO = "montoObjetivo";

    private final MetaApi api;

    public MetaService(MetaApi api) {
        this.api = api;
    }

    public Pagina<MetaVenta> buscar(Integer usuarioId, Integer anio, Integer mes, int pagina) {
        return api.buscar(usuarioId, anio, mes, pagina, TAMANO_PAGINA);
    }

    public static Optional<String> reglaMonto(String texto) {
        return Reglas.montoValido(texto, "Escribe el monto objetivo en quetzales.", 10)
                .or(() -> Reglas.monto(texto).filter(m -> m.signum() == 0).map(m -> "Debe ser mayor que cero."));
    }

    /** "Octubre 2026" */
    public static String periodo(int anio, int mes) {
        String nombre = Month.of(mes).getDisplayName(TextStyle.FULL, Formatos.GUATEMALA);
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1) + " " + anio;
    }

    public MetaVenta guardar(MetaVenta original, Usuario vendedor, Integer anio, Integer mes, String monto) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (vendedor == null) {
            errores.put(VENDEDOR, "Elige el vendedor.");
        } else if (vendedor.rol() != Rol.VENDEDOR) {
            errores.put(VENDEDOR, "Solo los vendedores tienen metas.");
        }
        if (anio == null || anio < 2020 || anio > 2100) {
            errores.put(ANIO, "Elige un año entre 2020 y 2100.");
        }
        if (mes == null || mes < 1 || mes > 12) {
            errores.put(MES, "Elige el mes.");
        }
        Reglas.revisar(errores, MONTO, () -> reglaMonto(monto));
        Reglas.exigir(errores);
        MetaVentaRequest request = new MetaVentaRequest(vendedor.id(), anio, mes, Reglas.monto(monto).orElseThrow());
        return original == null ? api.crear(request) : api.actualizar(original.id(), request);
    }

    public void eliminar(MetaVenta meta) {
        api.eliminar(meta.id());
    }
}
