package com.devsuj.crmdesktop.view.contratos;

import com.devsuj.crmdesktop.model.Contrato;
import com.devsuj.crmdesktop.model.EstadoContrato;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.service.ContratoService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.util.Alertas;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.Formatos;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Contratos: listado con filtro por estado y "por vencer", y las acciones del ciclo de vida
 * (alta, edición en borrador, estado, extensión, renovación). GERENTE y ADMIN además marcan como
 * vencidos, en lote, los vigentes cuya fecha ya pasó. Los contratos no se borran.
 */
public class ContratosController {

    private final ContratoService service;
    private final ClienteService clienteService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private Paginacion paginacion;
    private int ultimaSolicitud;

    @FXML
    private ComboBox<EstadoContrato> filtroEstado;
    @FXML
    private ToggleButton botonPorVencer;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonEstado;
    @FXML
    private Button botonExtender;
    @FXML
    private Button botonRenovar;
    @FXML
    private Button botonVencidos;
    @FXML
    private TableView<Contrato> tabla;
    @FXML
    private TableColumn<Contrato, String> colTitulo;
    @FXML
    private TableColumn<Contrato, String> colCliente;
    @FXML
    private TableColumn<Contrato, String> colVigencia;
    @FXML
    private TableColumn<Contrato, String> colMonto;
    @FXML
    private TableColumn<Contrato, EstadoContrato> colEstado;
    @FXML
    private TableColumn<Contrato, String> colVence;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;

    public ContratosController(ContratoService service, ClienteService clienteService, Permisos permisos) {
        this.service = service;
        this.clienteService = clienteService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando, this::cargar, "contratos");
        Tablas.ajustable(tabla);
        Tablas.texto(colTitulo, c -> c.titulo() + (c.contratoAnteriorId() == null ? "" : "  (renovación de #" + c.contratoAnteriorId() + ")"));
        Tablas.texto(colCliente, Contrato::clienteNombre);
        Tablas.texto(colVigencia, c -> Formatos.fecha(c.fechaInicio()) + " – " + Formatos.fecha(c.fechaFin()));
        Tablas.moneda(colMonto, Contrato::monto);
        Tablas.etiqueta(colEstado, Contrato::estado, "contrato-");
        Tablas.texto(colVence, ContratosController::textoVencimiento);

