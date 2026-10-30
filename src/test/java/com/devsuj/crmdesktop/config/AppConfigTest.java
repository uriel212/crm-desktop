package com.devsuj.crmdesktop.config;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @Test
    void cargaLaUrlDesdeConfigProperties() {
        assertTrue(AppConfig.cargar().apiBaseUrl().startsWith("http"));
    }

    @Test
    void laUrlForzadaTienePrioridadSobreElArchivo() {
        Properties propiedades = new Properties();
        propiedades.setProperty("api.base-url", "https://api.nube.test/api");

        assertEquals("http://localhost:8080/api",
                AppConfig.desde(propiedades, "http://localhost:8080/api").apiBaseUrl());
    }

    @Test
    void sinUrlForzadaSeUsaLaDelArchivo() {
        Properties propiedades = new Properties();
        propiedades.setProperty("api.base-url", "https://api.nube.test/api");

        assertEquals("https://api.nube.test/api", AppConfig.desde(propiedades, " ").apiBaseUrl());
    }

    @Test
    void quitaLaDiagonalFinalDeLaUrl() {
        Properties propiedades = new Properties();
        propiedades.setProperty("api.base-url", "http://localhost:8080/api/");

        assertEquals("http://localhost:8080/api", AppConfig.desde(propiedades).apiBaseUrl());
    }

    @Test
    void fallaSiFaltaLaUrl() {
        assertThrows(IllegalStateException.class, () -> AppConfig.desde(new Properties()));
    }

    @Test
    void losDatosDePruebaEstanApagadosSiNoSeIndica() {
        Properties propiedades = new Properties();
        propiedades.setProperty("api.base-url", "http://localhost:8080/api");

        assertFalse(AppConfig.desde(propiedades).usarDatosDePrueba());
    }

    @Test
    void leeElModoDeDatosDePrueba() {
        Properties propiedades = new Properties();
        propiedades.setProperty("api.base-url", "http://localhost:8080/api");
        propiedades.setProperty("api.datos-de-prueba", "true");

        assertTrue(AppConfig.desde(propiedades).usarDatosDePrueba());
    }
}
