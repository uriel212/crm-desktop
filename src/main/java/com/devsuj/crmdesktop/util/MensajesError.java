package com.devsuj.crmdesktop.util;

import com.devsuj.crmdesktop.client.ApiException;
import com.devsuj.crmdesktop.service.ValidacionException;

import java.lang.System.Logger.Level;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Convierte cualquier error en un mensaje en español apto para el usuario.
 * Nunca se muestra un stack trace en pantalla: los errores inesperados se registran en el log.
 */
public final class MensajesError {

    private static final System.Logger LOG = System.getLogger(MensajesError.class.getName());

    private MensajesError() {
    }

    public static String de(Throwable error) {
        Throwable causa = desenvolver(error);
        if (causa instanceof ApiException || causa instanceof ValidacionException
                || causa instanceof IllegalStateException) {
            return causa.getMessage();
        }
        LOG.log(Level.ERROR, "Error inesperado", causa);
        return "Ocurrió un error inesperado. Intenta de nuevo.";
    }

    private static Throwable desenvolver(Throwable error) {
        Throwable actual = error;
        while ((actual instanceof CompletionException || actual instanceof ExecutionException)
                && actual.getCause() != null) {
            actual = actual.getCause();
        }
        return actual;
    }
}
