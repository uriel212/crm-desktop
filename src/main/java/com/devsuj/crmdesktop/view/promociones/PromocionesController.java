package com.devsuj.crmdesktop.view.promociones;

import com.devsuj.crmdesktop.model.AlcancePromocion;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.ProductoDePromocion;
import com.devsuj.crmdesktop.model.Promocion;
import com.devsuj.crmdesktop.service.CampanaService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.service.PromocionService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Promociones: todos consultan (incluidas las vigentes hoy); GERENTE y ADMIN crean, editan y
 * activan o desactivan; solo ADMIN borra (y solo si nunca se aplicó en una venta).
 */
public class PromocionesController {

    private final PromocionService service;
    private final CampanaService campanaService;
    private final ProductoService productoService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private Paginacion paginacion;
    private int ultimaSolicitud;

    @FXML
    private ComboBox<Boolean> filtroActiva;
    @FXML
    private ComboBox<AlcancePromocion> filtroAlcance;
    @FXML
    private ToggleButton botonVigentes;
    @FXML
    private HBox accionesGestion;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonEstado;
    @FXML
    private Button botonEliminar;
    @FXML
    private TableView<Promocion> tabla;
    @FXML
    private TableColumn<Promocion, String> colNombre;
    @FXML
    private TableColumn<Promocion, String> colDescuento;
    @FXML
    private TableColumn<Promocion, AlcancePromocion> colAlcance;
    @FXML
    private TableColumn<Promocion, String> colMinimo;
    @FXML
    private TableColumn<Promocion, String> colVigencia;
    @FXML
    private TableColumn<Promocion, String> colCampana;
    @FXML
    private TableColumn<Promocion, String> colEstado;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;

    public PromocionesController(PromocionService service, CampanaService campanaService,
                                 ProductoService productoService, Permisos permisos) {
        this.service = service;
        this.campanaService = campanaService;
        this.productoService = productoService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando, this::cargar, "promociones");
        Tablas.ajustable(tabla);
        Tablas.texto(colNombre, Promocion::nombre);
        Tablas.texto(colDescuento, PromocionService::descripcionValor);
        Tablas.etiqueta(colAlcance, Promocion::alcance, "alcance-");
        Tablas.texto(colMinimo, p -> p.montoMinimo() == null ? null : Formatos.moneda(p.montoMinimo()));
        Tablas.texto(colVigencia, p -> Formatos.fecha(p.fechaInicio()) + " – " + Formatos.fecha(p.fechaFin()));
        Tablas.texto(colCampana, Promocion::campanaNombre);
        Tablas.texto(colEstado, p -> p.vigente() ? "Vigente hoy" : p.activa() ? "Activa (fuera de fecha o campaña)" : "Inactiva");
        Tablas.tooltipDeFila(tabla, p -> p.productos().isEmpty() ? null
                : "Productos: " + p.productos().stream().map(ProductoDePromocion::nombre).collect(Collectors.joining(", ")));

        filtroActiva.getItems().setAll(null, Boolean.TRUE, Boolean.FALSE);
        EtiquetaListCell.aplicar(filtroActiva, "Activas e inactivas", a -> a ? "Solo activas" : "Solo inactivas");
        EtiquetaListCell.filtro(filtroAlcance, "Todos los alcances", AlcancePromocion.values());
        filtroActiva.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroAlcance.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        botonVigentes.selectedProperty().addListener((obs, antes, ahora) -> cargar(0));

