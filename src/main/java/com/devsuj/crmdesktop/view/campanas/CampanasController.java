package com.devsuj.crmdesktop.view.campanas;

import com.devsuj.crmdesktop.model.CanalCampana;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.Destinatario;
import com.devsuj.crmdesktop.model.EstadoCampana;
import com.devsuj.crmdesktop.model.MetricasCampana;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.ResultadoCampana;
import com.devsuj.crmdesktop.service.CampanaService;
import com.devsuj.crmdesktop.service.ClienteService;
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
import com.devsuj.crmdesktop.view.comun.Tarjetas;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Campañas de marketing: listado a la izquierda y, a la derecha, el detalle de la campaña elegida
 * con sus métricas (GERENTE y ADMIN) y sus destinatarios.
 *
 * <p>GERENTE y ADMIN crean, editan, cambian el estado y administran destinatarios. El resultado de
 * cada destinatario (abierto, respondió) lo registra su vendedor, su gerente o un ADMIN.</p>
 */
public class CampanasController {

    private final CampanaService service;
    private final ClienteService clienteService;
    private final Permisos permisos;
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final BooleanProperty cargandoDetalle = new SimpleBooleanProperty(false);
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(350));
    private Paginacion paginacion;
    private Paginacion paginacionDestinatarios;
    private int ultimaSolicitud;

    @FXML
    private TextField campoBusqueda;
    @FXML
    private ComboBox<EstadoCampana> filtroEstado;
    @FXML
    private ComboBox<CanalCampana> filtroCanal;
    @FXML
    private HBox accionesGestion;
    @FXML
    private Button botonEditar;
    @FXML
    private Button botonEstado;
    @FXML
    private TableView<Campana> tabla;
    @FXML
    private TableColumn<Campana, String> colNombre;
    @FXML
    private TableColumn<Campana, CanalCampana> colCanal;
    @FXML
    private TableColumn<Campana, String> colVigencia;
    @FXML
    private TableColumn<Campana, EstadoCampana> colEstado;
    @FXML
    private TableColumn<Campana, String> colDestinatarios;
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
    private TilePane metricas;
    @FXML
    private ComboBox<ResultadoCampana> filtroResultado;
    @FXML
    private HBox accionesDestinatarios;
    @FXML
    private Button botonQuitar;
    @FXML
    private Button botonAbierto;
    @FXML
    private Button botonRespondio;
    @FXML
    private TableView<Destinatario> tablaDestinatarios;
    @FXML
    private TableColumn<Destinatario, String> colCliente;
    @FXML
    private TableColumn<Destinatario, String> colEmail;
    @FXML
    private TableColumn<Destinatario, String> colVendedor;
    @FXML
    private TableColumn<Destinatario, ResultadoCampana> colResultado;
    @FXML
    private VBox capaCargaDetalle;
    @FXML
    private Label etiquetaPaginacionDestinatarios;
    @FXML
    private Button botonAnteriorDestinatarios;
    @FXML
    private Button botonSiguienteDestinatarios;

    public CampanasController(CampanaService service, ClienteService clienteService, Permisos permisos) {
        this.service = service;
        this.clienteService = clienteService;
        this.permisos = permisos;
    }

    @FXML
    private void initialize() {
        paginacion = new Paginacion(botonAnterior, botonSiguiente, etiquetaPaginacion, cargando, this::cargar, "campañas");
        paginacionDestinatarios = new Paginacion(botonAnteriorDestinatarios, botonSiguienteDestinatarios,
                etiquetaPaginacionDestinatarios, cargandoDetalle, this::cargarDestinatarios, "destinatarios");
        Tablas.ajustable(tabla);
        Tablas.texto(colNombre, Campana::nombre);
        Tablas.etiqueta(colCanal, Campana::canal, "canal-");
        Tablas.texto(colVigencia, c -> Formatos.fecha(c.fechaInicio()) + " – " + Formatos.fecha(c.fechaFin()));
        Tablas.etiqueta(colEstado, Campana::estado, "campana-");
        Tablas.numero(colDestinatarios, Campana::totalDestinatarios);
        Tablas.ajustable(tablaDestinatarios);
        Tablas.texto(colCliente, Destinatario::clienteNombre);
        Tablas.texto(colEmail, Destinatario::clienteEmail);
        Tablas.texto(colVendedor, Destinatario::vendedorNombre);
        Tablas.etiqueta(colResultado, Destinatario::resultado, "resultado-");

        EtiquetaListCell.filtro(filtroEstado, "Todos los estados", EstadoCampana.values());
        EtiquetaListCell.filtro(filtroCanal, "Todos los canales", CanalCampana.values());
        EtiquetaListCell.filtro(filtroResultado, "Todos los resultados", ResultadoCampana.values());
        filtroEstado.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroCanal.valueProperty().addListener((obs, antes, ahora) -> cargar(0));
        filtroResultado.valueProperty().addListener((obs, antes, ahora) -> cargarDestinatarios(0));
        esperaBusqueda.setOnFinished(evento -> cargar(0));
        campoBusqueda.textProperty().addListener((obs, antes, ahora) -> esperaBusqueda.playFromStart());
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> mostrarDetalle());

        configurarAcciones();
        capaCarga.visibleProperty().bind(cargando);
        capaCargaDetalle.visibleProperty().bind(cargandoDetalle);
        mostrarDetalle();
        cargar(0);
    }

    private void configurarAcciones() {
        boolean gestiona = permisos.gestionaEquipo();
        for (var nodo : new javafx.scene.Node[]{accionesGestion, accionesDestinatarios}) {
            nodo.setVisible(gestiona);
            nodo.setManaged(gestiona);
        }
        var seleccion = tabla.getSelectionModel().selectedItemProperty();
        BooleanBinding abierta = Bindings.createBooleanBinding(
                () -> seleccion.get() != null && !seleccion.get().estado().estaCerrada(), seleccion);
        botonEditar.disableProperty().bind(abierta.not().or(cargando));
        botonEstado.disableProperty().bind(seleccion.isNull().or(cargando));
        if (gestiona) {
            Tablas.alDobleClic(tabla, this::onEditar);
        }
        var destinatario = tablaDestinatarios.getSelectionModel().selectedItemProperty();
        botonQuitar.disableProperty().bind(Bindings.createBooleanBinding(() -> destinatario.get() == null
                || seleccion.get() == null || seleccion.get().estado() != EstadoCampana.PLANEADA, destinatario, seleccion));
        botonAbierto.disableProperty().bind(puedeAvanzarA(ResultadoCampana.ABIERTO).not().or(cargandoDetalle));
        botonRespondio.disableProperty().bind(puedeAvanzarA(ResultadoCampana.RESPONDIO).not().or(cargandoDetalle));
    }

    /** El resultado solo avanza, con la campaña activa o finalizada, y lo cambia quien atiende al cliente. */
    private BooleanBinding puedeAvanzarA(ResultadoCampana destino) {
        var campana = tabla.getSelectionModel().selectedItemProperty();
        var destinatario = tablaDestinatarios.getSelectionModel().selectedItemProperty();
        return Bindings.createBooleanBinding(() -> destinatario.get() != null
                && CampanaService.admiteResultados(campana.get())
                && destinatario.get().resultado().puedeCambiarA(destino)
                && permisos.puedeGestionarDe(destinatario.get().vendedorId()), campana, destinatario);
    }

    private void cargar(int numeroPagina) {
        int solicitud = ++ultimaSolicitud;
        EstadoCampana estado = filtroEstado.getValue();
        CanalCampana canal = filtroCanal.getValue();
        String texto = campoBusqueda.getText();
        TareaEnSegundoPlano.ejecutar(() -> service.buscar(estado, canal, texto, numeroPagina),
                (Pagina<Campana> resultado) -> {
                    if (solicitud != ultimaSolicitud) {
                        return;
                    }
                    Integer elegida = tabla.getSelectionModel().getSelectedItem() == null
                            ? null : tabla.getSelectionModel().getSelectedItem().id();
                    tabla.getItems().setAll(resultado.contenido());
                    paginacion.mostrar(resultado);
                    tabla.getItems().stream().filter(c -> c.id().equals(elegida)).findFirst()
                            .ifPresent(c -> tabla.getSelectionModel().select(c));
                    tabla.setPlaceholder(permisos.gestionaEquipo()
                            ? EstadoVacio.crear("fth-send", "No hay campañas con esos filtros.", "Crear campaña", this::onNueva)
                            : EstadoVacio.crear("fth-send", "No hay campañas con esos filtros.", null, null));
                },
                error -> {
                    if (solicitud == ultimaSolicitud) {
                        tabla.getItems().clear();
                        paginacion.limpiar();
                        tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                                "No se pudieron cargar las campañas. " + MensajesError.de(error),
                                "Reintentar", () -> cargar(paginacion.actual())));
                    }
                },
                cargando);
    }

    private void mostrarDetalle() {
        Campana c = tabla.getSelectionModel().getSelectedItem();
        metricas.getChildren().clear();
        tablaDestinatarios.getItems().clear();
        paginacionDestinatarios.limpiar();
        if (c == null) {
            detalleTitulo.setText("Elige una campaña");
            detalleDatos.setText("Aquí verás sus destinatarios y, si eres gerente o administrador, su efectividad.");
            tablaDestinatarios.setPlaceholder(EstadoVacio.crear("fth-users", "Sin campaña elegida.", null, null));
            return;
        }
        detalleTitulo.setText(c.nombre());
        detalleDatos.setText(c.canal().etiqueta() + " · " + c.segmento().etiqueta() + " · "
                + Formatos.moneda(c.presupuesto()) + "\nAsunto: " + c.asunto());
        if (permisos.gestionaEquipo()) {
            TareaEnSegundoPlano.ejecutar(() -> service.metricas(c), this::mostrarMetricas,
                    error -> Notificaciones.advertencia(tabla, "No se pudieron cargar las métricas. " + MensajesError.de(error)),
                    null);
        }
        cargarDestinatarios(0);
    }

    private void mostrarMetricas(MetricasCampana m) {
        metricas.getChildren().setAll(
                Tarjetas.kpi("fth-mail", "Apertura", Formatos.porcentaje(m.tasaApertura()),
                        m.abiertos() + " de " + m.destinatarios() + " destinatarios"),
                Tarjetas.kpi("fth-message-circle", "Respuesta", Formatos.porcentaje(m.tasaRespuesta()),
                        m.respondieron() + " respondieron"),
                Tarjetas.kpi("fth-dollar-sign", "Costo por respuesta",
                        m.costoPorRespuesta() == null ? "—" : Formatos.moneda(m.costoPorRespuesta()),
                        "Presupuesto " + Formatos.moneda(m.presupuesto())));
    }

    private void cargarDestinatarios(int pagina) {
        Campana c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }
        ResultadoCampana resultado = filtroResultado.getValue();
        TareaEnSegundoPlano.ejecutar(() -> service.destinatarios(c, resultado, pagina),
                (Pagina<Destinatario> lista) -> {
                    tablaDestinatarios.getItems().setAll(lista.contenido());
                    paginacionDestinatarios.mostrar(lista);
                    tablaDestinatarios.setPlaceholder(permisos.gestionaEquipo() && !c.estado().estaCerrada()
                            ? EstadoVacio.crear("fth-users", "La campaña aún no tiene destinatarios.",
                                    "Generar desde el segmento", this::onGenerar)
                            : EstadoVacio.crear("fth-users", "Sin destinatarios.", null, null));
                },
                error -> tablaDestinatarios.setPlaceholder(EstadoVacio.crear("fth-alert-triangle",
                        "No se pudieron cargar los destinatarios. " + MensajesError.de(error), null, null)),
                cargandoDetalle);
    }

    @FXML
    private void onNueva() {
        CampanaDialogo.mostrar(ventana(), service, null).ifPresent(c -> {
            Notificaciones.exito(tabla, "Campaña " + c.nombre() + " creada como planeada.");
            cargar(0);
        });
    }

    @FXML
    private void onEditar() {
        Campana c = tabla.getSelectionModel().getSelectedItem();
        if (c != null && !c.estado().estaCerrada() && permisos.gestionaEquipo()) {
            CampanaDialogo.mostrar(ventana(), service, c).ifPresent(editada -> {
                Notificaciones.exito(tabla, "Campaña " + editada.nombre() + " actualizada.");
                cargar(paginacion.actual());
            });
        }
    }

    @FXML
    private void onCambiarEstado() {
        Campana c = tabla.getSelectionModel().getSelectedItem();
        if (c != null) {
            EstadoCampanaDialogo.mostrar(ventana(), service, c).ifPresent(cambiada -> {
                Notificaciones.exito(tabla, cambiada.nombre() + " ahora está " + cambiada.estado().etiqueta().toLowerCase() + ".");
                cargar(paginacion.actual());
            });
        }
    }

    @FXML
    private void onGenerar() {
        Campana c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> service.generarDestinatarios(c),
                g -> {
                    Notificaciones.exito(tabla, g.agregados() == 0
                            ? "No había clientes nuevos en el segmento. Total: " + g.totalDestinatarios() + "."
                            : g.agregados() + " destinatario(s) agregados. Total: " + g.totalDestinatarios() + ".");
                    cargar(paginacion.actual());
                    cargarDestinatarios(0);
                },
                error -> Alertas.error(tabla, MensajesError.de(error)), cargandoDetalle);
    }

    @FXML
    private void onAgregar() {
        Campana c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> clienteService.paraSeleccion(null),
                clientes -> AgregarDestinatarioDialogo.mostrar(ventana(), service, c, clientes).ifPresent(d -> {
                    Notificaciones.exito(tabla, d.clienteNombre() + " agregado a la campaña.");
                    cargar(paginacion.actual());
                    cargarDestinatarios(paginacionDestinatarios.actual());
                }),
                error -> Notificaciones.error(tabla, MensajesError.de(error)), cargandoDetalle);
    }

    @FXML
    private void onQuitar() {
        Campana c = tabla.getSelectionModel().getSelectedItem();
        Destinatario d = tablaDestinatarios.getSelectionModel().getSelectedItem();
        if (c == null || d == null || !Alertas.confirmar(tabla, "Quitar destinatario",
                "¿Quitar a " + d.clienteNombre() + " de la campaña " + c.nombre() + "?", "Quitar")) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> {
                    service.quitarDestinatario(c, d);
                    return Boolean.TRUE;
                },
                ok -> {
                    Notificaciones.exito(tabla, d.clienteNombre() + " ya no es destinatario.");
                    cargar(paginacion.actual());
                    cargarDestinatarios(paginacionDestinatarios.actual());
                },
                error -> Alertas.error(tabla, MensajesError.de(error)), cargandoDetalle);
    }

    @FXML
    private void onMarcarAbierto() {
        avanzar(ResultadoCampana.ABIERTO);
    }

    @FXML
    private void onMarcarRespondio() {
        avanzar(ResultadoCampana.RESPONDIO);
    }

    private void avanzar(ResultadoCampana nuevo) {
        Campana c = tabla.getSelectionModel().getSelectedItem();
        Destinatario d = tablaDestinatarios.getSelectionModel().getSelectedItem();
        if (c == null || d == null) {
            return;
        }
        TareaEnSegundoPlano.ejecutar(() -> service.avanzarResultado(c, d, nuevo),
                actualizado -> {
                    Notificaciones.exito(tabla, actualizado.clienteNombre() + ": " + actualizado.resultado().etiqueta() + ".");
                    mostrarDetalle();
                },
                error -> Alertas.error(tabla, MensajesError.de(error)), cargandoDetalle);
    }

    @FXML
    private void onRecargar() {
        cargar(paginacion.actual());
        mostrarDetalle();
    }

    private Window ventana() {
        return tabla.getScene() == null ? null : tabla.getScene().getWindow();
    }
}
