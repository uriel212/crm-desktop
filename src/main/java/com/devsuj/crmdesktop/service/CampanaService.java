package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.CampanaApi;
import com.devsuj.crmdesktop.model.CanalCampana;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.CampanaRequest;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Destinatario;
import com.devsuj.crmdesktop.model.EstadoCampana;
import com.devsuj.crmdesktop.model.GeneracionDestinatarios;
import com.devsuj.crmdesktop.model.MetricasCampana;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.ResultadoCampana;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Campañas de marketing: alta, edición, estado, destinatarios y su resultado. Sin JavaFX.
 *
 * <p>Reglas del backend: nace PLANEADA; FINALIZADA y CANCELADA ya no se editan ni generan
 * destinatarios; un destinatario solo se quita en PLANEADA; el resultado solo avanza
 * (ENVIADO → ABIERTO → RESPONDIO) y solo con la campaña ACTIVA o FINALIZADA. No se borran.</p>
 */
public class CampanaService {

    public static final int TAMANO_PAGINA = 20;
    public static final String NOMBRE = "nombre";
    public static final String CANAL = "canal";
    public static final String ASUNTO = "asunto";
    public static final String MENSAJE = "mensaje";
    public static final String SEGMENTO = "segmento";
    public static final String FECHA_INICIO = "fechaInicio";
    public static final String FECHA_FIN = "fechaFin";
    public static final String PRESUPUESTO = "presupuesto";

    private final CampanaApi api;

    public CampanaService(CampanaApi api) {
        this.api = api;
    }

    public Pagina<Campana> buscar(EstadoCampana estado, CanalCampana canal, String texto, int pagina) {
        return api.buscar(estado, canal, Reglas.limpiar(texto), pagina, TAMANO_PAGINA);
    }

    /** Campañas a las que se puede ligar una promoción (no cerradas), para el combo del formulario. */
    public List<Campana> abiertas() {
        return api.buscar(null, null, null, 0, 100).contenido().stream()
                .filter(c -> !c.estado().estaCerrada())
                .toList();
    }

    public Campana guardar(Campana original, CampanaFormulario datos) {
        if (original != null && original.estado().estaCerrada()) {
            throw new IllegalStateException("La campaña está " + original.estado().etiqueta().toLowerCase()
                    + " y ya no se edita.");
        }
        Map<String, String> errores = new LinkedHashMap<>();
        Reglas.revisar(errores, NOMBRE, () -> Reglas.obligatorio(datos.nombre(), "Escribe el nombre de la campaña.")
                .or(() -> Reglas.maximo(datos.nombre(), 120)));
        Reglas.revisar(errores, CANAL, () -> Reglas.obligatorio(datos.canal(), "Elige el canal."));
        Reglas.revisar(errores, ASUNTO, () -> Reglas.obligatorio(datos.asunto(), "Escribe el asunto.")
                .or(() -> Reglas.maximo(datos.asunto(), 150)));
        Reglas.revisar(errores, MENSAJE, () -> Reglas.obligatorio(datos.mensaje(), "Escribe el mensaje.")
                .or(() -> Reglas.maximo(datos.mensaje(), 5000)));
        Reglas.revisar(errores, SEGMENTO, () -> Reglas.obligatorio(datos.segmento(), "Elige a quién va dirigida."));
        Reglas.rangoFechas(errores, FECHA_INICIO, datos.fechaInicio(), FECHA_FIN, datos.fechaFin());
        Reglas.revisar(errores, PRESUPUESTO, () -> Reglas.montoValido(datos.presupuesto(),
                "Escribe el presupuesto en quetzales (0 si no tiene).", 8));
        Reglas.exigir(errores);

        CampanaRequest request = new CampanaRequest(datos.nombre().strip(), datos.canal(), datos.asunto().strip(),
                datos.mensaje().strip(), datos.segmento(), datos.fechaInicio(), datos.fechaFin(),
                Reglas.monto(datos.presupuesto()).orElseThrow());
        return original == null ? api.crear(request) : api.actualizar(original.id(), request);
    }

    public Campana cambiarEstado(Campana campana, EstadoCampana estado) {
        if (estado == null || estado == campana.estado()) {
            throw new ValidacionException(Map.of("estado", "Elige un estado distinto al actual."));
        }
        return api.cambiarEstado(campana.id(), estado);
    }

    public Pagina<Destinatario> destinatarios(Campana campana, ResultadoCampana resultado, int pagina) {
        return api.destinatarios(campana.id(), resultado, pagina, TAMANO_PAGINA);
    }

    public GeneracionDestinatarios generarDestinatarios(Campana campana) {
        exigirAbierta(campana, "generar destinatarios");
        return api.generarDestinatarios(campana.id());
    }

    public Destinatario agregarDestinatario(Campana campana, Cliente cliente) {
        exigirAbierta(campana, "agregar destinatarios");
        if (cliente == null) {
            throw new ValidacionException(Map.of("clienteId", "Elige el cliente."));
        }
        return api.agregarDestinatario(campana.id(), cliente.id());
    }

    public void quitarDestinatario(Campana campana, Destinatario destinatario) {
        if (campana.estado() != EstadoCampana.PLANEADA) {
            throw new IllegalStateException("Solo se quitan destinatarios mientras la campaña está planeada.");
        }
        api.quitarDestinatario(campana.id(), destinatario.clienteId());
    }

    /** ¿Se puede registrar el resultado de los destinatarios en esta campaña? */
    public static boolean admiteResultados(Campana campana) {
        return campana != null
                && (campana.estado() == EstadoCampana.ACTIVA || campana.estado() == EstadoCampana.FINALIZADA);
    }

    public Destinatario avanzarResultado(Campana campana, Destinatario destinatario, ResultadoCampana nuevo) {
        if (!admiteResultados(campana)) {
            throw new IllegalStateException("Los resultados se registran con la campaña activa o finalizada.");
        }
        if (!destinatario.resultado().puedeCambiarA(nuevo)) {
            throw new IllegalStateException("El resultado solo avanza: " + destinatario.clienteNombre() + " ya está en "
                    + destinatario.resultado().etiqueta() + ".");
        }
        return api.cambiarResultado(campana.id(), destinatario.clienteId(), nuevo);
    }

    public MetricasCampana metricas(Campana campana) {
        return api.metricas(campana.id());
    }

    private static void exigirAbierta(Campana campana, String accion) {
        if (campana.estado().estaCerrada()) {
            throw new IllegalStateException("No se pueden " + accion + " en una campaña "
                    + campana.estado().etiqueta().toLowerCase() + ".");
        }
    }
}
