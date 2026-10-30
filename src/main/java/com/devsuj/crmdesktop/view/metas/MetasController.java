package com.devsuj.crmdesktop.view.metas;

import com.devsuj.crmdesktop.model.MetaVenta;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.RendimientoVendedor;
import com.devsuj.crmdesktop.model.Rol;
import com.devsuj.crmdesktop.service.MetaService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.ReporteService;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.util.Alertas;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.AvanceCell;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Paginacion;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.util.stream.IntStream;

/**
 * Metas mensuales y rendimiento de los vendedores en el mes elegido. Todos ven (VENDEDOR lo suyo,
 * GERENTE su equipo); GERENTE y ADMIN crean, editan y eliminan metas.
 */
public class MetasController {

    private final MetaService service;
    private final ReporteService reporteService;
    private final UsuarioService usuarioService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final BooleanProperty cargandoRendimiento = new SimpleBooleanProperty(false);
    private Paginacion paginacion;

    @FXML
    private ComboBox<Integer> filtroMes;
    @FXML
    private ComboBox<Integer> filtroAnio;
    @FXML
    private HBox accionesGestion;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonEliminar;
    @FXML
    private TableView<MetaVenta> tabla;
    @FXML
    private TableColumn<MetaVenta, String> colVendedor;
    @FXML
    private TableColumn<MetaVenta, String> colPeriodo;
    @FXML
    private TableColumn<MetaVenta, String> colObjetivo;
    @FXML
    private TableColumn<MetaVenta, String> colVendido;
    @FXML
    private TableColumn<MetaVenta, String> colVentas;
    @FXML
    private TableColumn<MetaVenta, BigDecimal> colAvance;
    @FXML
    private TableColumn<MetaVenta, String> colFaltante;
    @FXML
    private VBox capaCarga;
    @FXML
    private Label etiquetaPaginacion;
    @FXML
    private Button botonAnterior;
    @FXML
    private Button botonSiguiente;
    @FXML
    private Label tituloRendimiento;
    @FXML
    private TableView<RendimientoVendedor> tablaRendimiento;
    @FXML
    private TableColumn<RendimientoVendedor, String> colRVendedor;
    @FXML
    private TableColumn<RendimientoVendedor, String> colRVentas;
    @FXML
    private TableColumn<RendimientoVendedor, String> colRTotal;
    @FXML
    private TableColumn<RendimientoVendedor, String> colRMeta;
    @FXML
    private TableColumn<RendimientoVendedor, BigDecimal> colRAvance;
    @FXML
    private TableColumn<RendimientoVendedor, String> colRGanadas;
    @FXML
    private TableColumn<RendimientoVendedor, String> colRPerdidas;
    @FXML
    private TableColumn<RendimientoVendedor, String> colRConversion;
    @FXML
    private VBox capaCargaRendimiento;

    public MetasController(MetaService service, ReporteService reporteService, UsuarioService usuarioService,
                           Permisos permisos) {
        this.service = service;
        this.reporteService = reporteService;
        this.usuarioService = usuarioService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando, this::cargar, "metas");
        Tablas.ajustable(tabla);
        Tablas.texto(colVendedor, MetaVenta::usuarioNombre);
        Tablas.texto(colPeriodo, m -> MetaService.periodo(m.anio(), m.mes()));
        Tablas.moneda(colObjetivo, MetaVenta::montoObjetivo);
        Tablas.moneda(colVendido, MetaVenta::montoVendido);
        Tablas.numero(colVentas, MetaVenta::numeroVentas);
        AvanceCell.aplicar(colAvance, MetaVenta::porcentajeAvance);
        Tablas.texto(colFaltante, m -> m.cumplida() ? "Cumplida" : Formatos.moneda(m.faltante()));

        Tablas.ajustable(tablaRendimiento);
        Tablas.texto(colRVendedor, RendimientoVendedor::usuarioNombre);
        Tablas.numero(colRVentas, RendimientoVendedor::numeroVentas);
        Tablas.moneda(colRTotal, RendimientoVendedor::totalVendido);
        Tablas.texto(colRMeta, r -> r.metaObjetivo() == null ? "Sin meta" : Formatos.moneda(r.metaObjetivo()));
        AvanceCell.aplicar(colRAvance, RendimientoVendedor::avanceMeta);
        Tablas.numero(colRGanadas, RendimientoVendedor::oportunidadesGanadas);
        Tablas.numero(colRPerdidas, RendimientoVendedor::oportunidadesPerdidas);
        Tablas.texto(colRConversion, r -> Formatos.porcentaje(r.tasaConversion()));