        boolean gestiona = permisos.gestionaEquipo();
        accionesGestion.setVisible(gestiona);
        accionesGestion.setManaged(gestiona);
        botonEliminar.setVisible(permisos.esAdmin());
        botonEliminar.setManaged(permisos.esAdmin());
        BooleanBinding sinSeleccion = tabla.getSelectionModel().selectedItemProperty().isNull();
        botonEditar.disableProperty().bind(sinSeleccion.or(cargando));
        botonEstado.disableProperty().bind(sinSeleccion.or(cargando));
        botonEliminar.disableProperty().bind(sinSeleccion.or(cargando));
        botonEstado.textProperty().bind(Bindings.createStringBinding(() -> {
            Promocion p = tabla.getSelectionModel().getSelectedItem();
            return p != null && !p.activa() ? "Activar" : "Desactivar";
        }, tabla.getSelectionModel().selectedItemProperty()));
        if (gestiona) {
            Tablas.alDobleClic(tabla, this::onEditar);
        }
        capaCarga.visibleProperty().bind(cargando);
        cargar(0);
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        boolean soloVigentes = botonVigentes.isSelected();
        Boolean activa = filtroActiva.getValue();
        AlcancePromocion alcance = filtroAlcance.getValue();
        TareaEnSegundoPlano.ejecutar(
                // Las vigentes no vienen paginadas: se muestran todas en una página.
                () -> soloVigentes ? enUnaPagina(service.vigentes()) : service.buscar(activa, alcance, numeroPagina),
                (Pagina<Promocion> resultado) -> {
                    if (solicitud != ultimaSolicitud) {
                        return;
                    }
                    tabla.getItems().setAll(resultado.contenido());
                    paginacion.mostrar(resultado);
                    tabla.setPlaceholder(permisos.gestionaEquipo()
                            ? EstadoVacio.crear("fth-tag", "No hay promociones con esos filtros.", "Crear promoción", this::onNueva)
                            : EstadoVacio.crear("fth-tag", "No hay promociones con esos filtros.", null, null));
                },
                error -> {
                    if (solicitud == ultimaSolicitud) {
                        tabla.getItems().clear();
                        paginacion.limpiar();
                        tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                                "No se pudieron cargar las promociones. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    private static Pagina<Promocion> enUnaPagina(List<Promocion> lista) {
        return new Pagina<>(lista, 0, Math.max(1, lista.size()), lista.size(), lista.isEmpty() ? 0 : 1);
    }

    @FXML
    private void onNueva() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        Promocion p = tabla.getSelectionModel().getSelectedItem();
        if (p != null && permisos.gestionaEquipo()) {
            abrirFormulario(p);
        }
    }

    /** Carga campañas abiertas y productos activos y abre el formulario. */
    private void abrirFormulario(Promocion original) {
        TareaEnSegundoPlano.ejecutar(
                () -> new Opciones(campanaService.abiertas(), productoService.paraVenta()),
                opciones -> PromocionDialogo.mostrar(ventana(), service, original, opciones.campanas(), opciones.productos())
                        .ifPresent(guardada -> {
                            Notificaciones.exito(tabla, "Promoción " + guardada.nombre()
                                    + (original == null ? " creada." : " actualizada."));
                            cargar(paginacion.actual());
                        }),
                error -> Notificaciones.error(tabla, MensajesError.de(error)), cargando);
    }

    private record Opciones(List<Campana> campanas, List<Producto> productos) {
    }

    @FXML
    private void onCambiarEstado() {
        Promocion p = tabla.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }
        boolean activar = !p.activa();
        TareaEnSegundoPlano.ejecutar(() -> service.cambiarEstado(p, activar),
                actualizada -> {
                    Notificaciones.exito(tabla, actualizada.nombre() + (activar ? " activada." : " desactivada."));
                    cargar(paginacion.actual());
                },
                error -> Alertas.error(tabla, MensajesError.de(error)), cargando);
    }

    @FXML
    private void onEliminar() {
        Promocion p = tabla.getSelectionModel().getSelectedItem();
        if (p == null || !Alertas.confirmar(tabla, "Eliminar promoción", "¿Eliminar la promoción " + p.nombre()
                + "?\n\nSolo se puede si nunca se aplicó en una venta; si ya se usó, desactívala.", "Eliminar")) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> {
                    service.eliminar(p);
                    return Boolean.TRUE;
                },
                ok -> {
                    Notificaciones.exito(tabla, "Promoción eliminada.");
                    cargar(paginacion.actual());
                },
                error -> Alertas.error(tabla, MensajesError.de(error)), cargando);
    }

    @FXML
    private void onRecargar() {
        cargar(paginacion.actual());
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }
}