        EtiquetaListCell.filtro(filtroEstado, "Todos los estados", EstadoContrato.values());
        filtroEstado.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        botonPorVencer.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));

        botonEditar.disableProperty().bind(si(c -> c.estado() == EstadoContrato.BORRADOR));
        botonEstado.disableProperty().bind(si(c -> !service.destinosPermitidos(c).isEmpty()));
        botonExtender.disableProperty().bind(si(c -> c.estado() == EstadoContrato.VIGENTE));
        botonRenovar.disableProperty().bind(si(ContratoService::sePuedeRenovar));
        botonVencidos.setVisible(permisos.gestionaEquipo());
        botonVencidos.setManaged(permisos.gestionaEquipo());
        botonVencidos.disableProperty().bind(cargando);
        Tablas.alDobleClic(tabla, this::onEditar);
        capaCarga.visibleProperty().bind(cargando);
        cargar(0);
    }

    /** "En 30 días", "Venció hace 3 días", o guion si no está vigente. */
    static String textoVencimiento(Contrato c) {
        if (c.diasParaVencer() == null) {
            return null;
        }
        long dias = c.diasParaVencer();
        if (dias < 0) {
            return "Venció hace " + (-dias) + " día(s): márcalo vencido";
        }
        return dias == 0 ? "Vence hoy" : "En " + dias + " día(s)";
    }

    /** Botón deshabilitado salvo que haya un contrato elegido que cumpla la condición y el usuario lo gestione. */
    private BooleanBinding si(Predicate<Contrato> condicion) {
        var seleccion = tabla.getSelectionModel().selectedItemProperty();
        return Bindings.createBooleanBinding(() -> {
            Contrato c = seleccion.get();
            // Lo gestiona el vendedor del cliente, su gerente o un ADMIN.
            return c == null || !permisos.puedeGestionarClienteDe(c.clienteVendedorId()) || !condicion.test(c);
        }, seleccion).or(cargando);
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        EstadoContrato estado = filtroEstado.getValue();
        boolean porVencer = botonPorVencer.isSelected();
        TareaEnSegundoPlano.ejecutar(() -> service.buscar(estado, porVencer, numeroPagina),
                (Pagina<Contrato> resultado) -> {
                    if (solicitud != ultimaSolicitud) {
                        return;
                    }
                    tabla.getItems().setAll(resultado.contenido());
                    paginacion.mostrar(resultado);
                    tabla.setPlaceholder(EstadoVacio.crear("fth-file-text", porVencer
                                    ? "Ningún contrato vigente vence en los próximos " + ContratoService.DIAS_POR_VENCER + " días."
                                    : "No hay contratos con esos filtros.", "Crear contrato", this::onNuevo));
                },
                error -> {
                    if (solicitud == ultimaSolicitud) {
                        tabla.getItems().clear();
                        paginacion.limpiar();
                        tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                                "No se pudieron cargar los contratos. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    @FXML
    private void onNuevo() {
        TareaEnSegundoPlano.ejecutar(
                () -> clienteService.paraSeleccion(null).stream().filter(permisos::puedeGestionarCliente).toList(),
                clientes -> despues(ContratoDialogos.alta(ventana(), service, null, clientes), "Contrato creado como borrador."),
                error -> Notificaciones.error(tabla, MensajesError.de(error)), cargando);
    }

    @FXML
    private void onEditar() {
        Contrato c = tabla.getSelectionModel().getSelectedItem();
        if (c != null && c.estado() == EstadoContrato.BORRADOR && permisos.puedeGestionarClienteDe(c.clienteVendedorId())) {
            despues(ContratoDialogos.alta(ventana(), service, c, List.of()), "Contrato actualizado.");
        }
    }

    @FXML
    private void onCambiarEstado() {
        Contrato c = tabla.getSelectionModel().getSelectedItem();
        if (c != null) {
            despues(ContratoDialogos.cambiarEstado(ventana(), service, c), null);
        }
    }

    @FXML
    private void onExtender() {
        Contrato c = tabla.getSelectionModel().getSelectedItem();
        if (c != null) {
            despues(ContratoDialogos.extender(ventana(), service, c), null);
        }
    }

    @FXML
    private void onRenovar() {
        Contrato c = tabla.getSelectionModel().getSelectedItem();
        if (c != null) {
            despues(ContratoDialogos.renovar(ventana(), service, c), "Renovación creada como borrador.");
        }
    }

    @FXML
    private void onMarcarVencidos() {
        if (!Alertas.confirmar(tabla, "Marcar vencidos",
                "¿Marcar como vencidos todos los contratos vigentes cuya fecha de fin ya pasó?", "Marcar vencidos")) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(service::marcarVencidos,
                lote -> {
                    Notificaciones.exito(tabla, lote.vencidos() == 0
                            ? "No había contratos vencidos por marcar."
                            : lote.vencidos() + " contrato(s) marcados como vencidos.");
                    cargar(paginacion.actual());
                },
                error -> Alertas.error(tabla, MensajesError.de(error)), cargando);
    }

    /** Después de un diálogo: aviso y recarga. Sin mensaje propio, describe el estado resultante. */
    private void despues(Optional<Contrato> resultado, String mensaje) {
        resultado.ifPresent(c -> {
            Notificaciones.exito(tabla, mensaje != null ? mensaje
                    : c.titulo() + ": " + c.estado().etiqueta().toLowerCase() + ", vence el " + Formatos.fecha(c.fechaFin()) + ".");
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