        int anio = Formatos.hoy().getYear();
        filtroMes.getItems().setAll(IntStream.rangeClosed(1, 12).boxed().toList());
        EtiquetaListCell.aplicar(filtroMes, "", m -> MetaService.periodo(2000, m).replace(" 2000", ""));
        filtroMes.setValue(Formatos.hoy().getMonthValue());
        filtroAnio.getItems().setAll(IntStream.rangeClosed(anio - 2, anio + 1).boxed().toList());
        EtiquetaListCell.aplicar(filtroAnio, "", String::valueOf);
        filtroAnio.setValue(anio);
        filtroMes.valueProperty().addListener((obs, antes, ahora) -> recargarTodo());
        filtroAnio.valueProperty().addListener((obs, antes, ahora) -> recargarTodo());

        boolean gestiona = permisos.gestionaEquipo();
        accionesGestion.setVisible(gestiona);
        accionesGestion.setManaged(gestiona);
        BooleanBinding sinSeleccion = tabla.getSelectionModel().selectedItemProperty().isNull();
        botonEditar.disableProperty().bind(sinSeleccion.or(cargando));
        botonEliminar.disableProperty().bind(sinSeleccion.or(cargando));
        if (gestiona) {
            Tablas.alDobleClic(tabla, this::onEditar);
        }
        capaCarga.visibleProperty().bind(cargando);
        capaCargaRendimiento.visibleProperty().bind(cargandoRendimiento);
        recargarTodo();
    }

    private void recargarTodo() {
        cargar(0);
        cargarRendimiento();
    }

    private void cargar(int numeroPagina) {
        Integer anio = filtroAnio.getValue();
        Integer mes = filtroMes.getValue();
        TareaEnSegundoPlano.ejecutar(() -> service.buscar(null, anio, mes, numeroPagina),
                (Pagina<MetaVenta> resultado) -> {
                    tabla.getItems().setAll(resultado.contenido());
                    paginacion.mostrar(resultado);
                    tabla.setPlaceholder(permisos.gestionaEquipo()
                            ? EstadoVacio.crear("fth-flag", "No hay metas para este mes.", "Crear meta", this::onNueva)
                            : EstadoVacio.crear("fth-flag", "No tienes meta asignada para este mes.", null, null));
                },
                error -> {
                    tabla.getItems().clear();
                    paginacion.limpiar();
                    tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                            "No se pudieron cargar las metas. " + MensajesError.de(error), "Reintentar",
                            () -> cargar(paginacion.actual())));
                },
                cargando);
    }

    private void cargarRendimiento() {
        int anio = filtroAnio.getValue();
        int mes = filtroMes.getValue();
        tituloRendimiento.setText("Rendimiento de vendedores · " + MetaService.periodo(anio, mes));
        TareaEnSegundoPlano.ejecutar(() -> reporteService.rendimiento(anio, mes),
                lista -> {
                    tablaRendimiento.getItems().setAll(lista);
                    tablaRendimiento.setPlaceholder(EstadoVacio.crear("fth-bar-chart-2",
                            "Sin datos de rendimiento para este mes.", null, null));
                },
                error -> tablaRendimiento.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                        "No se pudo cargar el rendimiento. " + MensajesError.de(error), "Reintentar", this::cargarRendimiento)),
                cargandoRendimiento);
    }

    @FXML
    private void onNueva() {
        abrirFormulario(null);
    }

    @FXML
    private void onEditar() {
        MetaVenta meta = tabla.getSelectionModel().getSelectedItem();
        if (meta != null && permisos.gestionaEquipo()) {
            abrirFormulario(meta);
        }
    }

    private void abrirFormulario(MetaVenta original) {
        TareaEnSegundoPlano.ejecutar(
                () -> usuarioService.responsables().stream().filter(u -> u.rol() == Rol.VENDEDOR).toList(),
                vendedores -> MetaDialogo.mostrar(ventana(), service, original, vendedores,
                                filtroAnio.getValue(), filtroMes.getValue())
                        .ifPresent(meta -> {
                            Notificaciones.exito(tabla, "Meta de " + meta.usuarioNombre() + " guardada.");
                            recargarTodo();
                        }),
                error -> Notificaciones.error(tabla, MensajesError.de(error)),
                cargando);
    }

    @FXML
    private void onEliminar() {
        MetaVenta meta = tabla.getSelectionModel().getSelectedItem();
        if (meta == null || !Alertas.confirmar(tabla, "Eliminar meta", "¿Eliminar la meta de " + meta.usuarioNombre()
                + " para " + MetaService.periodo(meta.anio(), meta.mes()) + "?\n\nLas ventas no se modifican.", "Eliminar")) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> {
                    service.eliminar(meta);
                    return Boolean.TRUE;
                },
                ok -> {
                    Notificaciones.exito(tabla, "Meta eliminada.");
                    recargarTodo();
                },
                error -> Alertas.error(tabla, MensajesError.de(error)), cargando);
    }

    @FXML
    private void onRecargar() {
        recargarTodo();
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }
}
