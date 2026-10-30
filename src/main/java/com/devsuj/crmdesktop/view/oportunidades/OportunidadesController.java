package com.devsuj.crmdesktop.view.oportunidades;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EtapaOportunidad;
import com.devsuj.crmdesktop.model.HistorialEtapa;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.service.OportunidadService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.Atajos;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Paginacion;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.Map;

/**
 * Pipeline de oportunidades: listado filtrable por etapa y, a la derecha, el detalle de la
 * oportunidad elegida con el diagrama del proceso y la línea de tiempo de su historial.
 *
 * <p>Acciones: nueva, editar (solo abiertas), cambiar etapa (abiertas, nunca a GANADA) y "nuevo
 * intento" para retomar una PERDIDA. Se habilitan según el rol y el responsable.</p>
 */
public class OportunidadesController {

    private final OportunidadService service;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final BooleanProperty cargandoDetalle = new SimpleBooleanProperty(false);
    private Paginacion paginacion;
    private int ultimaSolicitud;
    private int ultimoDetalle;

    @FXML
    private ComboBox<EtapaOportunidad> filtroEtapa;
    @FXML
    private CheckBox soloAbiertas;
    @FXML
    private CheckBox soloMias;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonEtapa;
    @FXML
    private Button botonNuevoIntento;
    @FXML
    private TableView<Oportunidad> tabla;
    @FXML
    private TableColumn<Oportunidad, String> colTitulo;
    @FXML
    private TableColumn<Oportunidad, String> colCliente;
    @FXML
    private TableColumn<Oportunidad, String> colMonto;
    @FXML
    private TableColumn<Oportunidad, EtapaOportunidad> colEtapa;
    @FXML
    private TableColumn<Oportunidad, String> colResponsable;
    @FXML
    private TableColumn<Oportunidad, String> colCierre;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;
    @FXML
    private Label detalleTitulo;
    @FXML
    private Label detalleDatos;
    @FXML
    private StackPane detalleDiagrama;
    @FXML
    private StackPane detalleLinea;
    @FXML
    private Label detalleResumen;
    @FXML
    private ProgressIndicator indicadorDetalle;

    public OportunidadesController(OportunidadService service, ClienteService clienteService,
                                   UsuarioService usuarioService, Permisos permisos) {
        this.service = service;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando,
                this::cargar, "oportunidades");
        Tablas.ajustable(tabla);
        Tablas.texto(colTitulo, Oportunidad::titulo);
        Tablas.texto(colCliente, Oportunidad::clienteNombre);
        Tablas.moneda(colMonto, Oportunidad::montoEstimado);
        Tablas.etiqueta(colEtapa, Oportunidad::etapa, "etapa-");
        Tablas.texto(colResponsable, Oportunidad::usuarioNombre);
        Tablas.fecha(colCierre, Oportunidad::fechaCierreEstimada);
        // Un VENDEDOR solo ve sus oportunidades: la columna de responsable no le aporta nada.
        colResponsable.setVisible(!permisos.esVendedor());
        Tablas.alDobleClic(tabla, this::onEditar);

