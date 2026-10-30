package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.OportunidadApi;
import com.devsuj.crmdesktop.model.EtapaOportunidad;
import com.devsuj.crmdesktop.model.HistorialEtapa;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.model.OportunidadActualizarRequest;
import com.devsuj.crmdesktop.model.OportunidadRequest;
import com.devsuj.crmdesktop.model.Pagina;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lógica del pipeline de oportunidades: validar, crear, editar, mover de etapa y armar el
 * diagrama del proceso a partir del historial. Sin JavaFX.
 *
 * <p>Reglas del backend: GANADA solo se pone al registrar la venta; una oportunidad cerrada
 * (GANADA o PERDIDA) ya no cambia; si una PERDIDA se retoma, se crea una NUEVA ligada a ella.</p>
 */
public class OportunidadService {

    public static final int TAMANO_PAGINA = 20;
    public static final String CLIENTE = "clienteId";
    public static final String TITULO = "titulo";
    public static final String MONTO = "montoEstimado";
    public static final String FECHA_CIERRE = "fechaCierreEstimada";
    public static final String ETAPA = "etapa";
    public static final String COMENTARIO = "comentario";

    /** Cómo se dibuja cada etapa en el diagrama del proceso. */
    public enum EstadoPaso { RECORRIDA, ACTUAL, PENDIENTE }

    private final OportunidadApi api;

    public OportunidadService(OportunidadApi api) {
        this.api = api;
    }

    /** @param abiertas {@code true} = solo abiertas; {@code null} = todas */
    public Pagina<Oportunidad> buscar(EtapaOportunidad etapa, Integer usuarioId, Boolean abiertas, int pagina) {
        return api.buscar(etapa, usuarioId, null, abiertas, pagina, TAMANO_PAGINA);
    }

    /** Oportunidades abiertas de un cliente (para ligarlas a una venta). */
    public List<Oportunidad> abiertasDe(int clienteId) {
        return api.buscar(null, null, clienteId, true, 0, 100).contenido();
    }

    public List<HistorialEtapa> historial(Oportunidad oportunidad) {
        return api.historial(oportunidad.id());
    }

    public static Optional<String> reglaTitulo(String valor) {
        return Reglas.obligatorio(valor, "Escribe un título que describa la oportunidad.")
                .or(() -> Reglas.maximo(valor, 150));
    }

    public static Optional<String> reglaMonto(String valor) {
        return Reglas.montoValido(valor, "Escribe el monto estimado en quetzales.", 10);
    }

    /**
     * Crea ({@code original == null}) o edita una oportunidad.
     *
     * @param anterior oportunidad PERDIDA de la que esta es un nuevo intento; {@code null} si no
     */
    public Oportunidad guardar(Oportunidad original, Oportunidad anterior, OportunidadFormulario datos) {
        if (original != null && original.etapa().esCerrada()) {
            throw new IllegalStateException("La oportunidad está " + original.etapa().etiqueta()
                    + " y ya no se edita. Si se retoma, crea un nuevo intento.");
        }
        Map<String, String> errores = new LinkedHashMap<>();
        if (original == null) {
            Reglas.revisar(errores, CLIENTE, () -> Reglas.obligatorio(datos.cliente(), "Elige el cliente."));
        }
        Reglas.revisar(errores, TITULO, () -> reglaTitulo(datos.titulo()));
        Reglas.revisar(errores, MONTO, () -> reglaMonto(datos.monto()));
        Reglas.exigir(errores);

        String titulo = datos.titulo().strip();
        BigDecimal monto = Reglas.monto(datos.monto()).orElseThrow();
        if (original != null) {
            return api.actualizar(original.id(),
                    new OportunidadActualizarRequest(titulo, monto, datos.fechaCierreEstimada()));
        }
        Integer responsableId = datos.responsable() == null ? null : datos.responsable().id();
        Integer anteriorId = anterior == null ? null : anterior.id();
        return api.crear(new OportunidadRequest(datos.cliente().id(), titulo, monto, datos.fechaCierreEstimada(),
                responsableId, anteriorId));
    }

    /** Mueve la oportunidad a otra etapa abierta o la marca PERDIDA. */
    public Oportunidad cambiarEtapa(Oportunidad oportunidad, EtapaOportunidad destino, String comentario) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (destino == null) {
            errores.put(ETAPA, "Elige la nueva etapa.");
        } else if (destino == EtapaOportunidad.GANADA) {
            errores.put(ETAPA, "Una oportunidad se gana al registrar su venta.");
        } else if (!oportunidad.etapa().destinosPermitidos().contains(destino)) {
            errores.put(ETAPA, "La oportunidad no puede pasar de " + oportunidad.etapa().etiqueta()
                    + " a " + destino.etiqueta() + ".");
        }
        if (destino == EtapaOportunidad.PERDIDA) {
            Reglas.revisar(errores, COMENTARIO,
                    () -> Reglas.obligatorio(comentario, "Escribe por qué se perdió (sirve para el análisis)."));
        }
        Reglas.revisar(errores, COMENTARIO, () -> Reglas.maximo(comentario, 255));
        Reglas.exigir(errores);
        return api.cambiarEtapa(oportunidad.id(), destino, Reglas.limpiar(comentario));
    }

    /**
     * Estado de cada etapa del camino normal para el diagrama: las que ya pasó, la actual y las que
     * faltan. Si la oportunidad se perdió, la etapa donde estaba al perderse queda como la última recorrida.
     */
    public static Map<EtapaOportunidad, EstadoPaso> diagrama(EtapaOportunidad actual, List<HistorialEtapa> historial) {
        Map<EtapaOportunidad, EstadoPaso> pasos = new EnumMap<>(EtapaOportunidad.class);
        EtapaOportunidad.camino().forEach(etapa -> pasos.put(etapa, EstadoPaso.PENDIENTE));
        historial.forEach(paso -> {
            if (pasos.containsKey(paso.etapa())) {
                pasos.put(paso.etapa(), EstadoPaso.RECORRIDA);
            }
        });
        if (pasos.containsKey(actual)) {
            pasos.put(actual, EstadoPaso.ACTUAL);
        }
        return pasos;
    }

    /** Días totales del proceso (suma de los días en cada etapa). */
    public static long diasTotales(List<HistorialEtapa> historial) {
        return historial.stream().map(HistorialEtapa::diasEnEtapa).filter(d -> d != null).mapToLong(Long::longValue).sum();
    }
}
