package com.devsuj.crmdesktop.view.login;

import com.devsuj.crmdesktop.client.AuthApiEnMemoria;
import com.devsuj.crmdesktop.client.ApiException;
import com.devsuj.crmdesktop.config.AppConfig;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.AuthService;
import com.devsuj.crmdesktop.service.ValidacionException;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Pantalla de inicio de sesión ({@code login-view.fxml}). Solo maneja la vista: lee el email y la
 * contraseña, muestra errores y el indicador de carga. Validar y llamar a la API lo hace
 * {@link AuthService}. Enter = Iniciar sesión.
 */
public class LoginController {

    private final AuthService authService;
    private final AppConfig config;
    private final Consumer<Usuario> alIniciarSesion;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);

    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoPassword;
    @FXML
    private Label errorEmail;
    @FXML
    private Label errorPassword;
    @FXML
    private Label mensajeGeneral;
    @FXML
    private Label aviso;
    @FXML
    private Label ayudaDemo;
    @FXML
    private Button botonEntrar;
    @FXML
    private ProgressIndicator indicadorCarga;

    /** @param alIniciarSesion qué hacer cuando el login fue correcto (abrir la ventana principal) */
    public LoginController(AuthService authService, AppConfig config, Consumer<Usuario> alIniciarSesion) {
        this.authService = authService;
        this.config = config;
        this.alIniciarSesion = alIniciarSesion;
    }

    @FXML
    private void initialize() {
        for (Label etiqueta : new Label[]{errorEmail, errorPassword, mensajeGeneral, aviso, ayudaDemo}) {
            etiqueta.visibleProperty().bind(etiqueta.textProperty().isNotEmpty());
            etiqueta.managedProperty().bind(etiqueta.visibleProperty());
        }
        campoEmail.textProperty().addListener((obs, antes, ahora) -> errorEmail.setText(""));
        campoPassword.textProperty().addListener((obs, antes, ahora) -> errorPassword.setText(""));

        botonEntrar.disableProperty().bind(cargando);
        botonEntrar.textProperty().bind(Bindings.when(cargando).then("Entrando…").otherwise("Iniciar sesión"));
        indicadorCarga.visibleProperty().bind(cargando);
        indicadorCarga.managedProperty().bind(cargando);
        campoEmail.disableProperty().bind(cargando);
        campoPassword.disableProperty().bind(cargando);

        if (config.usarDatosDePrueba()) {
            ayudaDemo.setText("Modo demostración: entra con ana.torres@crmventas.test (Admin), "
                    + "luis.ramirez@crmventas.test (Gerente) o carla.mendoza@crmventas.test (Vendedor). "
                    + "Contraseña: " + AuthApiEnMemoria.PASSWORD_DEMO);
        }
        Platform.runLater(campoEmail::requestFocus);
    }

    /** Mensaje informativo arriba del formulario (por ejemplo "Tu sesión expiró"). */
    public void mostrarAviso(String mensaje) {
        aviso.setText(mensaje == null ? "" : mensaje);
    }

    @FXML
    private void onEntrar() {
        errorEmail.setText("");
        errorPassword.setText("");
        mensajeGeneral.setText("");
        String email = campoEmail.getText();
        String password = campoPassword.getText();
        TareaEnSegundoPlano.ejecutar(
                () -> authService.iniciarSesion(email, password),
                alIniciarSesion,
                this::mostrarError,
                cargando);
    }

    private void mostrarError(Throwable error) {
        Map<String, String> porCampo = switch (error) {
            case ValidacionException e -> e.erroresPorCampo();
            case ApiException e -> e.erroresPorCampo();
            default -> Map.of();
        };
        errorEmail.setText(porCampo.getOrDefault(AuthService.EMAIL, ""));
        errorPassword.setText(porCampo.getOrDefault(AuthService.PASSWORD, ""));
        if (!(error instanceof ValidacionException)) {
            mensajeGeneral.setText(MensajesError.de(error));
            campoPassword.clear();
            campoPassword.requestFocus();
        }
    }
}
