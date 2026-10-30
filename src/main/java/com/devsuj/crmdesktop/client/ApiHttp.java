package com.devsuj.crmdesktop.client;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Cliente HTTP base. Todos los clientes por recurso ({@code ClienteApiHttp}, más adelante
 * {@code VentaApiHttp}, etc.) lo reutilizan, así la URL, los headers, el JSON y el manejo de
 * errores se escriben una sola vez (DRY).
 *
 * <p>Si hay sesión, agrega {@code Authorization: Bearer <token>} a cada petición. Si la API responde
 * 401 con una sesión abierta, avisa a {@link Sesion#expirar()} para volver al login.</p>
 *
 * <p>Los métodos son <b>bloqueantes</b> a propósito: son fáciles de leer y de probar. Por eso
 * nunca se llaman desde el hilo de JavaFX, sino desde una tarea en segundo plano
 * ({@code util.TareaEnSegundoPlano}).</p>
 */
public class ApiHttp {

    private static final Duration TIEMPO_MAXIMO_RESPUESTA = Duration.ofSeconds(15);
    private static final String JSON = "application/json";

    private final HttpClient http;
    private final ObjectMapper json;
    private final String baseUrl;
    private final Sesion sesion;

    /** @param sesion de donde sale el token JWT; se le avisa cuando la API responde 401 */
    public ApiHttp(HttpClient http, ObjectMapper json, String baseUrl, Sesion sesion) {
        this.http = http;
        this.json = json;
        this.baseUrl = baseUrl;
        this.sesion = sesion;
    }

    public <T> T get(String ruta, Map<String, ?> parametros, TypeReference<T> tipoRespuesta) {
        return enviar(peticion(ruta, parametros).GET(), tipoRespuesta);
    }

    public <T> T post(String ruta, Object cuerpo, TypeReference<T> tipoRespuesta) {
        return enviar(peticion(ruta, Map.of()).POST(conCuerpo(cuerpo)), tipoRespuesta);
    }

    public <T> T put(String ruta, Object cuerpo, TypeReference<T> tipoRespuesta) {
        return enviar(peticion(ruta, Map.of()).PUT(conCuerpo(cuerpo)), tipoRespuesta);
    }

    public <T> T patch(String ruta, Object cuerpo, TypeReference<T> tipoRespuesta) {
        return enviar(peticion(ruta, Map.of()).method("PATCH", conCuerpo(cuerpo)), tipoRespuesta);
    }

    public void delete(String ruta) {
        enviar(peticion(ruta, Map.of()).DELETE(), null);
    }

    private HttpRequest.Builder peticion(String ruta, Map<String, ?> parametros) {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(baseUrl + ruta + queryString(parametros)))
                .timeout(TIEMPO_MAXIMO_RESPUESTA)
                .header("Accept", JSON)
                .header("Content-Type", JSON);
        // El token se agrega aquí, en un solo lugar, para TODAS las peticiones (DRY).
        sesion.token().ifPresent(token -> peticion.header("Authorization", "Bearer " + token));
        return peticion;
    }

    /** Arma "?q=ana&page=0" ignorando los parámetros vacíos y codificando acentos y espacios. */
    static String queryString(Map<String, ?> parametros) {
        StringJoiner query = new StringJoiner("&", "?", "").setEmptyValue("");
        parametros.forEach((nombre, valor) -> {
            if (valor != null && !valor.toString().isBlank()) {
                query.add(nombre + "=" + URLEncoder.encode(valor.toString(), StandardCharsets.UTF_8));
            }
        });
        return query.toString();
    }

    private HttpRequest.BodyPublisher conCuerpo(Object cuerpo) {
        return HttpRequest.BodyPublishers.ofString(json.writeValueAsString(cuerpo), StandardCharsets.UTF_8);
    }

    private <T> T enviar(HttpRequest.Builder peticion, TypeReference<T> tipoRespuesta) {
        try {
            HttpResponse<String> respuesta =
                    http.send(peticion.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = respuesta.statusCode();
            String cuerpo = respuesta.body();

            if (status == 401 && sesion.iniciada()) {
                // Token vencido o inválido: la sesión termina y la ventana vuelve al login.
                // (Si no había sesión, el 401 es del login: "email o contraseña incorrectos".)
                sesion.expirar();
            }
            if (status < 200 || status >= 300) {
                throw ProblemDetailLector.aExcepcion(status, cuerpo, json);
            }
            if (tipoRespuesta == null || cuerpo == null || cuerpo.isBlank()) {
                return null; // por ejemplo, 204 No Content
            }
            return json.readValue(cuerpo, tipoRespuesta);
        } catch (HttpTimeoutException e) {
            throw new ApiException(ApiException.SIN_RESPUESTA,
                    "El servidor tardó demasiado en responder. Intenta de nuevo.");
        } catch (IOException e) {
            // Conexión rechazada, servidor apagado, red caída...
            throw sinConexion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ApiException.SIN_RESPUESTA, "La operación fue cancelada.");
        } catch (JacksonException e) {
            throw new ApiException(ApiException.SIN_RESPUESTA,
                    "La respuesta del servidor no tiene el formato esperado.");
        }
    }

    private ApiException sinConexion() {
        return new ApiException(ApiException.SIN_RESPUESTA,
                "No se pudo conectar con el servidor (" + baseUrl + "). Verifica que la API esté encendida.");
    }
}
