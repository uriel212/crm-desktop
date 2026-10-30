package com.devsuj.crmdesktop.view.interacciones;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoInteraccion;
import com.devsuj.crmdesktop.model.Interaccion;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.TipoInteraccion;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.service.InteraccionService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.Atajos;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Paginacion;
import com.devsuj.crmdesktop.view.comun.Selectores;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interacciones y agenda: a la izquierda el historial filtrable (por cliente, tipo, estado y
 * fechas) y a la derecha las actividades programadas de los próximos días.
 *
 * <p>Las acciones (editar, marcar realizada, cancelar) actúan sobre lo último que se eligió, en la
 * tabla o en la agenda. Nada se borra: una actividad que no ocurrirá se cancela.</p>
 */
public class InteraccionesController {

    private final InteraccionService service;
    private final ClienteService clienteService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final BooleanProperty cargandoAgenda = new SimpleBooleanProperty(false);
    /** Interacción elegida en la tabla o en la agenda. */
    private final ObjectProperty<Interaccion> seleccionada = new SimpleObjectProperty<>();
    private Paginacion paginacion;
    private int ultimaSolicitud;

    @FXML
    private StackPane contenedorCliente;
    @FXML
    private ComboBox<TipoInteraccion> filtroTipo;
    @FXML
    private ComboBox<EstadoInteraccion> filtroEstado;
    @FXML
    private DatePicker filtroDesde;
    @FXML
    private DatePicker filtroHasta;
    @FXML
    private CheckBox soloMias;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonRealizada;
    @FXML
    private Button botonCancelar;
    @FXML
    private TableView<Interaccion> tabla;
    @FXML
    private TableColumn<Interaccion, String> colFecha;
    @FXML
    private TableColumn<Interaccion, TipoInteraccion> colTipo;
    @FXML
    private TableColumn<Interaccion, String> colAsunto;
    @FXML
    private TableColumn<Interaccion, String> colCliente;
    @FXML
    private TableColumn<Interaccion, EstadoInteraccion> colEstado;
    @FXML
    private TableColumn<Interaccion, String> colUsuario;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;
    @FXML
    private ListView<Interaccion> agenda;
    @FXML
    private VBox capaCargaAgenda;

    private final SearchableComboBox<Cliente> filtroCliente = Selectores.clientes(List.of());

    public InteraccionesController(InteraccionService service, ClienteService clienteService, Permisos permisos) {
        this.service = service;
        this.clienteService = clienteService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando,
                this::cargar, "interacciones");
        Tablas.ajustable(tabla);
        Tablas.fechaHora(colFecha, Interaccion::fecha);
        Tablas.etiqueta(colTipo, Interaccion::tipo, "tipo-");
        Tablas.texto(colAsunto, Interaccion::asunto);
        Tablas.texto(colCliente, Interaccion::clienteNombre);
        Tablas.etiqueta(colEstado, Interaccion::estado, "interaccion-");
        Tablas.texto(colUsuario, Interaccion::usuarioNombre);
        Tablas.alDobleClic(tabla, this::onEditar);
        Tablas.tooltipDeFila(tabla, i -> {
            String detalle = (i.descripcion() == null ? "" : i.descripcion())
                    + (i.resultado() == null ? "" : "\nResultado: " + i.resultado());
            return detalle.isBlank() ? null : detalle.strip();
        });

        filtroCliente.setPromptText("Todos los clientes");
        filtroCliente.setMaxWidth(260);
        contenedorCliente.getChildren().setAll(filtroCliente);
        EtiquetaListCell.filtro(filtroTipo, "Todos los tipos", TipoInteraccion.values());
        EtiquetaListCell.filtro(filtroEstado, "Todos los estados", EstadoInteraccion.values());
        filtroCliente.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroTipo.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroEstado.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroDesde.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroHasta.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        soloMias.setSelected(permisos.esVendedor());
        soloMias.selectedProperty().addListener((obs, antes, ahora) -> {
            cargar(0);
            cargarAgenda();
        });

