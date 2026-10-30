package com.devsuj.crmdesktop.config;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Crea el {@link HttpClient} que comparte toda la aplicación.
 *
 * <p>Un {@code HttpClient} reutiliza conexiones y hilos internamente, por eso se crea
 * uno solo y no uno por petición.</p>
 */
public final class HttpConfig {

    private static final Duration TIEMPO_MAXIMO_CONEXION = Duration.ofSeconds(5);

    private HttpConfig() {
    }

    public static HttpClient crearHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(TIEMPO_MAXIMO_CONEXION)
                .build();
    }
}
