package com.devsuj.crmdesktop.view.tablero;

import com.devsuj.crmdesktop.model.ClienteConSaldo;
import com.devsuj.crmdesktop.model.Dashboard;
import com.devsuj.crmdesktop.model.EstadoCampana;
import com.devsuj.crmdesktop.model.MetricasCampana;
import com.devsuj.crmdesktop.model.PipelineEtapa;
import com.devsuj.crmdesktop.model.ProductoVendido;
import com.devsuj.crmdesktop.model.ReporteFidelizacion;
import com.devsuj.crmdesktop.model.ReporteSoporte;
import com.devsuj.crmdesktop.model.TicketsPorPrioridad;
import com.devsuj.crmdesktop.model.VentasPorCategoria;
import com.devsuj.crmdesktop.model.VentasPorMes;
import com.devsuj.crmdesktop.model.VentasPorVendedor;
import com.devsuj.crmdesktop.service.MetaService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.ReporteService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.Tablas;
import com.devsuj.crmdesktop.view.comun.Tarjetas;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;

/**
 * Tablero de inicio: indicadores del periodo (por defecto el mes en curso) y gráficas de ventas
 * por mes, pipeline, ventas por vendedor y por categoría. Todo viene ya filtrado por el alcance del
 * usuario: un VENDEDOR ve lo suyo, un GERENTE su equipo y un ADMIN todo.
 */
public class TableroController {

