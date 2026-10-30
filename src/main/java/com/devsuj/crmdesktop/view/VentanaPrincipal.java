package com.devsuj.crmdesktop.view;

import com.devsuj.crmdesktop.client.Sesion;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.util.Recursos;
import com.devsuj.crmdesktop.util.Vistas;
import com.devsuj.crmdesktop.view.login.LoginController;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.ArrayList;

/**
 * La ventana de la aplicación y sus dos "modos": la pantalla de login y la aplicación (menú +
 * módulos). Se usa UNA sola escena y se cambia su raíz, así la ventana conserva su tamaño.
 *
 * <p>Escucha la {@link Sesion}: cuando el usuario cierra sesión o la API rechaza el token (401),
 * vuelve al login con un aviso.</p>
 */
public class VentanaPrincipal {

    private static final double ANCHO_INICIAL = 1280;
    private static final double ALTO_INICIAL = 800;
    private static final double ANCHO_MINIMO = 1024;
    private static final double ALTO_MINIMO = 768;
    private static final String TITULO = "CRM de Ventas";

    private final Callback<Class<?>, Object> fabricaDeControladores;
    private Stage ventana;
    private Scene escena;

    public VentanaPrincipal(Sesion sesion, Callback<Class<?>, Object> fabricaDeControladores) {
        this.fabricaDeControladores = fabricaDeControladores;
        // La sesión puede terminar desde un hilo en segundo plano (un 401): runLater vuelve al hilo de JavaFX.
        sesion.alCerrar(motivo -> Platform.runLater(() -> mostrarLogin(motivo == Sesion.MotivoCierre.EXPIRADA
                ? "Tu sesión expiró o ya no es válida. Vuelve a iniciar sesión."
                : "Cerraste sesión correctamente.")));
    }

    public void abrir(Stage ventana) {
        this.ventana = ventana;
        ventana.setMinWidth(ANCHO_MINIMO);
        ventana.setMinHeight(ALTO_MINIMO);
        mostrarLogin(null);
        ventana.show();
    }

    /** @param aviso mensaje para el usuario (por ejemplo "Tu sesión expiró"); puede ser null */
    public void mostrarLogin(String aviso) {
        cerrarDialogosAbiertos();
        Vistas.Cargada<Parent> login = Vistas.cargar("view/login/login-view.fxml", fabricaDeControladores);
        ((LoginController) login.controlador()).mostrarAviso(aviso);
        poner(login.raiz());
        ventana.setTitle(TITULO + " · Iniciar sesión");
    }

    public void mostrarAplicacion(Usuario usuario) {
        poner(Vistas.cargar("view/main-view.fxml", fabricaDeControladores).raiz());
        ventana.setTitle(TITULO + " · " + usuario.nombre());
    }

    private void poner(Parent raiz) {
        if (escena == null) {
            escena = new Scene(raiz, ANCHO_INICIAL, ALTO_INICIAL);
            escena.getStylesheets().add(Recursos.url("css/styles.css").toExternalForm());
            ventana.setScene(escena);
        } else {
            escena.setRoot(raiz);
        }
    }

    /** Si la sesión expira con un formulario abierto, se cierra: sus datos ya no se podrían guardar. */
    private void cerrarDialogosAbiertos() {
        new ArrayList<>(Window.getWindows()).stream()
                .filter(otra -> otra != ventana)
                .forEach(Window::hide);
    }
}
