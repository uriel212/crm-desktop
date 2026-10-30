package com.devsuj.crmdesktop.config;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Crea el {@link ObjectMapper} (Jackson 3) que comparte toda la aplicación.
 *
 * <p>Jackson 3 ya entiende {@code LocalDate}/{@code LocalDateTime} (java.time) y las escribe
 * como texto ISO ("2026-10-20T10:00:00"), igual que la API de Spring Boot 4.</p>
 */
public final class JsonConfig {

    private JsonConfig() {
    }

    public static ObjectMapper crearObjectMapper() {
        return JsonMapper.builder()
                // Si la API agrega un campo nuevo, la app no se rompe: simplemente lo ignora.
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}
