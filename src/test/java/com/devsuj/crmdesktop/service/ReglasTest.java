package com.devsuj.crmdesktop.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasTest {

    @Test
    void leeMontosConComasYPrefijoQ() {
        assertEquals(new BigDecimal("1250.50"), Reglas.monto("Q 1,250.50").orElseThrow());
        assertTrue(Reglas.monto("abc").isEmpty());
    }

    @Test
    void validaMontos() {
        assertTrue(Reglas.montoValido("100.25", "falta", 10).isEmpty());
        assertEquals("Máximo 2 decimales.", Reglas.montoValido("1.234", "falta", 10).orElseThrow());
        assertEquals("No puede ser negativo.", Reglas.montoValido("-5", "falta", 10).orElseThrow());
        assertEquals("falta", Reglas.montoValido(" ", "falta", 10).orElseThrow());
    }

    @Test
    void validaEnteros() {
        assertTrue(Reglas.enteroValido("", false, 1, "falta").isEmpty());
        assertEquals("falta", Reglas.enteroValido("", true, 1, "falta").orElseThrow());
        assertTrue(Reglas.enteroValido("2.5", true, 1, "falta").isPresent());
        assertTrue(Reglas.enteroValido("0", true, 1, "falta").isPresent());
    }
}
