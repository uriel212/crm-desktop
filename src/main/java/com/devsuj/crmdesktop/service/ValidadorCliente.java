package com.devsuj.crmdesktop.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reglas de validación de un cliente. Son las mismas que aplica la API en {@code ClienteRequest}
 * (plan del backend 1.11), para avisar al usuario antes de enviar.
 *
 * <p>Cada regla es un método que devuelve el mensaje de error, o vacío si el valor es correcto.
 * El formulario las usa campo por campo (con ControlsFX) y el service todas juntas (DRY).</p>
 */
public final class ValidadorCliente {

    public static final String NOMBRE = "nombre";
    public static final String EMPRESA = "empresa";
    public static final String EMAIL = "email";
    public static final String TELEFONO = "telefono";
    public static final String DIRECCION = "direccion";

    private ValidadorCliente() {
    }

    public static Optional<String> nombre(String valor) {
        return obligatorio(valor, "Escribe el nombre del cliente.")
                .or(() -> maximo(valor, 100));
    }

    public static Optional<String> empresa(String valor) {
        return maximo(valor, 100);
    }

    public static Optional<String> email(String valor) {
        return Reglas.email(valor, "Escribe el email del cliente.");
    }

    public static Optional<String> telefono(String valor) {
        return maximo(valor, 20);
    }

    public static Optional<String> direccion(String valor) {
        return maximo(valor, 255);
    }

    /** Valida todo el formulario. @return campo -> mensaje; vacío si no hay errores */
    public static Map<String, String> validar(ClienteFormulario datos) {
        Map<String, String> errores = new LinkedHashMap<>();
        nombre(datos.nombre()).ifPresent(m -> errores.put(NOMBRE, m));
        empresa(datos.empresa()).ifPresent(m -> errores.put(EMPRESA, m));
        email(datos.email()).ifPresent(m -> errores.put(EMAIL, m));
        telefono(datos.telefono()).ifPresent(m -> errores.put(TELEFONO, m));
        direccion(datos.direccion()).ifPresent(m -> errores.put(DIRECCION, m));
        return errores;
    }

    private static Optional<String> obligatorio(String valor, String mensaje) {
        return Reglas.obligatorio(valor, mensaje);
    }

    private static Optional<String> maximo(String valor, int maximo) {
        return Reglas.maximo(valor, maximo);
    }
}
