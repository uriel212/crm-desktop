package com.devsuj.crmdesktop.view.soporte;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoTicket;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.PrioridadTicket;
import com.devsuj.crmdesktop.model.Ticket;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.TicketService;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Soporte: tickets de clientes con filtros por estado, prioridad, responsable y sin asignar.
 * Un ticket cerrado no se reabre: se crea un seguimiento ligado. GERENTE y ADMIN asignan responsables.
 */
public class SoporteController {

    private final TicketService service;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private Paginacion paginacion;
    private int ultimaSolicitud;

    @FXML
    private ComboBox<EstadoTicket> filtroEstado;
    @FXML
    private ComboBox<PrioridadTicket> filtroPrioridad;
    @FXML
    private CheckBox soloMios;
    @FXML
    private CheckBox sinAsignar;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonEstado;
    @FXML
    private Button botonAsignar;
    @FXML
    private Button botonSeguimiento;
    @FXML
    private TableView<Ticket> tabla;
    @FXML
    private TableColumn<Ticket, String> colNumero;
    @FXML
    private TableColumn<Ticket, String> colAsunto;
    @FXML
    private TableColumn<Ticket, String> colCliente;
    @FXML
    private TableColumn<Ticket, PrioridadTicket> colPrioridad;
    @FXML
    private TableColumn<Ticket, EstadoTicket> colEstado;
    @FXML
    private TableColumn<Ticket, String> colResponsable;
    @FXML
    private TableColumn<Ticket, String> colCreado;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;

    public SoporteController(TicketService service, ClienteService clienteService, UsuarioService usuarioService,
                             Permisos permisos) {
        this.service = service;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando, this::cargar, "tickets");
        Tablas.ajustable(tabla);
        Tablas.texto(colNumero, t -> "#" + t.id() + (t.ticketRelacionadoId() == null ? "" : " ← #" + t.ticketRelacionadoId()));
        Tablas.texto(colAsunto, Ticket::asunto);
        Tablas.texto(colCliente, Ticket::clienteNombre);
        Tablas.etiqueta(colPrioridad, Ticket::prioridad, "prioridad-");
        Tablas.etiqueta(colEstado, Ticket::estado, "ticket-");
        Tablas.texto(colResponsable, t -> t.usuarioNombre() == null ? "Sin asignar" : t.usuarioNombre());
        Tablas.fechaHora(colCreado, Ticket::creadoEn);
        Tablas.tooltipDeFila(tabla, Ticket::descripcion);

        EtiquetaListCell.filtro(filtroEstado, "Todos los estados", EstadoTicket.values());
        EtiquetaListCell.filtro(filtroPrioridad, "Todas las prioridades", PrioridadTicket.values());
        filtroEstado.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroPrioridad.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        soloMios.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));
        sinAsignar.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));

        botonEditar.disableProperty().bind(si(t -> t.estado() != EstadoTicket.CERRADO));
        botonEstado.disableProperty().bind(si(t -> !TicketService.destinosPermitidos(t).isEmpty()));
        botonAsignar.disableProperty().bind(si(t -> t.estado() != EstadoTicket.CERRADO));
        botonSeguimiento.disableProperty().bind(si(t -> t.estado() == EstadoTicket.CERRADO));
        botonAsignar.setVisible(permisos.gestionaEquipo());
        botonAsignar.setManaged(permisos.gestionaEquipo());
        Tablas.alDobleClic(tabla, this::onEditar);
        capaCarga.visibleProperty().bind(cargando);
        cargar(0);
    }

    private BooleanBinding si(Predicate<Ticket> condicion) {
        var seleccion = tabla.getSelectionModel().selectedItemProperty();
        return Bindings.createBooleanBinding(() -> seleccion.get() == null || !puedeGestionar(seleccion.get())
                        || !condicion.test(seleccion.get()), seleccion)
                .or(cargando);
    }

    /** Lo gestionan su responsable (o el gerente/ADMIN) o quien gestiona al cliente. */
    private boolean puedeGestionar(Ticket t) {
        return permisos.puedeGestionarDe(t.usuarioId()) || permisos.puedeGestionarClienteDe(t.clienteVendedorId());
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        EstadoTicket estado = filtroEstado.getValue();
        PrioridadTicket prioridad = filtroPrioridad.getValue();
        Integer usuarioId = soloMios.isSelected() ? permisos.usuario().id() : null;
        boolean libres = sinAsignar.isSelected();
        TareaEnSegundoPlano.ejecutar(() -> service.buscar(estado, prioridad, usuarioId, libres, numeroPagina),
                (Pagina<Ticket> resultado) -> {
                    if (solicitud != ultimaSolicitud) {
                        return;
                    }
                    tabla.getItems().setAll(resultado.contenido());
                    paginacion.mostrar(resultado);
                    tabla.setPlaceholder(EstadoVacio.crear("fth-life-buoy", "No hay tickets con esos filtros.",
                            "Nuevo ticket", this::onNuevo));
                },
                error -> {
                    if (solicitud == ultimaSolicitud) {
                        tabla.getItems().clear();
                        paginacion.limpiar();
                        tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                                "No se pudieron cargar los tickets. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    /** Al abrir: clientes que gestiona y a quién puede asignarlo (un VENDEDOR solo a sí mismo). */
    @FXML
    private void onNuevo() {
        TareaEnSegundoPlano.ejecutar(
                () -> new Opciones(
                        clienteService.paraSeleccion(null).stream().filter(permisos::puedeGestionarCliente).toList(),
                        permisos.gestionaEquipo() ? usuarioService.responsables() : List.of(permisos.usuario())),
                o -> despues(TicketDialogos.abrirOEditar(ventana(), service, null, o.clientes(), o.responsables()), "Ticket abierto."),
                error -> Notificaciones.error(tabla, MensajesError.de(error)), cargando);
    }

    private record Opciones(List<Cliente> clientes, List<Usuario> responsables) {
    }

    @FXML
    private void onEditar() {
        Ticket t = tabla.getSelectionModel().getSelectedItem();
        if (t != null && t.estado() != EstadoTicket.CERRADO && puedeGestionar(t)) {
            despues(TicketDialogos.abrirOEditar(ventana(), service, t, List.of(), List.of()), "Ticket actualizado.");
        }
    }

    @FXML
    private void onCambiarEstado() {
        Ticket t = tabla.getSelectionModel().getSelectedItem();
        if (t != null) {
            despues(TicketDialogos.cambiarEstado(ventana(), service, t), null);
        }
    }

    @FXML
    private void onAsignar() {
        Ticket t = tabla.getSelectionModel().getSelectedItem();
        if (t == null) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(usuarioService::responsables,
                responsables -> despues(TicketDialogos.asignar(ventana(), service, t, responsables), null),
                error -> Notificaciones.error(tabla, MensajesError.de(error)), cargando);
    }

    @FXML
    private void onSeguimiento() {
        Ticket t = tabla.getSelectionModel().getSelectedItem();
        if (t != null && t.estado() == EstadoTicket.CERRADO) {
            despues(TicketDialogos.seguimiento(ventana(), service, t), "Seguimiento creado como ticket nuevo.");
        }
    }

    private void despues(Optional<Ticket> resultado, String mensaje) {
        resultado.ifPresent(t -> {
            Notificaciones.exito(tabla, mensaje != null ? mensaje : "Ticket #" + t.id() + ": " + t.estado().etiqueta().toLowerCase()
                    + " · " + (t.usuarioNombre() == null ? "sin asignar" : t.usuarioNombre()) + ".");
            cargar(paginacion.actual());
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