        agenda.setCellFactory(lista -> new ActividadListCell());
        agenda.setPlaceholder(EstadoVacio.crear("fth-calendar", "No hay actividades programadas para los próximos "
                + InteraccionService.DIAS_PROXIMAS + " días.", null, null));
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> {
            if (ahora != null) {
                agenda.getSelectionModel().clearSelection();
                seleccionada.set(ahora);
            }
        });
        agenda.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> {
            if (ahora != null) {
                tabla.getSelectionModel().clearSelection();
                seleccionada.set(ahora);
            }
        });

        configurarBotones();
        capaCarga.visibleProperty().bind(cargando);
        capaCargaAgenda.visibleProperty().bind(cargandoAgenda);
        Atajos.instalar(tabla, Map.of(
                KeyCombination.keyCombination("Shortcut+N"), this::onNueva,
                new KeyCodeCombination(KeyCode.F5), this::onRecargar));
        cargarClientes();
        cargar(0);
        cargarAgenda();
    }

    private void configurarBotones() {
        BooleanBinding editable = Bindings.createBooleanBinding(() -> {
            Interaccion i = seleccionada.get();
            return i != null && i.estado() != EstadoInteraccion.CANCELADA && permisos.puedeGestionarDe(i.usuarioId());
        }, seleccionada);
        BooleanBinding programada = Bindings.createBooleanBinding(() -> {
            Interaccion i = seleccionada.get();
            return i != null && i.estado() == EstadoInteraccion.PROGRAMADA && permisos.puedeGestionarDe(i.usuarioId());
        }, seleccionada);
        botonEditar.disableProperty().bind(editable.not().or(cargando));
        botonRealizada.disableProperty().bind(programada.not().or(cargando));
        botonCancelar.disableProperty().bind(programada.not().or(cargando));
        botonEditar.textProperty().bind(Bindings.createStringBinding(() ->
                seleccionada.get() != null && seleccionada.get().estado() == EstadoInteraccion.PROGRAMADA
                        ? "Reprogramar" : "Editar", seleccionada));
    }

    private void cargarClientes() {
        TareaEnSegundoPlano.ejecutar(() -> clienteService.paraSeleccion(null),
                clientes -> filtroCliente.getItems().setAll(clientes),
                error -> Notificaciones.advertencia(tabla, "No se pudo cargar la lista de clientes para el filtro."),
                null);
    }

    private Integer usuarioFiltro() {
        return soloMias.isSelected() ? permisos.usuario().id() : null;
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        Cliente cliente = filtroCliente.getValue();
        Integer clienteId = cliente == null ? null : cliente.id();
        TipoInteraccion tipo = filtroTipo.getValue();
        EstadoInteraccion estado = filtroEstado.getValue();
        LocalDate desde = filtroDesde.getValue();
        LocalDate hasta = filtroHasta.getValue();
        Integer usuarioId = usuarioFiltro();
        TareaEnSegundoPlano.ejecutar(
                () -> service.buscar(usuarioId, clienteId, tipo, estado, desde, hasta, numeroPagina),
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
                                "No se pudieron cargar las interacciones. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    private void mostrarPagina(Pagina<Interaccion> resultado) {
        tabla.getItems().setAll(resultado.contenido());
        paginacion.mostrar(resultado);
        tabla.setPlaceholder(EstadoVacio.crear("fth-message-square",
                "No hay interacciones con esos filtros.", "Registrar interacción", this::onNueva));
    }

    private void cargarAgenda() {
        Integer usuarioId = usuarioFiltro();
        TareaEnSegundoPlano.ejecutar(() -> service.proximas(usuarioId),
                actividades -> agenda.getItems().setAll(actividades),
                error -> agenda.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                        "No se pudo cargar la agenda. " + MensajesError.de(error), "Reintentar", this::cargarAgenda)),
                cargandoAgenda);
    }

    @FXML
    private void onLimpiarFiltros() {
        filtroCliente.setValue(null);
        filtroTipo.setValue(null);
        filtroEstado.setValue(null);
        filtroDesde.setValue(null);
        filtroHasta.setValue(null);
    }

    @FXML
    private void onNueva() {
        TareaEnSegundoPlano.ejecutar(
                () -> clienteService.paraSeleccion(null).stream().filter(permisos::puedeGestionarCliente).toList(),
                clientes -> InteraccionDialogo.mostrar(ventana(), service, null, clientes)
                        .ifPresent(i -> despuesDeGuardar(i.estado() == EstadoInteraccion.PROGRAMADA
                                ? "Actividad agendada." : "Interacción registrada.")),
                error -> Notificaciones.error(tabla, MensajesError.de(error)),
                cargando);
    }

    @FXML
    private void onEditar() {
        Interaccion i = seleccionada.get();
        if (i == null || i.estado() == EstadoInteraccion.CANCELADA || !permisos.puedeGestionarDe(i.usuarioId())) {
            return;
        }
        InteraccionDialogo.mostrar(ventana(), service, i, List.of())
                .ifPresent(guardada -> despuesDeGuardar("Interacción actualizada."));
    }

    @FXML
    private void onMarcarRealizada() {
        cerrar(EstadoInteraccion.REALIZADA, "Actividad marcada como realizada.");
    }

    @FXML
    private void onCancelar() {
        cerrar(EstadoInteraccion.CANCELADA, "Actividad cancelada.");
    }

    private void cerrar(EstadoInteraccion estado, String mensaje) {
        Interaccion i = seleccionada.get();
        if (i == null || i.estado() != EstadoInteraccion.PROGRAMADA) {
            return;
        }
        CerrarActividadDialogo.mostrar(ventana(), service, i, estado).ifPresent(cerrada -> despuesDeGuardar(mensaje));
    }

    private void despuesDeGuardar(String mensaje) {
        Notificaciones.exito(tabla, mensaje);
        seleccionada.set(null);
        cargar(paginacion.actual());
        cargarAgenda();
    }

    @FXML
    private void onRecargar() {
        cargar(paginacion.actual());
        cargarAgenda();
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }
}
