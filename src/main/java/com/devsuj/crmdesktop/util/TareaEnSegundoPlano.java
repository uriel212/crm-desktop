package com.devsuj.crmdesktop.util;

import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Ejecuta trabajo lento (llamadas HTTP) fuera del hilo de JavaFX para que la ventana no se congele.
 *
 * <p>Regla de oro de JavaFX: la interfaz solo se modifica desde el hilo de JavaFX. Un {@link Task}
 * ejecuta {@code call()} en otro hilo y llama a {@code setOnSucceeded}/{@code setOnFailed} de
 * vuelta en el hilo de JavaFX, así que dentro de {@code alTerminar} y {@code alFallar} se puede
 * tocar la interfaz sin {@code Platform.runLater}.</p>
 */
public final class TareaEnSegundoPlano {

    /** Hilos "daemon": no impiden que la aplicación se cierre. */
    private static final ExecutorService HILOS = Executors.newCachedThreadPool(tarea -> {
        Thread hilo = new Thread(tarea, "crm-segundo-plano");
        hilo.setDaemon(true);
        return hilo;
    });

    private TareaEnSegundoPlano() {
    }

    /**
     * @param trabajo    lo que se hace en segundo plano (por ejemplo, {@code () -> service.buscar(...)})
     * @param alTerminar qué hacer con el resultado (en el hilo de JavaFX)
     * @param alFallar   qué hacer si hubo un error (en el hilo de JavaFX)
     * @param cargando   propiedad que vale {@code true} mientras se trabaja (para el indicador de carga); puede ser null
     */
    public static <T> void ejecutar(Callable<T> trabajo, Consumer<T> alTerminar,
                                    Consumer<Throwable> alFallar, BooleanProperty cargando) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabajo.call();
            }
        };
        if (cargando != null) {
            cargando.set(true);
        }
        tarea.setOnSucceeded(evento -> {
            terminar(cargando);
            alTerminar.accept(tarea.getValue());
        });
        tarea.setOnFailed(evento -> {
            terminar(cargando);
            alFallar.accept(tarea.getException());
        });
        HILOS.execute(tarea);
    }

    private static void terminar(BooleanProperty cargando) {
        if (cargando != null) {
            cargando.set(false);
        }
    }
}
