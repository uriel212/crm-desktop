package com.devsuj.crmdesktop.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorClienteTest {

    @Test
    void unFormularioCorrectoNoTieneErrores() {
        var datos = new ClienteFormulario("Ana", null, "ana@empresa.com", "5512345678", null, null);
        assertTrue(ValidadorCliente.validar(datos).isEmpty());
    }

    @Test
    void nombreYEmailSonObligatorios() {
        Map<String, String> errores = ValidadorCliente.validar(new ClienteFormulario(" ", "", "", "", "", null));

        assertEquals(2, errores.size());
        assertTrue(errores.containsKey(ValidadorCliente.NOMBRE));
        assertTrue(errores.containsKey(ValidadorCliente.EMAIL));
    }

    @Test
    void rechazaUnEmailMalEscrito() {
        assertTrue(ValidadorCliente.email("ana@empresa").isPresent());
        assertTrue(ValidadorCliente.email("ana empresa.com").isPresent());
        assertTrue(ValidadorCliente.email("ana@empresa.com").isEmpty());
    }

    @Test
    void respetaLasLongitudesMaximasDeLaApi() {
        assertTrue(ValidadorCliente.nombre("a".repeat(101)).isPresent());
        assertTrue(ValidadorCliente.telefono("1".repeat(21)).isPresent());
        assertTrue(ValidadorCliente.telefono("1".repeat(20)).isEmpty());
        assertTrue(ValidadorCliente.direccion("d".repeat(256)).isPresent());
    }
}
