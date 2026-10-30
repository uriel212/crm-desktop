package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.config.JsonConfig;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProblemDetailLectorTest {

    private final ObjectMapper json = JsonConfig.crearObjectMapper();

    @Test
    void usaElDetailComoMensaje() {
        String cuerpo = """
                {"type":"about:blank","title":"Conflict","status":409,
                 "detail":"Ya existe un cliente con el email ana@test.com"}""";

        ApiException error = ProblemDetailLector.aExcepcion(409, cuerpo, json);

        assertEquals(409, error.status());
        assertEquals("Ya existe un cliente con el email ana@test.com", error.getMessage());
        assertTrue(error.erroresPorCampo().isEmpty());
    }

    @Test
    void leeErroresPorCampoEnFormaDeObjeto() {
        String cuerpo = """
                {"status":400,"detail":"Datos inválidos",
                 "errores":{"email":"debe ser un email válido","nombre":"es obligatorio"}}""";

        ApiException error = ProblemDetailLector.aExcepcion(400, cuerpo, json);

        assertEquals(Map.of("email", "debe ser un email válido", "nombre", "es obligatorio"),
                error.erroresPorCampo());
    }

    @Test
    void leeErroresPorCampoEnFormaDeLista() {
        String cuerpo = """
                {"status":400,"errors":[{"field":"email","message":"formato inválido"}]}""";

        ApiException error = ProblemDetailLector.aExcepcion(400, cuerpo, json);

        assertEquals(Map.of("email", "formato inválido"), error.erroresPorCampo());
        assertEquals(ProblemDetailLector.mensajePorDefecto(400), error.getMessage());
    }

    @Test
    void usaUnMensajeEnEspanolSiElCuerpoNoEsJson() {
        ApiException error = ProblemDetailLector.aExcepcion(500, "<html>Error</html>", json);

        assertEquals("El servidor tuvo un problema. Intenta de nuevo más tarde.", error.getMessage());
    }

    @Test
    void entiendeElErrorPorDefectoDeSpring() {
        // Es lo que responde hoy crm-api cuando una ruta no existe.
        String cuerpo = """
                {"timestamp":"2026-10-21T01:53:15.833Z","status":404,"error":"Not Found","path":"/api/clientes"}""";

        ApiException error = ProblemDetailLector.aExcepcion(404, cuerpo, json);

        assertEquals(ProblemDetailLector.mensajePorDefecto(404), error.getMessage());
    }
}
