package com.devsuj.crmdesktop.view.fidelizacion;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.MovimientoPuntos;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.SaldoPuntos;
import com.devsuj.crmdesktop.model.TipoMovimientoPuntos;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.service.FidelizacionService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.Paginacion;
import com.devsuj.crmdesktop.view.comun.Selectores;
import com.devsuj.crmdesktop.view.comun.Tablas;
import com.devsuj.crmdesktop.view.comun.Tarjetas;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;

/**
 * Fidelización: saldo y estado de cuenta de puntos de un cliente, y canje por productos.
 * Todos consultan; canjear solo el responsable del cliente, su gerente o un ADMIN.
 */
public class FidelizacionController {

    private final FidelizacionService service;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final ObjectProperty<SaldoPuntos> saldo = new SimpleObjectProperty<>();
    private final SearchableComboBox<Cliente> selectorCliente = Selectores.clientes(List.of());
    private Paginacion paginacion;

    @FXML
    private StackPane contenedorCliente;
    @FXML
    private Button botonCanjear;
    @FXML
    private TilePane tarjetas;
    @FXML
    private TableView<MovimientoPuntos> tabla;
    @FXML
    private TableColumn<MovimientoPuntos, String> colFecha;
    @FXML
    private TableColumn<MovimientoPuntos, TipoMovimientoPuntos> colTipo;
    @FXML
    private TableColumn<MovimientoPuntos, String> colPuntos;
    @FXML
    private TableColumn<MovimientoPuntos, String> colMotivo;
    @FXML
    private TableColumn<MovimientoPuntos, String> colUsuario;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;

    public FidelizacionController(FidelizacionService service, ClienteService clienteService,
                                  ProductoService productoService, Permisos permisos) {
        this.service = service;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando,
                this::cargarMovimientos, "movimientos");
        Tablas.ajustable(tabla);
        Tablas.fechaHora(colFecha, MovimientoPuntos::fecha);
        Tablas.etiqueta(colTipo, MovimientoPuntos::tipo, "puntos-");
        // Los puntos llegan siempre positivos: el signo lo pone el tipo.
        Tablas.numero(colPuntos, m -> m.tipo() == TipoMovimientoPuntos.CANJEADO ? -m.puntos() : m.puntos());
        Tablas.texto(colMotivo, m -> m.tipo() == TipoMovimientoPuntos.CANJEADO && m.productoNombre() != null
                ? m.productoNombre() + " × " + m.cantidad()
                : m.motivo() != null ? m.motivo() : m.ventaId() == null ? null : "Venta #" + m.ventaId());
        Tablas.texto(colUsuario, MovimientoPuntos::usuarioNombre);

        selectorCliente.setPromptText("Elige un cliente para ver sus puntos");
        contenedorCliente.getChildren().setAll(selectorCliente);
        selectorCliente.valueProperty().addListener((obs, antes, ahora) -> cargarTodo());
        botonCanjear.disableProperty().bind(Bindings.createBooleanBinding(
                () -> saldo.get() == null || !permisos.puedeGestionarCliente(selectorCliente.getValue()),
                saldo, selectorCliente.valueProperty()).or(cargando));
        capaCarga.visibleProperty().bind(cargando);
        tabla.setPlaceholder(EstadoVacio.crear("fth-gift", "Elige un cliente para ver su saldo y su estado de cuenta.",
                null, null));

        TareaEnSegundoPlano.ejecutar(() -> clienteService.paraSeleccion(null),
                clientes -> selectorCliente.getItems().setAll(clientes),
                error -> Notificaciones.error(tabla, MensajesError.de(error)), cargando);
    }

    private void cargarTodo() {
        Cliente cliente = selectorCliente.getValue();
        saldo.set(null);
        tarjetas.getChildren().clear();
        if (cliente == null) {
            tabla.getItems().clear();
            paginacion.limpiar();
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> service.saldo(cliente), this::mostrarSaldo,
                error -> Notificaciones.error(tabla, MensajesError.de(error)), null);
        cargarMovimientos(0);
    }

    private void mostrarSaldo(SaldoPuntos s) {
        saldo.set(s);
        tarjetas.getChildren().setAll(
                Tarjetas.kpi("fth-gift", "Saldo disponible", Formatos.entero(s.saldo()), "puntos para canjear"),
                Tarjetas.kpi("fth-trending-up", "Puntos ganados", Formatos.entero(s.puntosGanados()),
                        "1 punto por cada Q100 de compra"),
                Tarjetas.kpi("fth-shopping-bag", "Puntos canjeados", Formatos.entero(s.puntosCanjeados()),
                        "en productos del catálogo"));
    }

    private void cargarMovimientos(int pagina) {
        Cliente cliente = selectorCliente.getValue();
        if (cliente == null) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> service.movimientos(cliente, pagina),
                (Pagina<MovimientoPuntos> resultado) -> {
                    tabla.getItems().setAll(resultado.contenido());
                    paginacion.mostrar(resultado);
                    tabla.setPlaceholder(EstadoVacio.crear("fth-gift",
                            cliente.nombre() + " aún no tiene movimientos de puntos. Los gana al comprar.", null, null));
                },
                error -> {
                    tabla.getItems().clear();
                    paginacion.limpiar();
                    tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                            "No se pudo cargar el estado de cuenta. " + MensajesError.de(error),
                            "Reintentar", () -> cargarMovimientos(paginacion.actual())));
                },
                cargando);
    }

    /** Carga los productos canjeables y abre el diálogo de canje. */
    @FXML
    private void onCanjear() {
        Cliente cliente = selectorCliente.getValue();
        SaldoPuntos actual = saldo.get();
        if (cliente == null || actual == null) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(productoService::canjeables,
                canjeables -> {
                    if (canjeables.isEmpty()) {
                        Notificaciones.advertencia(tabla, "No hay productos con precio en puntos en el catálogo.");
                        return;
                    }
                    CanjeDialogo.mostrar(ventana(), service, cliente, actual, canjeables).ifPresent(canje -> {
                        Notificaciones.exito(tabla, "Canje registrado: " + canje.movimiento().productoNombre()
                                + ". Saldo restante: " + Formatos.entero(canje.saldo()) + " puntos.");
                        cargarTodo();
                    });
                },
                error -> Notificaciones.error(tabla, MensajesError.de(error)), cargando);
    }

    @FXML
    private void onRecargar() {
        cargarTodo();
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }
}
