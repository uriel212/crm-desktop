package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.config.HttpConfig;
import com.devsuj.crmdesktop.config.JsonConfig;
import com.devsuj.crmdesktop.model.LoginResponse;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba el cliente HTTP base contra un servidor HTTP real pero local (incluido en el JDK),
 * sin necesitar la API de Spring Boot.
 */
class ApiHttpTest {

    record Saludo(String mensaje) {
    }

    private HttpServer servidor;
    private ApiHttp api;
    private final Sesion sesion = new Sesion();
    private final AtomicReference<String> ultimaAutorizacion = new AtomicReference<>();
    private final AtomicReference<String> ultimaRuta = new AtomicReference<>();
    private final AtomicReference<String> ultimoCuerpo = new AtomicReference<>();

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api/saludos", intercambio -> {
            ultimaRuta.set(intercambio.getRequestURI().toString());
            ultimaAutorizacion.set(intercambio.getRequestHeaders().getFirst("Authorization"));
            ultimoCuerpo.set(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            responder(intercambio, 200, "{\"mensaje\":\"hola\",\"campoNuevo\":1}");
        });
        servidor.createContext("/api/errores", intercambio -> responder(intercambio, 400,
                "{\"status\":400,\"detail\":\"Revisa los datos\",\"errores\":{\"email\":\"es obligatorio\"}}"));
        servidor.createContext("/api/protegido", intercambio -> responder(intercambio, 401,
                "{\"status\":401,\"detail\":\"Debes iniciar sesión o tu token es inválido o expiró.\"}"));
        servidor.start();

        String baseUrl = "http://localhost:" + servidor.getAddress().getPort() + "/api";
        api = new ApiHttp(HttpConfig.crearHttpClient(), JsonConfig.crearObjectMapper(), baseUrl, sesion);
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    @Test
    void convierteLaRespuestaJsonEnUnRecordEIgnoraCamposDesconocidos() {
        Saludo saludo = api.get("/saludos", Map.of(), new TypeReference<>() {});

        assertEquals(new Saludo("hola"), saludo);
    }

    @Test
    void codificaLosParametrosYOmiteLosVacios() {
        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("q", "José Pérez");
        parametros.put("estado", null);
        parametros.put("page", 0);

        api.get("/saludos", parametros, new TypeReference<Saludo>() {});

        assertEquals("/api/saludos?q=Jos%C3%A9+P%C3%A9rez&page=0", ultimaRuta.get());
    }

    @Test
    void enviaElCuerpoComoJson() {
        api.post("/saludos", new Saludo("nuevo"), new TypeReference<Saludo>() {});

        assertEquals("{\"mensaje\":\"nuevo\"}", ultimoCuerpo.get());
    }

    @Test
    void traduceElProblemDetailAApiException() {
        ApiException error = assertThrows(ApiException.class,
                () -> api.get("/errores", Map.of(), new TypeReference<Saludo>() {}));

        assertEquals(400, error.status());
        assertEquals("Revisa los datos", error.getMessage());
        assertEquals(Map.of("email", "es obligatorio"), error.erroresPorCampo());
    }

    @Test
    void avisaCuandoNoHayServidor() {
        ApiHttp sinServidor = new ApiHttp(HttpConfig.crearHttpClient(), JsonConfig.crearObjectMapper(),
                "http://localhost:1/api", new Sesion());

        ApiException error = assertThrows(ApiException.class,
                () -> sinServidor.get("/saludos", Map.of(), new TypeReference<Saludo>() {}));

        assertEquals(ApiException.SIN_RESPUESTA, error.status());
    }

    @Test
    void sinSesionNoEnviaAuthorization() {
        api.get("/saludos", Map.of(), new TypeReference<Saludo>() {});

        assertNull(ultimaAutorizacion.get());
    }

    @Test
    void conSesionEnviaElTokenBearer() {
        sesion.iniciar(login("abc.def.ghi"));

        api.get("/saludos", Map.of(), new TypeReference<Saludo>() {});

        assertEquals("Bearer abc.def.ghi", ultimaAutorizacion.get());
    }

    @Test
    void un401ConSesionAbiertaExpiraLaSesionYAvisa() {
        sesion.iniciar(login("abc"));
        List<Sesion.MotivoCierre> avisos = new ArrayList<>();
        sesion.alCerrar(avisos::add);

        ApiException error = assertThrows(ApiException.class,
                () -> api.get("/protegido", Map.of(), new TypeReference<Saludo>() {}));

        assertEquals(401, error.status());
        assertFalse(sesion.iniciada());
        assertEquals(List.of(Sesion.MotivoCierre.EXPIRADA), avisos);
    }

    private static LoginResponse login(String token) {
        Usuario usuario = new Usuario(1, "Ana", "ana@test.com", Rol.ADMIN, null, null, true, null);
        return new LoginResponse(token, "Bearer", Instant.now().plus(1, ChronoUnit.HOURS), usuario);
    }

    private static void responder(com.sun.net.httpserver.HttpExchange intercambio, int status, String json)
            throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().add("Content-Type", "application/json");
        intercambio.sendResponseHeaders(status, bytes.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(bytes);
        }
    }
}