        EtiquetaListCell.filtro(filtroEtapa, "Todas las etapas", EtapaOportunidad.values());
        filtroEtapa.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        soloAbiertas.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));
        soloMias.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));
        soloMias.setSelected(permisos.esVendedor());

        configurarBotones();
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> mostrarDetalle(ahora));
        indicadorDetalle.visibleProperty().bind(cargandoDetalle);
        capaCarga.visibleProperty().bind(cargando);
        Atajos.instalar(tabla, Map.of(
                KeyCombination.keyCombination("Shortcut+N"), this::onNueva,
                new KeyCodeCombination(KeyCode.F5), this::onRecargar));
        mostrarDetalle(null);
        cargar(0);
    }

    private void configurarBotones() {
        BooleanBinding gestionableAbierta = Bindings.createBooleanBinding(() -> {
            Oportunidad o = tabla.getSelectionModel().getSelectedItem();
            return o != null && !o.etapa().esCerrada() && permisos.puedeGestionarDe(o.usuarioId());
        }, tabla.getSelectionModel().selectedItemProperty());
        BooleanBinding perdida = Bindings.createBooleanBinding(() -> {
            Oportunidad o = tabla.getSelectionModel().getSelectedItem();
            return o != null && o.etapa() == EtapaOportunidad.PERDIDA;
        }, tabla.getSelectionModel().selectedItemProperty());
        botonEditar.disableProperty().bind(gestionableAbierta.not().or(cargando));
        botonEtapa.disableProperty().bind(gestionableAbierta.not().or(cargando));
        botonNuevoIntento.disableProperty().bind(perdida.not().or(cargando));
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        EtapaOportunidad etapa = filtroEtapa.getValue();
        Boolean abiertas = soloAbiertas.isSelected() ? Boolean.TRUE : null;
        Integer usuarioId = soloMias.isSelected() ? permisos.usuario().id() : null;
        TareaEnSegundoPlano.ejecutar(
                () -> service.buscar(etapa, usuarioId, abiertas, numeroPagina),
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
                                "No se pudieron cargar las oportunidades. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    private void mostrarPagina(Pagina<Oportunidad> resultado) {
        Integer seleccionadaId = tabla.getSelectionModel().getSelectedItem() == null
                ? null : tabla.getSelectionModel().getSelectedItem().id();
        tabla.getItems().setAll(resultado.contenido());
        paginacion.mostrar(resultado);
        tabla.getItems().stream().filter(o -> o.id().equals(seleccionadaId)).findFirst()
                .ifPresent(o -> tabla.getSelectionModel().select(o));
        boolean hayFiltros = filtroEtapa.getValue() != null || soloAbiertas.isSelected() || soloMias.isSelected();
        tabla.setPlaceholder(hayFiltros
                ? EstadoVacio.crear("fth-search", "No hay oportunidades con esos filtros.", "Ver todas", () -> {
                    filtroEtapa.setValue(null);
                    soloAbiertas.setSelected(false);
                    soloMias.setSelected(false);
                })
                : EstadoVacio.crear("fth-target", "Aún no hay oportunidades.", "Registrar oportunidad", this::onNueva));
    }

    /** Muestra el panel derecho y carga el historial de la oportunidad elegida. */
    private void mostrarDetalle(Oportunidad o) {
        int solicitud = ++ultimoDetalle;
        detalleDiagrama.getChildren().clear();
        detalleLinea.getChildren().clear();
        detalleResumen.setText("");
        if (o == null) {
            detalleTitulo.setText("Elige una oportunidad");
            detalleDatos.setText("Aquí verás su proceso: por qué etapas pasó, cuánto tiempo estuvo en cada una "
                    + "y quién la movió.");
            return;
        }
        detalleTitulo.setText(o.titulo());
        detalleDatos.setText(o.clienteNombre() + " · " + Formatos.moneda(o.montoEstimado())
                + " · Responsable: " + Formatos.textoOGuion(o.usuarioNombre())
                + (o.oportunidadAnteriorId() == null ? "" : "\nNuevo intento de la oportunidad #" + o.oportunidadAnteriorId()));
        TareaEnSegundoPlano.ejecutar(
                () -> service.historial(o),
                historial -> {
                    if (solicitud == ultimoDetalle) {
                        pintarHistorial(o, historial);
                    }
                },
                error -> {
                    if (solicitud == ultimoDetalle) {
                        detalleResumen.setText("No se pudo cargar el historial. " + MensajesError.de(error));
                    }
                },
                cargandoDetalle);
    }

    private void pintarHistorial(Oportunidad o, List<HistorialEtapa> historial) {
        detalleDiagrama.getChildren().setAll(ProcesoOportunidadVista.diagrama(o.etapa(), historial));
        detalleLinea.getChildren().setAll(historial.isEmpty()
                ? new Label("Sin movimientos registrados.")
                : ProcesoOportunidadVista.lineaDeTiempo(historial));
        detalleResumen.setText("Proceso de " + ProcesoOportunidadVista.dias(OportunidadService.diasTotales(historial))
                + " en " + historial.size() + " paso(s).");
    }

    @FXML
    private void onNueva() {
        abrirFormulario(null, null);
    }

    @FXML
    private void onEditar() {
        Oportunidad o = tabla.getSelectionModel().getSelectedItem();
        if (o != null && !o.etapa().esCerrada() && permisos.puedeGestionarDe(o.usuarioId())) {
            abrirFormulario(o, null);
        }
    }

    @FXML
    private void onNuevoIntento() {
        Oportunidad o = tabla.getSelectionModel().getSelectedItem();
        if (o != null && o.etapa() == EtapaOportunidad.PERDIDA) {
            abrirFormulario(null, o);
        }
    }

    /** Al crear, primero carga los clientes (y los responsables si es GERENTE/ADMIN). */
    private void abrirFormulario(Oportunidad original, Oportunidad anterior) {
        TareaEnSegundoPlano.ejecutar(
                () -> original != null ? new Opciones(List.of(), List.of()) : new Opciones(
                        clienteService.paraSeleccion(null).stream().filter(permisos::puedeGestionarCliente).toList(),
                        permisos.gestionaEquipo() ? usuarioService.responsables() : List.of()),
                opciones -> OportunidadDialogo.mostrar(ventana(), service, original, anterior,
                                opciones.clientes(), opciones.responsables())
                        .ifPresent(guardada -> {
                            Notificaciones.exito(tabla, "Oportunidad \"" + guardada.titulo() + "\" "
                                    + (original == null ? "registrada." : "actualizada."));
                            cargar(paginacion.actual());
                        }),
                error -> Notificaciones.error(tabla, MensajesError.de(error)),
                cargando);
    }

    private record Opciones(List<Cliente> clientes, List<Usuario> responsables) {
    }

    @FXML
    private void onCambiarEtapa() {
        Oportunidad o = tabla.getSelectionModel().getSelectedItem();
        if (o == null || o.etapa().esCerrada()) {
            return;
        }
        CambioEtapaDialogo.mostrar(ventana(), service, o).ifPresent(movida -> {
            Notificaciones.exito(tabla, "\"" + movida.titulo() + "\" pasó a " + movida.etapa().etiqueta() + ".");
            cargar(paginacion.actual());
            mostrarDetalle(movida);
        });
    }

    @FXML
    private void onRecargar() {
        cargar(paginacion.actual());
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }
}
