package com.devsuj.crmdesktop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Reglas de validación comunes a todos los formularios (obligatorio, longitud máxima, email,
 * montos). Cada validador de módulo las combina en lugar de repetirlas (DRY).
 *
 * <p>Cada regla devuelve el mensaje de error, o vacío si el valor es correcto.</p>
 */
public final class Reglas {

    // Validación sencilla: algo@algo.algo, sin espacios.
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Reglas() {
    }

    public static Optional<String> obligatorio(String valor, String mensaje) {
        return valor == null || valor.isBlank() ? Optional.of(mensaje) : Optional.empty();
    }

    public static Optional<String> obligatorio(Object valor, String mensaje) {
        return valor == null ? Optional.of(mensaje) : Optional.empty();
    }

    public static Optional<String> maximo(String valor, int maximo) {
        return valor != null && valor.strip().length() > maximo
                ? Optional.of("Máximo " + maximo + " caracteres.")
                : Optional.empty();
    }

    public static Optional<String> email(String valor, String mensajeSiFalta) {
        return obligatorio(valor, mensajeSiFalta)
                .or(() -> FORMATO_EMAIL.matcher(valor.strip()).matches()
                        ? Optional.empty()
                        : Optional.of("Escribe un email válido, por ejemplo nombre@empresa.com."))
                .or(() -> maximo(valor, 150));
    }

    /**
     * Convierte el texto de un campo de dinero ("1,250.50" o "Q 1250.5") a {@link BigDecimal}.
     *
     * @return vacío si el texto está en blanco o no es un número
     */
    public static Optional<BigDecimal> monto(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        String limpio = texto.strip().replaceFirst("^[Qq]\\s*", "").replace(",", "");
        try {
            return Optional.of(new BigDecimal(limpio));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Monto obligatorio, mayor o igual a 0 y con máximo 2 decimales. */
    public static Optional<String> montoValido(String texto, String mensajeSiFalta, int maximoEnteros) {
        if (texto == null || texto.isBlank()) {
            return Optional.of(mensajeSiFalta);
        }
        Optional<BigDecimal> monto = monto(texto);
        if (monto.isEmpty()) {
            return Optional.of("Escribe solo números, por ejemplo 1250.50.");
        }
        BigDecimal valor = monto.get();
        if (valor.signum() < 0) {
            return Optional.of("No puede ser negativo.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            return Optional.of("Máximo 2 decimales.");
        }
        if (valor.precision() - valor.scale() > maximoEnteros) {
            return Optional.of("El monto es demasiado grande.");
        }
        return Optional.empty();
    }

    /** Número entero (vacío = sin valor). @return vacío si el texto no es un entero */
    public static Optional<Integer> entero(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(texto.strip().replace(",", "")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Entero obligatorio (si {@code obligatorio}) y mayor o igual que {@code minimo}. */
    public static Optional<String> enteroValido(String texto, boolean obligatorio, int minimo, String mensajeSiFalta) {
        if (texto == null || texto.isBlank()) {
            return obligatorio ? Optional.of(mensajeSiFalta) : Optional.empty();
        }
        Optional<Integer> numero = entero(texto);
        if (numero.isEmpty()) {
            return Optional.of("Escribe un número entero, sin decimales.");
        }
        return numero.get() < minimo ? Optional.of("Debe ser mayor o igual a " + minimo + ".") : Optional.empty();
    }

    /** Fechas obligatorias y en orden (fin igual o posterior al inicio). */
    public static void rangoFechas(Map<String, String> errores, String campoInicio, LocalDate inicio,
                                   String campoFin, LocalDate fin) {
        if (inicio == null) {
            errores.putIfAbsent(campoInicio, "Elige la fecha de inicio.");
        }
        if (fin == null) {
            errores.putIfAbsent(campoFin, "Elige la fecha de fin.");
        }
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            errores.putIfAbsent(campoFin, "La fecha de fin no puede ser anterior a la de inicio.");
        }
    }

    /** Agrega el error al mapa si la regla falla. */
    public static void revisar(Map<String, String> errores, String campo, Supplier<Optional<String>> regla) {
        regla.get().ifPresent(mensaje -> errores.putIfAbsent(campo, mensaje));
    }

    /** Lanza {@link ValidacionException} si hubo errores (así el service no llama a la API). */
    public static void exigir(Map<String, String> errores) {
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }

    /** Quita espacios sobrantes y convierte los textos vacíos en null (campo opcional sin valor). */
    public static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}
