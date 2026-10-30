package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.config.HttpConfig;
import com.devsuj.crmdesktop.config.JsonConfig;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.ClienteRequest;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Pagina;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Comprueba que {@link ClienteApiHttp} usa las rutas, métodos y JSON del contrato de la API
 * (plan del backend, sección 1.B) contra un servidor HTTP local de prueba.
 */
class ClienteApiHttpTest {

    private static final String CLIENTE_JSON = """
            {"id":1,"nombre":"Roberto Garza","empresa":"Grupo Industrial del Norte",
             "email":"rgarza@gin.test","telefono":"8112345678","direccion":"Monterrey",
             "estado":"ACTIVO","vendedorId":3,"vendedorNombre":"Carla Mendoza",
             "creadoEn":"2025-11-03T10:00:00","actualizadoEn":"2025-11-03T10:00:00"}""";

    private HttpServer servidor;
    private ClienteApiHttp api;
    private final List<String> peticiones = new ArrayList<>();
    private final List<String> cuerpos = new ArrayList<>();

    @BeforeEach
    void iniciar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/api/clientes", intercambio -> {
            peticiones.add(intercambio.getRequestMethod() + " " + intercambio.getRequestURI());
            cuerpos.add(new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String respuesta = intercambio.getRequestURI().getPath().equals("/api/clientes")
                    && intercambio.getRequestMethod().equals("GET")
                    ? "{\"contenido\":[" + CLIENTE_JSON + "],\"pagina\":0,\"tamano\":20,"
                      + "\"totalElementos\":1,\"totalPaginas\":1}"
                    : CLIENTE_JSON;
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.sendResponseHeaders(intercambio.getRequestMethod().equals("POST") ? 201 : 200, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();
        String baseUrl = "http://localhost:" + servidor.getAddress().getPort() + "/api";
        api = new ClienteApiHttp(new ApiHttp(HttpConfig.crearHttpClient(), JsonConfig.crearObjectMapper(), baseUrl, new Sesion()));
    }

    @AfterEach
    void detener() {
        servidor.stop(0);
    }

    @Test
    void buscarUsaLosParametrosDelContrato() {
        Pagina<Cliente> pagina = api.buscar("garza", EstadoCliente.ACTIVO, 3, 0, 20);

        assertEquals("GET /api/clientes?q=garza&estado=ACTIVO&vendedorId=3&page=0&size=20&sort=nombre", peticiones.getFirst());
        assertEquals(1, pagina.totalElementos());
        Cliente cliente = pagina.contenido().getFirst();
        assertEquals(EstadoCliente.ACTIVO, cliente.estado());
        assertEquals(LocalDateTime.of(2025, 11, 3, 10, 0), cliente.creadoEn());
    }

    @Test
    void crearActualizarYCambiarEstadoUsanLosMetodosCorrectos() {
        ClienteRequest request = new ClienteRequest("Ana", null, "ana@test.com", null, null, null);

        api.crear(request);
        api.actualizar(1, request);
        api.cambiarEstado(1, EstadoCliente.INACTIVO);

        assertEquals(List.of("POST /api/clientes", "PUT /api/clientes/1", "PATCH /api/clientes/1/estado"),
                peticiones);
        assertEquals("{\"estado\":\"INACTIVO\"}", cuerpos.get(2));
    }
}
