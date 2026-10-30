package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.InteraccionApi;
import com.devsuj.crmdesktop.model.EstadoInteraccion;
import com.devsuj.crmdesktop.model.Interaccion;
import com.devsuj.crmdesktop.model.InteraccionActualizarRequest;
import com.devsuj.crmdesktop.model.InteraccionRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.TipoInteraccion;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lógica de interacciones y actividades programadas. Sin JavaFX.
 *
 * <p>Reglas del backend: una interacción nueva es REALIZADA (fecha pasada u hoy) o PROGRAMADA
 * (fecha futura). Una PROGRAMADA se cierra como REALIZADA (con su resultado) o CANCELADA. Nada se
 * borra, y una CANCELADA ya no se edita.</p>
 */
public class InteraccionService {

    public static final int TAMANO_PAGINA = 20;
    public static final int DIAS_PROXIMAS = 7;
    public static final String CLIENTE = "clienteId";
    public static final String TIPO = "tipo";
    public static final String ESTADO = "estado";
    public static final String FECHA = "fecha";
    public static final String HORA = "hora";
    public static final String ASUNTO = "asunto";
    public static final String DESCRIPCION = "descripcion";
    public static final String RESULTADO = "resultado";

    private final InteraccionApi api;
    /** Reloj en hora de Guatemala; en las pruebas se usa uno fijo. */
    private final Clock reloj;

    public InteraccionService(InteraccionApi api, Clock reloj) {
        this.api = api;
        this.reloj = reloj;
    }

    public Pagina<Interaccion> buscar(Integer usuarioId, Integer clienteId, TipoInteraccion tipo,
                                      EstadoInteraccion estado, LocalDate desde, LocalDate hasta, int pagina) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalStateException("La fecha \"desde\" no puede ser posterior a \"hasta\".");
        }
        return api.buscar(usuarioId, clienteId, tipo, estado, desde, hasta, pagina, TAMANO_PAGINA);
    }

    /** Actividades programadas de los próximos {@value #DIAS_PROXIMAS} días (incluye las vencidas). */
    public List<Interaccion> proximas(Integer usuarioId) {
        return api.proximas(usuarioId, DIAS_PROXIMAS);
    }

    public static Optional<String> reglaAsunto(String valor) {
        return Reglas.obligatorio(valor, "Escribe el asunto, por ejemplo \"Seguimiento de cotización\".")
                .or(() -> Reglas.maximo(valor, 150));
    }

    /** "09:30" → 09:30. @return vacío si el texto no es una hora válida */
    public static Optional<LocalTime> hora(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalTime.parse(texto.strip().length() == 4 ? "0" + texto.strip() : texto.strip()));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /**
     * Crea ({@code original == null}) o corrige/reprograma una interacción.
     */
    public Interaccion guardar(Interaccion original, InteraccionFormulario datos) {
        if (original != null && original.estado() == EstadoInteraccion.CANCELADA) {
            throw new IllegalStateException("Una actividad cancelada ya no se edita.");
        }
        EstadoInteraccion estado = original == null ? datos.estado() : original.estado();
        Map<String, String> errores = new LinkedHashMap<>();
        if (original == null) {
            Reglas.revisar(errores, CLIENTE, () -> Reglas.obligatorio(datos.cliente(), "Elige el cliente."));
            Reglas.revisar(errores, ESTADO, () -> Reglas.obligatorio(estado, "Indica si ya ocurrió o es una actividad a futuro."));
        }
        Reglas.revisar(errores, TIPO, () -> Reglas.obligatorio(datos.tipo(), "Elige el tipo."));
        Reglas.revisar(errores, ASUNTO, () -> reglaAsunto(datos.asunto()));
        Reglas.revisar(errores, DESCRIPCION, () -> Reglas.maximo(datos.descripcion(), 5000));
        LocalDateTime fecha = fecha(datos, estado, original != null, errores);
        Reglas.exigir(errores);

        String asunto = datos.asunto().strip();
        String descripcion = Reglas.limpiar(datos.descripcion());
        return original == null
                ? api.crear(datos.cliente().id(), new InteraccionRequest(datos.tipo(), estado, fecha, asunto, descripcion))
                : api.actualizar(original.id(), new InteraccionActualizarRequest(datos.tipo(), fecha, asunto, descripcion));
    }

    /** Arma la fecha y hora y revisa las reglas de fecha según el estado. */
    private LocalDateTime fecha(InteraccionFormulario datos, EstadoInteraccion estado, boolean editando,
                                Map<String, String> errores) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        boolean horaEscrita = datos.hora() != null && !datos.hora().isBlank();
        Optional<LocalTime> hora = hora(datos.hora());
        if (horaEscrita && hora.isEmpty()) {
            errores.put(HORA, "Escribe la hora como 09:30 (formato de 24 horas).");
            return null;
        }
        if (datos.fecha() == null) {
            // REALIZADA sin fecha al crear = ahora (la pone la API). En los demás casos es obligatoria.
            if (estado == EstadoInteraccion.REALIZADA && !editando) {
                return null;
            }
            errores.put(FECHA, "Elige la fecha.");
            return null;
        }
        if (estado == EstadoInteraccion.PROGRAMADA && hora.isEmpty()) {
            errores.put(HORA, "Escribe la hora de la actividad, por ejemplo 10:00.");
            return null;
        }
        LocalDateTime fecha = datos.fecha().atTime(hora.orElse(ahora.toLocalTime().withSecond(0).withNano(0)));
        if (estado == EstadoInteraccion.PROGRAMADA && !fecha.isAfter(ahora)) {
            errores.put(FECHA, "Una actividad programada debe ser en el futuro.");
        }
        if (estado == EstadoInteraccion.REALIZADA && fecha.isAfter(ahora)) {
            errores.put(FECHA, "Lo que ya ocurrió no puede tener fecha futura; agéndalo como actividad programada.");
        }
        return fecha;
    }

    /** Cierra una actividad PROGRAMADA como REALIZADA (con su resultado) o CANCELADA. */
    public Interaccion cerrar(Interaccion actividad, EstadoInteraccion nuevoEstado, String resultado) {
        if (actividad.estado() != EstadoInteraccion.PROGRAMADA) {
            throw new IllegalStateException("Solo una actividad programada se puede cerrar; esta ya está "
                    + actividad.estado().etiqueta().toLowerCase() + ".");
        }
        Map<String, String> errores = new LinkedHashMap<>();
        if (nuevoEstado != EstadoInteraccion.REALIZADA && nuevoEstado != EstadoInteraccion.CANCELADA) {
            errores.put(ESTADO, "Elige Realizada o Cancelada.");
        }
        Reglas.revisar(errores, RESULTADO, () -> Reglas.maximo(resultado, 5000));
        Reglas.exigir(errores);
        return api.cambiarEstado(actividad.id(), nuevoEstado, Reglas.limpiar(resultado));
    }
}