    private final ReporteService service;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);

    @FXML
    private Label saludo;
    @FXML
    private DatePicker filtroDesde;
    @FXML
    private DatePicker filtroHasta;
    @FXML
    private ProgressIndicator indicadorCarga;
    @FXML
    private Label mensajeError;
    @FXML
    private TilePane tarjetas;
    @FXML
    private BarChart<String, Number> graficaMeses;
    @FXML
    private NumberAxis ejeMeses;
    @FXML
    private BarChart<String, Number> graficaPipeline;
    @FXML
    private NumberAxis ejePipeline;
    @FXML
    private BarChart<String, Number> graficaVendedores;
    @FXML
    private NumberAxis ejeVendedores;
    @FXML
    private PieChart graficaCategorias;
    @FXML
    private ListView<ProductoVendido> masVendidos;
    @FXML
    private TilePane soporteTarjetas;
    @FXML
    private BarChart<String, Number> graficaPrioridad;
    @FXML
    private NumberAxis ejePrioridad;
    @FXML
    private TilePane fidelizacionTarjetas;
    @FXML
    private ListView<ClienteConSaldo> topClientes;
    @FXML
    private VBox seccionCampanas;
    @FXML
    private TableView<MetricasCampana> tablaCampanas;
    @FXML
    private TableColumn<MetricasCampana, String> colCampana;
    @FXML
    private TableColumn<MetricasCampana, EstadoCampana> colCampanaEstado;
    @FXML
    private TableColumn<MetricasCampana, String> colDestinatarios;
    @FXML
    private TableColumn<MetricasCampana, String> colApertura;
    @FXML
    private TableColumn<MetricasCampana, String> colRespuesta;
    @FXML
    private TableColumn<MetricasCampana, String> colCosto;

    public TableroController(ReporteService service, Permisos permisos) {
        this.service = service;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        saludo.setText("Hola, " + permisos.usuario().nombre().split(" ")[0] + ". "
                + switch (permisos.usuario().rol()) {
                    case VENDEDOR -> "Estos son tus resultados.";
                    case GERENTE -> "Estos son los resultados de tu equipo.";
                    case ADMIN -> "Estos son los resultados de toda la empresa.";
                });
        LocalDate hoy = Formatos.hoy();
        filtroDesde.setValue(hoy.withDayOfMonth(1));
        filtroHasta.setValue(hoy);
        filtroDesde.valueProperty().addListener((obs, antes, ahora) -> cargar());
        filtroHasta.valueProperty().addListener((obs, antes, ahora) -> cargar());
        indicadorCarga.visibleProperty().bind(cargando);
        mensajeError.visibleProperty().bind(mensajeError.textProperty().isNotEmpty());
        mensajeError.managedProperty().bind(mensajeError.visibleProperty());

        Graficas.ejeMoneda(ejeMeses, "Quetzales");
        Graficas.ejeMoneda(ejeVendedores, "Quetzales");
        ejePipeline.setLabel("Oportunidades");
        ejePipeline.setMinorTickVisible(false);
        ejePipeline.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number valor) {
                // Solo enteros: no existen "1.5 oportunidades".
                return valor.doubleValue() % 1 == 0 ? Formatos.entero(valor.longValue()) : "";
            }

            @Override
            public Number fromString(String texto) {
                return 0;
            }
        });
        for (var grafica : List.of(graficaMeses, graficaPipeline, graficaVendedores)) {
            grafica.setLegendVisible(false);
            grafica.setAnimated(false);
        }
        graficaCategorias.setAnimated(false);
        masVendidos.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(ProductoVendido p, boolean vacia) {
                super.updateItem(p, vacia);
                setText(vacia || p == null ? null
                        : p.productoNombre() + " · " + Formatos.entero(p.unidades()) + " u. · " + Formatos.moneda(p.total()));
            }
        });
        masVendidos.setPlaceholder(new Label("Sin ventas en el periodo."));
        configurarReportesNuevos();
        cargar();
    }

    /** Soporte, fidelización y efectividad de campañas (esta última solo para GERENTE y ADMIN). */
    private void configurarReportesNuevos() {
        ejePrioridad.setLabel("Tickets");
        ejePrioridad.setMinorTickVisible(false);
        ejePrioridad.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number valor) {
                return valor.doubleValue() % 1 == 0 ? Formatos.entero(valor.longValue()) : "";
            }

            @Override
            public Number fromString(String texto) {
                return 0;
            }
        });
        graficaPrioridad.setLegendVisible(false);
        graficaPrioridad.setAnimated(false);
        topClientes.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(ClienteConSaldo c, boolean vacia) {
                super.updateItem(c, vacia);
                setText(vacia || c == null ? null : c.clienteNombre() + " · " + Formatos.entero(c.saldo()) + " puntos");
            }
        });
        topClientes.setPlaceholder(new Label("Ningún cliente tiene puntos todavía."));

        boolean veCampanas = permisos.gestionaEquipo();
        seccionCampanas.setVisible(veCampanas);
        seccionCampanas.setManaged(veCampanas);
        Tablas.ajustable(tablaCampanas);
        Tablas.texto(colCampana, MetricasCampana::campanaNombre);
        Tablas.etiqueta(colCampanaEstado, MetricasCampana::estado, "campana-");
        Tablas.numero(colDestinatarios, MetricasCampana::destinatarios);
        Tablas.texto(colApertura, m -> Formatos.porcentaje(m.tasaApertura()));
        Tablas.texto(colRespuesta, m -> Formatos.porcentaje(m.tasaRespuesta()));
        Tablas.texto(colCosto, m -> m.costoPorRespuesta() == null ? "Nadie respondió" : Formatos.moneda(m.costoPorRespuesta()));
        tablaCampanas.setPlaceholder(new Label("Aún no hay campañas."));
    }

    private void mostrarSoporte(ReporteSoporte s) {
        soporteTarjetas.getChildren().setAll(
                Tarjetas.kpi("fth-life-buoy", "Tickets pendientes", Formatos.entero(s.abiertos() + s.enProceso()),
                        s.abiertos() + " abiertos · " + s.enProceso() + " en proceso"),
                Tarjetas.kpi("fth-user-x", "Sin asignar", Formatos.entero(s.sinAsignar()), "esperan responsable"),
                Tarjetas.kpi("fth-clock", "Cierre promedio",
                        s.horasPromedioCierre() == null ? "—" : Formatos.decimal(s.horasPromedioCierre()) + " h",
                        Formatos.entero(s.cerrados()) + " ticket(s) cerrados"));
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (TicketsPorPrioridad p : s.pendientesPorPrioridad()) {
            XYChart.Data<String, Number> dato = new XYChart.Data<>(p.prioridad().etiqueta(), p.cantidad());
            Graficas.tooltip(dato, "Prioridad " + p.prioridad().etiqueta().toLowerCase() + ": " + p.cantidad() + " pendiente(s)");
            serie.getData().add(dato);
        }
        graficaPrioridad.getData().setAll(List.of(serie));
    }

    private void mostrarFidelizacion(ReporteFidelizacion f) {
        fidelizacionTarjetas.getChildren().setAll(
                Tarjetas.kpi("fth-gift", "Puntos en circulación", Formatos.entero(f.saldoEnCirculacion()),
                        f.clientesConSaldo() + " cliente(s) con saldo"),
                Tarjetas.kpi("fth-repeat", "Emitidos / canjeados",
                        Formatos.entero(f.puntosEmitidos()) + " / " + Formatos.entero(f.puntosCanjeados()),
                        "puntos desde el inicio"));
        topClientes.getItems().setAll(f.topClientes());
    }

    @FXML
    private void onActualizar() {
        cargar();
    }

    @FXML
    private void onMesActual() {
        LocalDate hoy = Formatos.hoy();
        filtroDesde.setValue(hoy.withDayOfMonth(1));
        filtroHasta.setValue(hoy);
    }

    @FXML
    private void onAnioActual() {
        LocalDate hoy = Formatos.hoy();
        filtroDesde.setValue(hoy.withDayOfYear(1));
        filtroHasta.setValue(hoy);
    }

    private void cargar() {
        LocalDate desde = filtroDesde.getValue();
        LocalDate hasta = filtroHasta.getValue();
        mensajeError.setText("");
        boolean conCampanas = permisos.gestionaEquipo();
        TareaEnSegundoPlano.ejecutar(() -> service.tablero(desde, hasta, conCampanas), this::mostrar,
                error -> mensajeError.setText("No se pudo cargar el tablero. " + MensajesError.de(error)),
                cargando);
    }

    private void mostrar(ReporteService.Tablero tablero) {
        mostrarTarjetas(tablero.kpis());

        XYChart.Series<String, Number> meses = new XYChart.Series<>();
        for (VentasPorMes mes : tablero.porMes()) {
            String nombre = Month.of(mes.mes()).getDisplayName(TextStyle.SHORT, Formatos.GUATEMALA);
            XYChart.Data<String, Number> dato = new XYChart.Data<>(nombre, Graficas.numero(mes.total()));
            Graficas.tooltip(dato, MetaService.periodo(mes.anio(), mes.mes()) + ": " + Formatos.moneda(mes.total())
                    + " en " + mes.numeroVentas() + " venta(s)");
            meses.getData().add(dato);
        }
        graficaMeses.setTitle("Ventas por mes · " + (tablero.porMes().isEmpty() ? "" : tablero.porMes().getFirst().anio()));
        graficaMeses.getData().setAll(List.of(meses));

        XYChart.Series<String, Number> pipeline = new XYChart.Series<>();
        for (PipelineEtapa etapa : tablero.pipeline()) {
            XYChart.Data<String, Number> dato = new XYChart.Data<>(etapa.etapa().etiqueta(), etapa.cantidad());
            Graficas.tooltip(dato, etapa.etapa().etiqueta() + ": " + etapa.cantidad() + " oportunidad(es) · "
                    + Formatos.moneda(etapa.montoEstimado()));
            pipeline.getData().add(dato);
        }
        graficaPipeline.getData().setAll(List.of(pipeline));

        XYChart.Series<String, Number> vendedores = new XYChart.Series<>();
        for (VentasPorVendedor v : tablero.porVendedor()) {
            XYChart.Data<String, Number> dato = new XYChart.Data<>(v.usuarioNombre(), Graficas.numero(v.total()));
            Graficas.tooltip(dato, v.usuarioNombre() + ": " + Formatos.moneda(v.total()) + " · " + v.numeroVentas()
                    + " venta(s) · ticket promedio " + Formatos.moneda(v.ticketPromedio()));
            vendedores.getData().add(dato);
        }
        graficaVendedores.getData().setAll(List.of(vendedores));

        graficaCategorias.getData().setAll(FXCollections.observableArrayList());
        for (VentasPorCategoria c : tablero.porCategoria()) {
            PieChart.Data dato = new PieChart.Data(c.categoriaNombre(), Graficas.numero(c.total()));
            graficaCategorias.getData().add(dato);
            Graficas.tooltip(dato, c.categoriaNombre() + ": " + Formatos.moneda(c.total()) + " · "
                    + Formatos.entero(c.unidades()) + " unidades");
        }
        masVendidos.getItems().setAll(tablero.masVendidos());
        mostrarSoporte(tablero.soporte());
        mostrarFidelizacion(tablero.fidelizacion());
        tablaCampanas.getItems().setAll(tablero.campanas());
    }

    private void mostrarTarjetas(Dashboard k) {
        tarjetas.getChildren().setAll(
                Tarjetas.kpi("fth-dollar-sign", "Total vendido", Formatos.moneda(k.totalVendido()),
                        Formatos.entero(k.numeroVentas()) + " venta(s) · ticket promedio " + Formatos.moneda(k.ticketPromedio())),
                Tarjetas.kpi("fth-flag", "Meta del mes",
                        k.metaMes() == null || k.metaMes().signum() == 0 ? "Sin meta" : Formatos.porcentaje(k.avanceMetaMes()),
                        "Vendido " + Formatos.moneda(k.vendidoMes())
                                + (k.metaMes() == null || k.metaMes().signum() == 0 ? "" : " de " + Formatos.moneda(k.metaMes()))),
                Tarjetas.kpi("fth-target", "Pipeline abierto", Formatos.moneda(k.montoPipeline()),
                        Formatos.entero(k.oportunidadesAbiertas()) + " oportunidad(es) abiertas"),
                Tarjetas.kpi("fth-percent", "Tasa de conversión", Formatos.porcentaje(k.tasaConversion()),
                        k.oportunidadesGanadas() + " ganadas · " + k.oportunidadesPerdidas() + " perdidas"),
                Tarjetas.kpi("fth-users", "Clientes activos", Formatos.entero(k.clientesActivos()),
                        k.clientesProspecto() + " prospecto(s) · " + k.clientesNuevos() + " nuevo(s) en el periodo"),
                Tarjetas.kpi("fth-package", "Stock bajo", Formatos.entero(k.productosStockBajo()),
                        "producto(s) por resurtir · " + k.ticketsAbiertos() + " ticket(s) de soporte abiertos"));
    }
}
