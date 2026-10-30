package com.devsuj.crmdesktop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatos comunes para mostrar datos en pantalla (moneda, fechas, números). Un solo lugar para
 * que toda la app muestre los valores igual (DRY).
 *
 * <p>El CRM trabaja en Guatemala: montos en quetzales ("Q1,234.50") y fechas en la zona
 * {@code America/Guatemala}, la misma que usa la API.</p>
 */
public final class Formatos {

    /** Español de Guatemala: "Q1,234.50", fechas "20/10/2026". */
    public static final Locale GUATEMALA = Locale.forLanguageTag("es-GT");
    /** Zona horaria de la API. Las fechas que llegan sin zona (LocalDateTime) ya están en esta hora. */
    public static final ZoneId ZONA = ZoneId.of("America/Guatemala");

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", GUATEMALA);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", GUATEMALA);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", GUATEMALA);

    private Formatos() {
    }

    public static String moneda(BigDecimal monto) {
        if (monto == null) {
            return "";
        }
        // NumberFormat no es seguro entre hilos, por eso se crea uno en cada llamada.
        return NumberFormat.getCurrencyInstance(GUATEMALA).format(monto);
    }

    /** Número con hasta 2 decimales: 71.43, 12.5, 3. */
    public static String decimal(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        NumberFormat formato = NumberFormat.getNumberInstance(GUATEMALA);
        formato.setMinimumFractionDigits(0);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

    /** Porcentaje que llega de la API como 71.43 → "71.43 %". */
    public static String porcentaje(BigDecimal valor) {
        return valor == null ? "" : decimal(valor) + " %";
    }

    /** Monto abreviado para ejes de gráficas: Q1.2 M, Q85 k, Q900. */
    public static String monedaCorta(double monto) {
        double absoluto = Math.abs(monto);
        if (absoluto >= 1_000_000) {
            return "Q" + String.format(GUATEMALA, "%.1f M", monto / 1_000_000);
        }
        if (absoluto >= 1_000) {
            return "Q" + String.format(GUATEMALA, "%.0f k", monto / 1_000);
        }
        return "Q" + String.format(GUATEMALA, "%.0f", monto);
    }

    /** Número entero con separador de miles: 12,500. */
    public static String entero(Object numero) {
        return numero == null ? "" : NumberFormat.getIntegerInstance(GUATEMALA).format(numero);
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora == null ? "" : FECHA_HORA.format(fechaHora);
    }

    /** Un instante (por ejemplo, el vencimiento del token, que llega en UTC) en hora de Guatemala. */
    public static String fechaHora(Instant instante) {
        return instante == null ? "" : FECHA_HORA.format(instante.atZone(ZONA));
    }

    public static String hora(LocalDateTime fechaHora) {
        return fechaHora == null ? "" : HORA.format(fechaHora);
    }

    /** Fecha actual en Guatemala (no la de la computadora, que podría estar en otra zona). */
    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    /** Muestra un guion para los datos opcionales vacíos (por ejemplo, un cliente sin empresa). */
    public static String textoOGuion(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }
}
