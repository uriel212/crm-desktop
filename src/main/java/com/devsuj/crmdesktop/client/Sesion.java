package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.LoginResponse;
import com.devsuj.crmdesktop.model.Usuario;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Sesión del usuario que inició sesión: guarda el token JWT y los datos del usuario.
 *
 * <p>Hay UNA sola sesión en la app (la crea {@code config.Dependencias}). {@link ApiHttp} le pide el
 * token para el header {@code Authorization} y le avisa cuando la API responde 401; la ventana
 * principal escucha {@link #alCerrar(Consumer)} para volver al login.</p>
 *
 * <p>Es segura entre hilos: las peticiones HTTP corren en segundo plano y la interfaz en el hilo de
 * JavaFX. No depende de JavaFX, así que se puede probar con JUnit.</p>
 */
public class Sesion {

    /** Por qué terminó la sesión (para decidir qué mensaje mostrar en el login). */
    public enum MotivoCierre {
        /** El usuario pulsó "Cerrar sesión". */
        CERRADA_POR_USUARIO,
        /** El token venció o la API lo rechazó (401). */
        EXPIRADA
    }

    private final Clock reloj;
    private final List<Consumer<MotivoCierre>> oyentes = new CopyOnWriteArrayList<>();
    private String token;
    private Instant expiraEn;
    private Usuario usuario;

    public Sesion() {
        this(Clock.systemUTC());
    }

    /** @param reloj se puede cambiar en las pruebas para simular que pasó el tiempo */
    public Sesion(Clock reloj) {
        this.reloj = reloj;
    }

    public synchronized void iniciar(LoginResponse login) {
        this.token = login.token();
        this.expiraEn = login.expiraEn();
        this.usuario = login.usuario();
    }

    /** El token si sigue vigente; vacío si no hay sesión o ya venció. */
    public synchronized Optional<String> token() {
        boolean vencido = expiraEn != null && !reloj.instant().isBefore(expiraEn);
        return token == null || vencido ? Optional.empty() : Optional.of(token);
    }

    /** @return el usuario actual, o {@code null} si no hay sesión */
    public synchronized Usuario usuario() {
        return usuario;
    }

    public synchronized boolean iniciada() {
        return usuario != null;
    }

    /** Cierre pedido por el usuario. */
    public void cerrar() {
        terminar(MotivoCierre.CERRADA_POR_USUARIO);
    }

    /** La API rechazó el token (401). Avisa una sola vez aunque fallen varias peticiones a la vez. */
    public void expirar() {
        terminar(MotivoCierre.EXPIRADA);
    }

    /** Registra quién debe enterarse cuando la sesión termina (la ventana, para volver al login). */
    public void alCerrar(Consumer<MotivoCierre> oyente) {
        oyentes.add(oyente);
    }

    private void terminar(MotivoCierre motivo) {
        synchronized (this) {
            if (usuario == null) {
                return; // ya estaba cerrada
            }
            token = null;
            expiraEn = null;
            usuario = null;
        }
        // Fuera del bloque synchronized: los oyentes no deben ejecutarse con el candado tomado.
        oyentes.forEach(oyente -> oyente.accept(motivo));
    }
}
