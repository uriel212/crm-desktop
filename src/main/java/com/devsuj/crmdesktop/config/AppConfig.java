package com.devsuj.crmdesktop.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Configuración de la aplicación leída de {@code config.properties}.
 *
 * @param apiBaseUrl        URL base de la API REST (por ejemplo, http://localhost:8080/api)
 * @param usarDatosDePrueba si es {@code true}, la app trabaja con datos en memoria sin llamar a la API
 */
public record AppConfig(String apiBaseUrl, boolean usarDatosDePrueba) {

    private static final String ARCHIVO = "/config.properties";
    private static final String CLAVE_API_BASE_URL = "api.base-url";
    private static final String CLAVE_DATOS_DE_PRUEBA = "api.datos-de-prueba";

    public AppConfig {
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Falta la propiedad '" + CLAVE_API_BASE_URL + "' en config.properties");
        }
        // Sin "/" final, para poder concatenar rutas como apiBaseUrl + "/clientes".
        apiBaseUrl = apiBaseUrl.strip().replaceAll("/+$", "");
    }

    /** Lee config.properties desde el classpath (src/main/resources). */
    public static AppConfig cargar() {
        try (InputStream entrada = AppConfig.class.getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontró " + ARCHIVO + " en el classpath");
            }
            Properties propiedades = new Properties();
            propiedades.load(entrada);
            return desde(propiedades);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + ARCHIVO, e);
        }
    }

    /** Crea la configuración a partir de propiedades ya cargadas (útil en pruebas). */
    public static AppConfig desde(Properties propiedades) {
        return new AppConfig(
                propiedades.getProperty(CLAVE_API_BASE_URL),
                Boolean.parseBoolean(propiedades.getProperty(CLAVE_DATOS_DE_PRUEBA, "false").strip()));
    }
}
