package com.devsuj.crmdesktop.client;

import java.util.Map;
import java.util.Optional;

/**
 * Error al comunicarse con la API, ya traducido a un mensaje en español para el usuario.
 *
 * <p>Es una {@link RuntimeException} para no obligar a cada método a declarar {@code throws}.</p>
 *
 * @see ProblemDetailLector
 */
public class ApiException extends RuntimeException {

    /** Código usado cuando no hubo respuesta HTTP (sin conexión, tiempo agotado, etc.). */
    public static final int SIN_RESPUESTA = 0;

    private final int status;
    private final Map<String, String> erroresPorCampo;
    private final Map<String, String> datos;

    /**
     * @param datos propiedades extra del ProblemDetail (por ejemplo {@code clientesAsignados} en un 409)
     */
    public ApiException(int status, String mensaje, Map<String, String> erroresPorCampo, Map<String, String> datos) {
        super(mensaje);
        this.status = status;
        this.erroresPorCampo = Map.copyOf(erroresPorCampo);
        this.datos = Map.copyOf(datos);
    }

    public ApiException(int status, String mensaje, Map<String, String> erroresPorCampo) {
        this(status, mensaje, erroresPorCampo, Map.of());
    }

    public ApiException(int status, String mensaje) {
        this(status, mensaje, Map.of());
    }

    /** Código HTTP (400, 404, 409...) o {@link #SIN_RESPUESTA}. */
    public int status() {
        return status;
    }

    /** Errores de validación por campo (por ejemplo "email" -> "ya está registrado"). */
    public Map<String, String> erroresPorCampo() {
        return erroresPorCampo;
    }

    /** Dato extra que mandó la API con el error, como texto (vacío si no vino). */
    public Optional<String> dato(String nombre) {
        return Optional.ofNullable(datos.get(nombre));
    }
}
