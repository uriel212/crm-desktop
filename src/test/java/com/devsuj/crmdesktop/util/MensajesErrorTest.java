package com.devsuj.crmdesktop.util;

import com.devsuj.crmdesktop.client.ApiException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MensajesErrorTest {

    @Test
    void usaElMensajeDeLaApi() {
        assertEquals("Email duplicado", MensajesError.de(new ApiException(409, "Email duplicado")));
    }

    @Test
    void desenvuelveErroresAnidados() {
        Throwable anidado = new CompletionException(new ApiException(404, "No existe"));
        assertEquals("No existe", MensajesError.de(anidado));
    }

    @Test
    void ocultaLosDetallesDeErroresInesperados() {
        assertEquals("Ocurrió un error inesperado. Intenta de nuevo.",
                MensajesError.de(new NullPointerException("x es null")));
    }
}
