package com.devsuj.crmdesktop;

import com.devsuj.crmdesktop.config.AppConfig;
import com.devsuj.crmdesktop.config.Dependencias;
import com.devsuj.crmdesktop.util.Formatos;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Locale;

/**
 * Aplicación JavaFX: arma las dependencias, aplica el tema y abre la ventana en la pantalla de login.
 */
public class CrmApplication extends Application {

    public static void main(String[] args) {
        // Idioma y formato regional de Guatemala para los controles de JavaFX (DatePicker: dd/MM/yyyy,
        // botones de los diálogos en español).
        Locale.setDefault(Formatos.GUATEMALA);
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Dependencias dependencias = new Dependencias(AppConfig.cargar());
        dependencias.tema().aplicar();
        dependencias.ventanaPrincipal().abrir(stage);
    }
}
