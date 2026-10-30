package com.devsuj.crmdesktop.view.clientes;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.util.Alertas;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.Atajos;
import com.devsuj.crmdesktop.view.comun.Paginacion;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

/**
 * Pantalla de clientes: listado paginado con búsqueda y filtros, y acciones de alta, edición, baja
 * y asignación de responsable.
 *
 * <p>Las acciones se adaptan al rol ({@link Permisos}): un VENDEDOR solo edita o da de baja sus
 * clientes; GERENTE y ADMIN además asignan responsables (uno o varios clientes a la vez).</p>
 *
 * <p>Toda llamada a la API pasa por los services y corre en segundo plano
 * ({@link TareaEnSegundoPlano}) para no congelar la ventana; mientras tanto se muestra un
 * indicador de carga.</p>
 */
public class ClientesController {

    private static final KeyCombination ATAJO_NUEVO = KeyCombination.keyCombination("Shortcut+N");
    private static final KeyCombination ATAJO_BUSCAR = KeyCombination.keyCombination("Shortcut+F");
    private static final KeyCombination ATAJO_RECARGAR = new KeyCodeCombination(KeyCode.F5);

    private final ClienteService service;
    private final UsuarioService usuarioService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    /** Espera a que el usuario deje de escribir antes de buscar (no una petición por tecla). */
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(350));
    private Paginacion paginacion;
    /** Para ignorar respuestas viejas si el usuario buscó otra cosa mientras tanto. */
    private int ultimaSolicitud;

    @FXML
    private TextField campoBusqueda;
    @FXML
    private ComboBox<EstadoCliente> filtroEstado;
    @FXML
    private CheckBox soloMios;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonBaja;
    @FXML
    private Button botonAsignar;
    @FXML
    private Button boton360;
    @FXML
    private TableView<Cliente> tabla;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, String> colEmpresa;
    @FXML
    private TableColumn<Cliente, String> colEmail;
    @FXML
    private TableColumn<Cliente, String> colTelefono;
    @FXML
    private TableColumn<Cliente, EstadoCliente> colEstado;
    @FXML
    private TableColumn<Cliente, String> colVendedor;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;

    public ClientesController(ClienteService service, UsuarioService usuarioService, Permisos permisos) {
        this.service = service;
        this.usuarioService = usuarioService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando,
                this::cargar, "clientes");
        configurarTabla();
        configurarFiltros();
        configurarBotones();
        Atajos.instalar(tabla, Map.of(
                ATAJO_NUEVO, this::onNuevo,
                ATAJO_BUSCAR, campoBusqueda::requestFocus,
                ATAJO_RECARGAR, this::onRecargar));
        capaCarga.visibleProperty().bind(cargando);
        cargar(0);
    }

    private void configurarTabla() {
        Tablas.ajustable(tabla);
        Tablas.texto(colNombre, Cliente::nombre);
        Tablas.texto(colEmpresa, Cliente::empresa);
        Tablas.texto(colEmail, Cliente::email);
        Tablas.texto(colTelefono, Cliente::telefono);
        Tablas.texto(colVendedor, c -> c.vendedorNombre() == null ? "Sin asignar" : c.vendedorNombre());
        Tablas.etiqueta(colEstado, Cliente::estado, "estado-");
        Tablas.alDobleClic(tabla, this::onEditar);
        // GERENTE y ADMIN pueden elegir varios clientes (Ctrl/Shift + clic) para reasignarlos juntos.
        if (permisos.puedeAsignarVendedores()) {
            tabla.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }
    }

    private void configurarFiltros() {
        filtroEstado.getItems().add(null); // null = "Todos los estados"
        filtroEstado.getItems().addAll(EstadoCliente.values());
        filtroEstado.setCellFactory(lista -> new EstadoClienteListCell("Todos los estados"));
        filtroEstado.setButtonCell(new EstadoClienteListCell("Todos los estados"));
        filtroEstado.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        soloMios.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));

        esperaBusqueda.setOnFinished(evento -> cargar(0));
        campoBusqueda.textProperty().addListener((obs, antes, ahora) -> esperaBusqueda.playFromStart());
    }

    private void configurarBotones() {
        BooleanProperty seleccionGestionable = new SimpleBooleanProperty(false);
        BooleanProperty seleccionInactiva = new SimpleBooleanProperty(false);
        BooleanProperty unoSeleccionado = new SimpleBooleanProperty(false);
        tabla.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Cliente>) cambio -> {
            List<Cliente> seleccion = List.copyOf(tabla.getSelectionModel().getSelectedItems());
            unoSeleccionado.set(seleccion.size() == 1);
            seleccionGestionable.set(seleccion.size() == 1 && permisos.puedeGestionarCliente(seleccion.getFirst()));
            seleccionInactiva.set(seleccion.size() == 1 && seleccion.getFirst().estado() == EstadoCliente.INACTIVO);
        });
        BooleanBinding sinSeleccion = Bindings.isEmpty(tabla.getSelectionModel().getSelectedItems());

        botonEditar.disableProperty().bind(seleccionGestionable.not().or(cargando));
        botonBaja.disableProperty().bind(seleccionGestionable.not().or(seleccionInactiva).or(cargando));
        botonAsignar.disableProperty().bind(sinSeleccion.or(cargando));
        // La vista 360 la ven el vendedor del cliente, su gerente o un ADMIN.
        boton360.disableProperty().bind(seleccionGestionable.not().or(cargando));
        // Lo que el rol no permite, no se muestra.
        botonAsignar.setVisible(permisos.puedeAsignarVendedores());
        botonAsignar.setManaged(permisos.puedeAsignarVendedores());
        soloMios.setText(permisos.esVendedor() ? "Solo mis clientes" : "Solo los que atiendo yo");
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        String texto = campoBusqueda.getText();
        EstadoCliente estado = filtroEstado.getValue();
        Integer vendedorId = soloMios.isSelected() ? permisos.usuario().id() : null;
        TareaEnSegundoPlano.ejecutar(
                () -> service.buscar(texto, estado, vendedorId, numeroPagina),
                resultado -> {
                    if (solicitud == ultimaSolicitud) {
                        mostrarPagina(resultado);
                    }
                },
                error -> {
                    if (solicitud == ultimaSolicitud) {
                        mostrarErrorDeCarga(error);
                    }
                },
                cargando);
    }

    private void mostrarPagina(Pagina<Cliente> resultado) {
        tabla.getItems().setAll(resultado.contenido());
        paginacion.mostrar(resultado);

        boolean hayFiltros = !campoBusqueda.getText().isBlank() || filtroEstado.getValue() != null
                || soloMios.isSelected();
        tabla.setPlaceholder(hayFiltros
                ? EstadoVacio.crear("fth-search", "No se encontraron clientes con esos filtros.",
                        "Limpiar filtros", this::limpiarFiltros)
                : EstadoVacio.crear("fth-users", "Aún no hay clientes.", "Crear cliente", this::onNuevo));
    }

    private void mostrarErrorDeCarga(Throwable error) {
        tabla.getItems().clear();
        paginacion.limpiar();
        tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                "No se pudieron cargar los clientes. " + MensajesError.de(error),
                "Reintentar", () -> cargar(paginacion.actual())));
    }

    private void limpiarFiltros() {
        campoBusqueda.clear();
        filtroEstado.setValue(null);
        soloMios.setSelected(false);
        cargar(0);
    }

    @FXML
    private void onNuevo() {
        ClienteDialogo.mostrar(ventana(), service, null).ifPresent(creado -> {
            Notificaciones.exito(tabla, "Cliente " + creado.nombre() + " creado.");
            cargar(paginacion.actual());
        });
    }

    @FXML
    private void onEditar() {
        Cliente seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null || !permisos.puedeGestionarCliente(seleccionado)) {
            return;
        }
        ClienteDialogo.mostrar(ventana(), service, seleccionado).ifPresent(actualizado -> {
            Notificaciones.exito(tabla, "Cliente " + actualizado.nombre() + " actualizado.");
            cargar(paginacion.actual());
        });
    }

    @FXML
    private void onDarDeBaja() {
        Cliente seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        boolean confirmado = Alertas.confirmar(tabla, "Dar de baja al cliente",
                "¿Dar de baja a " + seleccionado.nombre() + "?\n\n"
                        + "Pasará a estado Inactivo. Su historial (ventas, contratos, etc.) se conserva "
                        + "y podrás reactivarlo editándolo.",
                "Dar de baja");
        if (!confirmado) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(
                () -> service.darDeBaja(seleccionado),
                cliente -> {
                    Notificaciones.exito(tabla, "Cliente " + cliente.nombre() + " dado de baja.");
                    cargar(paginacion.actual());
                },
                error -> Alertas.error(tabla, MensajesError.de(error)),
                cargando);
    }

    /** Primero carga los responsables posibles (en segundo plano) y luego abre el diálogo. */
    @FXML
    private void onAsignar() {
        List<Cliente> seleccion = List.copyOf(tabla.getSelectionModel().getSelectedItems());
        if (seleccion.isEmpty()) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(
                usuarioService::responsables,
                responsables -> AsignarVendedorDialogo.mostrar(ventana(), service, seleccion, responsables)
                        .ifPresent(cambiados -> {
                            Notificaciones.exito(tabla, cambiados == 1
                                    ? "Responsable asignado."
                                    : cambiados + " clientes reasignados.");
                            cargar(paginacion.actual());
                        }),
                error -> Notificaciones.error(tabla, MensajesError.de(error)),
                cargando);
    }

    @FXML
    private void onVista360() {
        Cliente seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null || !permisos.puedeGestionarCliente(seleccionado)) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> service.resumen(seleccionado),
                resumen -> Cliente360Dialogo.mostrar(ventana(), resumen),
                error -> Notificaciones.error(tabla, MensajesError.de(error)),
                cargando);
    }

    @FXML
    private void onRecargar() {
        cargar(paginacion.actual());
    }

    private Window ventana() {
        Scene escena = tabla.getScene();
        return escena == null ? null : escena.getWindow();
    }
}
