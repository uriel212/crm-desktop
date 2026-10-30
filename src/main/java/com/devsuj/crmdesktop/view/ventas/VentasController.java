package com.devsuj.crmdesktop.view.ventas;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.model.VentaResumen;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.service.OportunidadService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.service.VentaService;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.Atajos;
import com.devsuj.crmdesktop.view.comun.Paginacion;
import com.devsuj.crmdesktop.view.comun.Selectores;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
 * Ventas: listado filtrable (cliente, fechas, solo mías), detalle de cada venta y registro de
 * ventas nuevas. Una venta no se edita ni se cancela.
 */
public class VentasController {

    private final VentaService service;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final OportunidadService oportunidadService;
    private final UsuarioService usuarioService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final SearchableComboBox<Cliente> filtroCliente = Selectores.clientes(List.of());
    private Paginacion paginacion;
    private int ultimaSolicitud;

    @FXML
    private StackPane contenedorCliente;
    @FXML
    private DatePicker filtroDesde;
    @FXML
    private DatePicker filtroHasta;
    @FXML
    private CheckBox soloMias;
    @FXML
    private Button botonDetalle;
    @FXML
    private TableView<VentaResumen> tabla;
    @FXML
    private TableColumn<VentaResumen, String> colFecha;
    @FXML
    private TableColumn<VentaResumen, String> colNumero;
    @FXML
    private TableColumn<VentaResumen, String> colCliente;
    @FXML
    private TableColumn<VentaResumen, String> colVendedor;
    @FXML
    private TableColumn<VentaResumen, String> colSubtotal;
    @FXML
    private TableColumn<VentaResumen, String> colDescuento;
    @FXML
    private TableColumn<VentaResumen, String> colTotal;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;

    public VentasController(VentaService service, ClienteService clienteService, ProductoService productoService,
                            OportunidadService oportunidadService, UsuarioService usuarioService, Permisos permisos) {
        this.service = service;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.oportunidadService = oportunidadService;
        this.usuarioService = usuarioService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando, this::cargar, "ventas");
        Tablas.ajustable(tabla);
        Tablas.fechaHora(colFecha, VentaResumen::fecha);
        Tablas.texto(colNumero, v -> "#" + v.id());
        Tablas.texto(colCliente, VentaResumen::clienteNombre);
        Tablas.texto(colVendedor, VentaResumen::usuarioNombre);
        Tablas.moneda(colSubtotal, VentaResumen::subtotal);
        Tablas.moneda(colDescuento, VentaResumen::descuento);
        Tablas.moneda(colTotal, VentaResumen::total);
        Tablas.alDobleClic(tabla, this::onDetalle);

        filtroCliente.setPromptText("Todos los clientes");
        contenedorCliente.getChildren().setAll(filtroCliente);
        filtroCliente.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroDesde.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroHasta.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        soloMias.setSelected(permisos.esVendedor());
        soloMias.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));
        botonDetalle.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull().or(cargando));
        capaCarga.visibleProperty().bind(cargando);
        Atajos.instalar(tabla, Map.of(
                KeyCombination.keyCombination("Shortcut+N"), this::onNueva,
                new KeyCodeCombination(KeyCode.F5), this::onRecargar));

        TareaEnSegundoPlano.ejecutar(() -> clienteService.paraSeleccion(null),
                clientes -> filtroCliente.getItems().setAll(clientes),
                error -> Notificaciones.advertencia(tabla, "No se pudo cargar la lista de clientes para el filtro."),
                null);
        cargar(0);
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        Integer clienteId = filtroCliente.getValue() == null ? null : filtroCliente.getValue().id();
        Integer usuarioId = soloMias.isSelected() ? permisos.usuario().id() : null;
        LocalDate desde = filtroDesde.getValue();
        LocalDate hasta = filtroHasta.getValue();
        TareaEnSegundoPlano.ejecutar(
                () -> service.buscar(clienteId, usuarioId, desde, hasta, numeroPagina),
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
                                "No se pudieron cargar las ventas. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    private void mostrarPagina(Pagina<VentaResumen> resultado) {
        tabla.getItems().setAll(resultado.contenido());
        paginacion.mostrar(resultado);
        tabla.setPlaceholder(EstadoVacio.crear("fth-shopping-cart", "No hay ventas con esos filtros.",
                "Registrar venta", this::onNueva));
    }

    @FXML
    private void onLimpiarFiltros() {
        filtroCliente.setValue(null);
        filtroDesde.setValue(null);
        filtroHasta.setValue(null);
    }

    /** Carga clientes, productos y responsables en segundo plano y abre el registro de venta. */
    @FXML
    private void onNueva() {
        TareaEnSegundoPlano.ejecutar(
                () -> new Opciones(
                        clienteService.paraSeleccion(null).stream().filter(permisos::puedeGestionarCliente).toList(),
                        productoService.paraVenta(),
                        permisos.gestionaEquipo() ? usuarioService.responsables() : List.of()),
                opciones -> NuevaVentaDialogo.mostrar(ventana(), service, oportunidadService,
                                opciones.clientes(), opciones.productos(), opciones.responsables())
                        .ifPresent(venta -> {
                            Notificaciones.exito(tabla, "Venta #" + venta.id() + " registrada por "
                                    + Formatos.moneda(venta.total()) + ". " + venta.clienteNombre() + " ganó "
                                    + Formatos.entero(venta.puntosGanados()) + " puntos.");
                            cargar(0);
                        }),
                error -> Notificaciones.error(tabla, MensajesError.de(error)),
                cargando);
    }

    private record Opciones(List<Cliente> clientes, List<Producto> productos, List<Usuario> responsables) {
    }

    @FXML
    private void onDetalle() {
        VentaResumen elegida = tabla.getSelectionModel().getSelectedItem();
        if (elegida == null) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> service.obtener(elegida),
                venta -> DetalleVentaDialogo.mostrar(ventana(), venta),
                error -> Notificaciones.error(tabla, MensajesError.de(error)),
                cargando);
    }

    @FXML
    private void onRecargar() {
        cargar(paginacion.actual());
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }
}
