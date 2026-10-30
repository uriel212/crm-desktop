package com.devsuj.crmdesktop.view.usuarios;

import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.util.Alertas;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.Atajos;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Paginacion;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Map;

/**
 * Pantalla de usuarios y equipos. GERENTE y ADMIN la ven; solo ADMIN crea, edita, activa o
 * desactiva usuarios y cambia contraseñas.
 *
 * <p>Al desactivar un vendedor con clientes, la API responde 409 con {@code clientesAsignados}:
 * la pantalla pregunta si se reasignan a su gerente y, si el usuario acepta, repite la operación
 * confirmándolo.</p>
 */
public class UsuariosController {

    private final UsuarioService service;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(350));
    private Paginacion paginacion;
    private int ultimaSolicitud;

    @FXML
    private TextField campoBusqueda;
    @FXML
    private ComboBox<Rol> filtroRol;
    @FXML
    private ComboBox<Boolean> filtroActivo;
    @FXML
    private HBox accionesAdmin;
    @FXML
    private Label avisoSoloLectura;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonEstado;
    @FXML
    private Button botonPassword;
    @FXML
    private TableView<Usuario> tabla;
    @FXML
    private TableColumn<Usuario, String> colNombre;
    @FXML
    private TableColumn<Usuario, String> colEmail;
    @FXML
    private TableColumn<Usuario, Rol> colRol;
    @FXML
    private TableColumn<Usuario, String> colGerente;
    @FXML
    private TableColumn<Usuario, String> colEstado;
    @FXML
    private TableColumn<Usuario, String> colAlta;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;

    public UsuariosController(UsuarioService service, Permisos permisos) {
        this.service = service;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando,
                this::cargar, "usuarios");
        Tablas.ajustable(tabla);
        Tablas.texto(colNombre, Usuario::nombre);
        Tablas.texto(colEmail, Usuario::email);
        Tablas.etiqueta(colRol, Usuario::rol, "rol-");
        Tablas.texto(colGerente, Usuario::gerenteNombre);
        Tablas.texto(colEstado, u -> u.activo() ? "Activo" : "Inactivo");
        Tablas.fechaHora(colAlta, Usuario::creadoEn);

        EtiquetaListCell.filtro(filtroRol, "Todos los roles", Rol.values());
        filtroActivo.getItems().setAll(null, Boolean.TRUE, Boolean.FALSE);
        EtiquetaListCell.aplicar(filtroActivo, "Activos e inactivos", activo -> activo ? "Solo activos" : "Solo inactivos");
        filtroRol.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroActivo.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        esperaBusqueda.setOnFinished(evento -> cargar(0));
        campoBusqueda.textProperty().addListener((obs, antes, ahora) -> esperaBusqueda.playFromStart());

        configurarAcciones();
        capaCarga.visibleProperty().bind(cargando);
        cargar(0);
    }

    private void configurarAcciones() {
        boolean admin = permisos.puedeAdministrarUsuarios();
        accionesAdmin.setVisible(admin);
        accionesAdmin.setManaged(admin);
        avisoSoloLectura.setVisible(!admin);
        avisoSoloLectura.setManaged(!admin);
        if (!admin) {
            return;
        }
        BooleanBinding sinSeleccion = tabla.getSelectionModel().selectedItemProperty().isNull();
        botonEditar.disableProperty().bind(sinSeleccion.or(cargando));
        botonEstado.disableProperty().bind(sinSeleccion.or(cargando));
        botonPassword.disableProperty().bind(sinSeleccion.or(cargando));
        botonEstado.textProperty().bind(Bindings.createStringBinding(() -> {
            Usuario u = tabla.getSelectionModel().getSelectedItem();
            return u != null && !u.activo() ? "Activar" : "Desactivar";
        }, tabla.getSelectionModel().selectedItemProperty()));
        Tablas.alDobleClic(tabla, this::onEditar);
        Atajos.instalar(tabla, Map.of(
                KeyCombination.keyCombination("Shortcut+N"), this::onNuevo,
                KeyCombination.keyCombination("Shortcut+F"), campoBusqueda::requestFocus,
                new KeyCodeCombination(KeyCode.F5), this::onRecargar));
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        String texto = campoBusqueda.getText();
        Rol rol = filtroRol.getValue();
        Boolean activo = filtroActivo.getValue();
        TareaEnSegundoPlano.ejecutar(
                () -> service.buscar(texto, rol, activo, numeroPagina),
                resultado -> {
                    if (solicitud == ultimaSolicitud) {
                        mostrarPagina(resultado);
                    }
                },
                error -> {
                    if (solicitud == ultimaSolicitud) {
                        tabla.getItems().clear();
                        paginacion.limpiar();
                        tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                                "No se pudieron cargar los usuarios. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    private void mostrarPagina(Pagina<Usuario> resultado) {
        tabla.getItems().setAll(resultado.contenido());
        paginacion.mostrar(resultado);
        tabla.setPlaceholder(EstadoVacio.crear("fth-search", "No se encontraron usuarios con esos filtros.",
                "Limpiar filtros", () -> {
                    campoBusqueda.clear();
                    filtroRol.setValue(null);
                    filtroActivo.setValue(null);
                }));
    }

    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Usuario seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            abrirFormulario(seleccionado);
        }
    }

    /** Carga los gerentes (para el combo de equipo) y abre el formulario. */
    private void abrirFormulario(Usuario original) {
        TareaEnSegundoPlano.ejecutar(
                service::gerentes,
                gerentes -> UsuarioDialogo.mostrar(ventana(), service, original, gerentes).ifPresent(guardado -> {
                    Notificaciones.exito(tabla, "Usuario " + guardado.nombre()
                            + (original == null ? " creado." : " actualizado."));
                    cargar(paginacion.actual());
                }),
                error -> Notificaciones.error(tabla, MensajesError.de(error)),
                cargando);
    }

    @FXML
    private void onCambiarEstado() {
        Usuario u = tabla.getSelectionModel().getSelectedItem();
        if (u == null) {
            return;
        }
        boolean activar = !u.activo();
        if (!activar && !Alertas.confirmar(tabla, "Desactivar usuario",
                "¿Desactivar a " + u.nombre() + "?\n\nNo podrá iniciar sesión hasta que lo vuelvas a activar.",
                "Desactivar")) {
            return;
        }
        cambiarEstado(u, activar, false);
    }

    private void cambiarEstado(Usuario u, boolean activar, boolean reasignar) {
        TareaEnSegundoPlano.ejecutar(
                () -> service.cambiarEstado(u, activar, reasignar),
                actualizado -> {
                    Notificaciones.exito(tabla, actualizado.nombre() + (activar ? " activado." : " desactivado."));
                    cargar(paginacion.actual());
                },
                error -> UsuarioService.clientesAsignados(error).ifPresentOrElse(
                        clientes -> confirmarReasignacion(u, clientes),
                        () -> Alertas.error(tabla, MensajesError.de(error))),
                cargando);
    }

    /** 409 con clientesAsignados: se pregunta antes de mover sus clientes al gerente. */
    private void confirmarReasignacion(Usuario u, int clientes) {
        String destino = u.gerenteNombre() == null ? "su gerente" : u.gerenteNombre();
        boolean confirmado = Alertas.confirmar(tabla, "El vendedor tiene clientes",
                u.nombre() + " tiene " + clientes + " cliente(s) asignado(s).\n\n"
                        + "Si continúas, pasarán a " + destino + " y " + u.nombre() + " quedará desactivado.",
                "Reasignar y desactivar");
        if (confirmado) {
            cambiarEstado(u, false, true);
        }
    }

    @FXML
    private void onCambiarPassword() {
        Usuario u = tabla.getSelectionModel().getSelectedItem();
        if (u == null) {
            return;
        }
        boolean propia = permisos.esResponsable(u.id());
        if (PasswordDialogo.mostrar(ventana(), service, u, propia)) {
            Notificaciones.exito(tabla, "Contraseña de " + u.nombre() + " cambiada.");
        }
    }

    @FXML
    private void onRecargar() {
        cargar(paginacion.actual());
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }
}
