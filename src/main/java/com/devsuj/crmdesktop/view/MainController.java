package com.devsuj.crmdesktop.view;

import com.devsuj.crmdesktop.config.AppConfig;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.AuthService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.view.usuarios.PasswordDialogo;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.Navegador;
import com.devsuj.crmdesktop.util.Tema;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Set;

/**
 * Controlador de la ventana principal: barra superior, menú lateral y área de contenido.
 *
 * <p>El menú se construye a partir de la lista {@link #MODULOS}: para agregar un módulo nuevo
 * basta con agregar una línea (abierto a extensión, cerrado a modificación).</p>
 */
public class MainController {

    private static final String PANTALLA_INICIO = "view/tablero/tablero-view.fxml";

    private static final List<Modulo> MODULOS = List.of(
            new Modulo("Tablero", "fth-home", PANTALLA_INICIO),
            new Modulo("Clientes", "fth-users", "view/clientes/clientes-view.fxml"),
            new Modulo("Interacciones", "fth-message-square", "view/interacciones/interacciones-view.fxml"),
            new Modulo("Oportunidades", "fth-target", "view/oportunidades/oportunidades-view.fxml"),
            new Modulo("Ventas", "fth-shopping-cart", "view/ventas/ventas-view.fxml"),
            new Modulo("Productos", "fth-package", "view/productos/productos-view.fxml"),
            new Modulo("Campañas", "fth-send", "view/campanas/campanas-view.fxml"),
            new Modulo("Promociones", "fth-tag", "view/promociones/promociones-view.fxml"),
            new Modulo("Fidelización", "fth-gift", "view/fidelizacion/fidelizacion-view.fxml"),
            new Modulo("Contratos", "fth-file-text", "view/contratos/contratos-view.fxml"),
            new Modulo("Soporte", "fth-life-buoy", "view/soporte/soporte-view.fxml"),
            new Modulo("Metas y rendimiento", "fth-trending-up", "view/metas/metas-view.fxml"),
            new Modulo("Usuarios", "fth-user-check", "view/usuarios/usuarios-view.fxml", Permisos::puedeVerUsuarios));

    /** Módulos que funcionan sin API (modo demostración). */
    private static final Set<String> MODULOS_DEMO = Set.of("Clientes", "Usuarios");

    private final AppConfig config;
    private final Tema tema;
    private final Permisos permisos;
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final Callback<Class<?>, Object> fabricaDeControladores;
    private final ToggleGroup grupoMenu = new ToggleGroup();
    private Navegador navegador;

    @FXML
    private Label apiUrlLabel;
    @FXML
    private ToggleButton botonTema;
    @FXML
    private MenuButton menuUsuario;
    @FXML
    private VBox menu;
    @FXML
    private StackPane contenido;

    public MainController(AppConfig config, Tema tema, Permisos permisos, AuthService authService,
                          UsuarioService usuarioService, Callback<Class<?>, Object> fabricaDeControladores) {
        this.config = config;
        this.tema = tema;
        this.permisos = permisos;
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.fabricaDeControladores = fabricaDeControladores;
    }

    @FXML
    private void initialize() {
        navegador = new Navegador(contenido, fabricaDeControladores);
        apiUrlLabel.setText(config.usarDatosDePrueba()
                ? "Modo demostración (datos de prueba, sin API)"
                : "API: " + config.apiBaseUrl());

        configurarBotonTema();
        configurarMenuUsuario();
        // Cada rol ve solo los módulos que puede usar. En modo demostración, solo los que tienen datos en memoria.
        MODULOS.stream()
                .filter(modulo -> modulo.visibleSi().test(permisos))
                .filter(modulo -> !config.usarDatosDePrueba() || MODULOS_DEMO.contains(modulo.nombre()))
                .forEach(modulo -> menu.getChildren().add(crearBotonMenu(modulo)));

        // Evita que el usuario "deseleccione" el módulo actual al volver a pulsarlo.
        grupoMenu.selectedToggleProperty().addListener((obs, anterior, actual) -> {
            if (actual == null) {
                grupoMenu.selectToggle(anterior);
            }
        });
        grupoMenu.selectToggle(grupoMenu.getToggles().getFirst());
    }

    private void configurarBotonTema() {
        // El botón y el tema quedan enlazados en ambos sentidos: si uno cambia, el otro también.
        botonTema.selectedProperty().bindBidirectional(tema.oscuroProperty());
        FontIcon luna = new FontIcon("fth-moon");
        FontIcon sol = new FontIcon("fth-sun");
        botonTema.graphicProperty().bind(Bindings
                .when(botonTema.selectedProperty()).then(sol).otherwise(luna));
        botonTema.setTooltip(new Tooltip("Cambiar entre modo claro y oscuro"));
    }

    private void configurarMenuUsuario() {
        Usuario usuario = permisos.usuario();
        menuUsuario.setText(usuario.nombre() + " · " + usuario.rol().etiqueta());
        menuUsuario.setGraphic(new FontIcon("fth-user"));
        menuUsuario.setTooltip(new Tooltip(usuario.email()));
    }

    @FXML
    private void onCambiarPassword() {
        if (PasswordDialogo.mostrar(menu.getScene().getWindow(), usuarioService, permisos.usuario(), true)) {
            Notificaciones.exito(menu, "Tu contraseña se cambió correctamente.");
        }
    }

    @FXML
    private void onCerrarSesion() {
        // La ventana escucha la sesión y vuelve al login (ver VentanaPrincipal).
        authService.cerrarSesion();
    }

    private ToggleButton crearBotonMenu(Modulo modulo) {
        ToggleButton boton = new ToggleButton(modulo.nombre(), new FontIcon(modulo.icono()));
        boton.setToggleGroup(grupoMenu);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(Pos.CENTER_LEFT);
        boton.getStyleClass().addAll("boton-menu", "flat");
        if (!modulo.disponible()) {
            boton.setTooltip(new Tooltip("Próximamente"));
        }
        boton.selectedProperty().addListener((obs, antes, seleccionado) -> {
            if (seleccionado) {
                abrir(modulo);
            }
        });
        return boton;
    }

    private void abrir(Modulo modulo) {
        if (modulo.disponible()) {
            navegador.mostrar(modulo.rutaFxml());
        } else {
            navegador.mostrar(EstadoVacio.crear(modulo.icono(),
                    "El módulo " + modulo.nombre() + " estará disponible próximamente.", null, null));
        }
    }
}
