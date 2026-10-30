package com.devsuj.crmdesktop.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatosTest {

    @Test
    void formateaMonedaEnQuetzales() {
        assertEquals("Q1,234.50", Formatos.moneda(new BigDecimal("1234.5")));
    }

    @Test
    void muestraLosInstantesEnHoraDeGuatemala() {
        // 16:00 UTC = 10:00 en Guatemala (UTC-6, sin horario de verano)
        assertEquals("21/10/2026 10:00", Formatos.fechaHora(java.time.Instant.parse("2026-10-21T16:00:00Z")));
    }

    @Test
    void formateaDecimalesYPorcentajes() {
        assertEquals("12.5", Formatos.decimal(new BigDecimal("12.50")));
        assertEquals("71.43 %", Formatos.porcentaje(new BigDecimal("71.43")));
        assertEquals("", Formatos.decimal(null));
    }

    @Test
    void formateaEnterosConSeparadorDeMiles() {
        assertEquals("12,500", Formatos.entero(12500));
    }

    @Test
    void formateaFechas() {
        assertEquals("05/03/2026", Formatos.fecha(LocalDate.of(2026, 3, 5)));
        assertEquals("05/03/2026 09:07", Formatos.fechaHora(LocalDateTime.of(2026, 3, 5, 9, 7)));
    }

    @Test
    void losValoresNulosSeMuestranVacios() {
        assertEquals("", Formatos.moneda(null));
        assertEquals("", Formatos.fecha(null));
        assertEquals("—", Formatos.textoOGuion("  "));
    }
}
