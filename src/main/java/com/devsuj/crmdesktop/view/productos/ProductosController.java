package com.devsuj.crmdesktop.view.productos;

import com.devsuj.crmdesktop.model.Categoria;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.util.Alertas;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.Formatos;
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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

/**
 * Catálogo de productos con inventario. Todos ven el catálogo; GERENTE y ADMIN además crean y
 * editan productos, los activan o desactivan, registran entradas/salidas, ven el kardex y la
 * alerta de stock bajo, y administran las categorías.
 */
public class ProductosController {

    private final ProductoService service;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(350));
    private Paginacion paginacion;
    private int ultimaSolicitud;

    @FXML
    private TextField campoBusqueda;
    @FXML
    private ComboBox<Categoria> filtroCategoria;
    @FXML
    private ComboBox<Boolean> filtroActivo;
    @FXML
    private ToggleButton botonStockBajo;
    @FXML
    private HBox accionesGestion;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonEstado;
    @FXML
    private Button botonMovimiento;
    @FXML
    private Button botonKardex;
    @FXML
    private TableView<Producto> tabla;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, String> colPrecio;
    @FXML
    private TableColumn<Producto, String> colPuntos;
    @FXML
    private TableColumn<Producto, Producto> colStock;
    @FXML
    private TableColumn<Producto, String> colMinimo;
    @FXML
    private TableColumn<Producto, String> colEstado;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;

    public ProductosController(ProductoService service, Permisos permisos) {
        this.service = service;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando,
                this::cargar, "productos");
        Tablas.ajustable(tabla);
        Tablas.texto(colNombre, Producto::nombre);
        Tablas.texto(colCategoria, Producto::categoriaNombre);
        Tablas.moneda(colPrecio, Producto::precio);
        Tablas.numero(colPuntos, Producto::precioPuntos);
        Tablas.numero(colMinimo, Producto::stockMinimo);
        Tablas.texto(colEstado, p -> p.activo() ? "Activo" : "Inactivo");
        colStock.setCellValueFactory(fila -> new ReadOnlyObjectWrapper<>(fila.getValue()));
        colStock.setCellFactory(c -> new StockCell());

        filtroCategoria.getItems().setAll((Categoria) null);
        EtiquetaListCell.aplicar(filtroCategoria, "Todas las categorías", Categoria::nombre);
        filtroActivo.getItems().setAll(null, Boolean.TRUE, Boolean.FALSE);
        EtiquetaListCell.aplicar(filtroActivo, "Activos e inactivos", a -> a ? "Solo activos" : "Solo inactivos");
        filtroActivo.setValue(Boolean.TRUE);
        filtroCategoria.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroActivo.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        botonStockBajo.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));
        esperaBusqueda.setOnFinished(evento -> cargar(0));
        campoBusqueda.textProperty().addListener((obs, antes, ahora) -> esperaBusqueda.playFromStart());

        configurarAcciones();
        capaCarga.visibleProperty().bind(cargando);
        Atajos.instalar(tabla, Map.of(
                KeyCombination.keyCombination("Shortcut+F"), campoBusqueda::requestFocus,
                new KeyCodeCombination(KeyCode.F5), this::onRecargar));
        cargarCategorias();
        cargar(0);
    }

    private void configurarAcciones() {
        boolean gestiona = permisos.puedeGestionarCatalogo();
        for (var nodo : List.of(accionesGestion, botonStockBajo)) {
            nodo.setVisible(gestiona);
            nodo.setManaged(gestiona);
        }
        BooleanBinding sinSeleccion = tabla.getSelectionModel().selectedItemProperty().isNull();
        botonEditar.disableProperty().bind(sinSeleccion.or(cargando));
        botonEstado.disableProperty().bind(sinSeleccion.or(cargando));
        botonMovimiento.disableProperty().bind(sinSeleccion.or(cargando));
        botonKardex.disableProperty().bind(sinSeleccion.or(cargando));
        botonEstado.textProperty().bind(Bindings.createStringBinding(() -> {
            Producto p = tabla.getSelectionModel().getSelectedItem();
            return p != null && !p.activo() ? "Activar" : "Desactivar";
        }, tabla.getSelectionModel().selectedItemProperty()));
        if (gestiona) {
            Tablas.alDobleClic(tabla, this::onEditar);
            Atajos.instalar(campoBusqueda, Map.of(KeyCombination.keyCombination("Shortcut+N"), this::onNuevo));
        }
    }

    private void cargarCategorias() {
        TareaEnSegundoPlano.ejecutar(service::categorias, categorias -> {
                    Categoria actual = filtroCategoria.getValue();
                    filtroCategoria.getItems().setAll((Categoria) null);
                    filtroCategoria.getItems().addAll(categorias);
                    filtroCategoria.setValue(actual);
                },
                error -> Notificaciones.advertencia(tabla, "No se pudieron cargar las categorías."), null);
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        boolean soloStockBajo = botonStockBajo.isSelected();
        String texto = campoBusqueda.getText();
        Categoria categoria = filtroCategoria.getValue();
        Boolean activo = filtroActivo.getValue();
        TareaEnSegundoPlano.ejecutar(
                // La lista de stock bajo no es paginada: se muestra completa en una sola página.
                () -> soloStockBajo
                        ? new Pagina<>(service.stockBajo(), 0, Integer.MAX_VALUE, 0, 1)
                        : service.buscar(texto, categoria, activo, numeroPagina),
                resultado -> {
                    if (solicitud == ultimaSolicitud) {
                        mostrarPagina(resultado, soloStockBajo);
                    }
                },
                error -> {
                    if (solicitud == ultimaSolicitud) {
                        tabla.getItems().clear();
                        paginacion.limpiar();
                        tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                                "No se pudieron cargar los productos. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    private void mostrarPagina(Pagina<Producto> resultado, boolean soloStockBajo) {
        tabla.getItems().setAll(resultado.contenido());
        if (soloStockBajo) {
            paginacion.mostrar(new Pagina<>(resultado.contenido(), 0, resultado.contenido().size(),
                    resultado.contenido().size(), resultado.estaVacia() ? 0 : 1));
            tabla.setPlaceholder(EstadoVacio.crear("fth-check-circle",
                    "Ningún producto está en su stock mínimo. No hay que resurtir.", null, null));
        } else {
            paginacion.mostrar(resultado);
            tabla.setPlaceholder(permisos.puedeGestionarCatalogo()
                    ? EstadoVacio.crear("fth-package", "No hay productos con esos filtros.", "Crear producto", this::onNuevo)
                    : EstadoVacio.crear("fth-package", "No hay productos con esos filtros.", null, null));
        }
    }

    @FXML
    private void onNuevo() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Producto p = tabla.getSelectionModel().getSelectedItem();
        if (p != null) {
            abrirFormulario(p);
        }
    }

    private void abrirFormulario(Producto original) {
        TareaEnSegundoPlano.ejecutar(service::categorias,
                categorias -> {
                    if (categorias.isEmpty()) {
                        Notificaciones.advertencia(tabla, "Primero crea una categoría (botón Categorías).");
                        return;
                    }
                    ProductoDialogo.mostrar(ventana(), service, original, categorias).ifPresent(guardado -> {
                        Notificaciones.exito(tabla, "Producto " + guardado.nombre()
                                + (original == null ? " creado." : " actualizado."));
                        cargar(paginacion.actual());
                    });
                },
                error -> Notificaciones.error(tabla, MensajesError.de(error)), cargando);
    }

    @FXML
    private void onCambiarEstado() {
        Producto p = tabla.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }
        boolean activar = !p.activo();
        if (!activar && !Alertas.confirmar(tabla, "Desactivar producto",
                "¿Desactivar " + p.nombre() + "?\n\nYa no se podrá vender ni canjear, pero su historial se conserva.",
                "Desactivar")) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> service.cambiarEstado(p, activar),
                actualizado -> {
                    Notificaciones.exito(tabla, actualizado.nombre() + (activar ? " activado." : " desactivado."));
                    cargar(paginacion.actual());
                },
                error -> Alertas.error(tabla, MensajesError.de(error)), cargando);
    }

    @FXML
    private void onMovimiento() {
        Producto p = tabla.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }
        MovimientoDialogo.mostrar(ventana(), service, p).ifPresent(m -> {
            Notificaciones.exito(tabla, m.tipo().etiqueta() + " de " + m.cantidad() + " registrada. Existencias: "
                    + Formatos.entero(m.stockResultante()) + ".");
            cargar(paginacion.actual());
        });
    }

    @FXML
    private void onKardex() {
        Producto p = tabla.getSelectionModel().getSelectedItem();
        if (p != null) {
            KardexDialogo.mostrar(ventana(), service, p);
        }
    }

    @FXML
    private void onCategorias() {
        if (CategoriasDialogo.mostrar(ventana(), service, permisos)) {
            cargarCategorias();
            cargar(paginacion.actual());
        }
    }

    @FXML
    private void onRecargar() {
        cargar(paginacion.actual());
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }

    /** Muestra las existencias y una etiqueta "Resurtir" cuando están en el mínimo o por debajo. */
    private static final class StockCell extends TableCell<Producto, Producto> {
        private final Label alerta = new Label("Resurtir");

        StockCell() {
            alerta.getStyleClass().addAll("etiqueta", "etiqueta-peligro");
            setContentDisplay(ContentDisplay.RIGHT);
            setGraphicTextGap(8);
        }

        @Override
        protected void updateItem(Producto producto, boolean vacia) {
            super.updateItem(producto, vacia);
            if (vacia || producto == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            setText(Formatos.entero(producto.stock()));
            setGraphic(producto.stockBajo() ? alerta : null);
        }
    }
}
