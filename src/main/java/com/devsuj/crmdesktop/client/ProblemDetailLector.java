package com.devsuj.crmdesktop.client;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Convierte una respuesta de error de la API en una {@link ApiException}.
 *
 * <p>La API responde los errores con {@code ProblemDetail} (RFC 9457):
 * {@code {"type", "title", "status", "detail", "instance", ...}}. Se usa {@code detail} como
 * mensaje porque es el texto en español que escribe el backend. {@code title} no se usa porque
 * suele venir en inglés ("Bad Request").</p>
 *
 * <p>Los errores por campo (400 de validación) se buscan en una propiedad extra. Como el backend
 * todavía no define su nombre exacto, se aceptan {@code errores}, {@code errors} o {@code campos},
 * tanto en forma de objeto ({@code {"email": "mensaje"}}) como de lista
 * ({@code [{"campo": "email", "mensaje": "..."}]}).</p>
 */
public final class ProblemDetailLector {

    private static final String[] PROPIEDADES_DE_CAMPOS = {"errores", "errors", "campos"};
    /** Propiedades estándar del ProblemDetail; las demás se guardan como datos extra. */
    private static final Set<String> ESTANDAR =
            Set.of("type", "title", "status", "detail", "instance", "errores", "errors", "campos");

    private ProblemDetailLector() {
    }

    public static ApiException aExcepcion(int status, String cuerpo, ObjectMapper json) {
        JsonNode raiz = leer(cuerpo, json);
        String detalle = texto(raiz.path("detail"));
        String mensaje = detalle.isBlank() ? mensajePorDefecto(status) : detalle;
        return new ApiException(status, mensaje, erroresPorCampo(raiz), datosExtra(raiz));
    }

    /** Por ejemplo {@code "clientesAsignados": 4} en el 409 al desactivar un vendedor. */
    private static Map<String, String> datosExtra(JsonNode raiz) {
        Map<String, String> datos = new LinkedHashMap<>();
        if (raiz.isObject()) {
            raiz.properties().forEach(e -> {
                if (!ESTANDAR.contains(e.getKey()) && e.getValue().isValueNode()) {
                    datos.put(e.getKey(), texto(e.getValue()));
                }
            });
        }
        return datos;
    }

    /** Mensaje en español cuando la API no envía un {@code detail}. */
    static String mensajePorDefecto(int status) {
        return switch (status) {
            case 400 -> "Los datos enviados no son válidos. Revisa el formulario.";
            case 401 -> "Tu sesión expiró o no has iniciado sesión.";
            case 403 -> "No tienes permiso para realizar esta acción.";
            case 404 -> "No se encontró el recurso solicitado en el servidor.";
            case 409 -> "La operación entra en conflicto con datos que ya existen.";
            default -> status >= 500
                    ? "El servidor tuvo un problema. Intenta de nuevo más tarde."
                    : "Ocurrió un error al comunicarse con el servidor (código " + status + ").";
        };
    }

    private static JsonNode leer(String cuerpo, ObjectMapper json) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return json.missingNode();
        }
        try {
            return json.readTree(cuerpo);
        } catch (JacksonException e) {
            // El cuerpo no es JSON (por ejemplo, una página HTML de error): usamos el mensaje por defecto.
            return json.missingNode();
        }
    }

    private static Map<String, String> erroresPorCampo(JsonNode raiz) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (String propiedad : PROPIEDADES_DE_CAMPOS) {
            JsonNode nodo = raiz.path(propiedad);
            if (nodo.isObject()) {
                nodo.properties().forEach(e -> errores.put(e.getKey(), texto(e.getValue())));
            } else if (nodo.isArray()) {
                for (JsonNode item : nodo) {
                    String campo = primerTexto(item, "campo", "field");
                    String mensaje = primerTexto(item, "mensaje", "message", "defaultMessage");
                    if (!campo.isBlank()) {
                        errores.put(campo, mensaje);
                    }
                }
            }
        }
        return errores;
    }

    private static String primerTexto(JsonNode nodo, String... nombres) {
        for (String nombre : nombres) {
            String valor = texto(nodo.path(nombre));
            if (!valor.isBlank()) {
                return valor;
            }
        }
        return "";
    }

    private static String texto(JsonNode nodo) {
        return nodo.isValueNode() ? nodo.asString("") : "";
    }
}
