package com.devsuj.crmdesktop.config;

import com.devsuj.crmdesktop.client.ApiHttp;
import com.devsuj.crmdesktop.client.AuthApi;
import com.devsuj.crmdesktop.client.AuthApiEnMemoria;
import com.devsuj.crmdesktop.client.AuthApiHttp;
import com.devsuj.crmdesktop.client.CampanaApiHttp;
import com.devsuj.crmdesktop.client.CategoriaApiHttp;
import com.devsuj.crmdesktop.client.ContratoApiHttp;
import com.devsuj.crmdesktop.client.PromocionApiHttp;
import com.devsuj.crmdesktop.client.FidelizacionApiHttp;
import com.devsuj.crmdesktop.client.InteraccionApiHttp;
import com.devsuj.crmdesktop.client.MetaApiHttp;
import com.devsuj.crmdesktop.client.OportunidadApiHttp;
import com.devsuj.crmdesktop.client.ProductoApiHttp;
import com.devsuj.crmdesktop.client.ReporteApiHttp;
import com.devsuj.crmdesktop.client.TicketApiHttp;
import com.devsuj.crmdesktop.client.VentaApiHttp;
import com.devsuj.crmdesktop.client.ClienteApi;
import com.devsuj.crmdesktop.client.ClienteApiEnMemoria;
import com.devsuj.crmdesktop.client.ClienteApiHttp;
import com.devsuj.crmdesktop.client.Sesion;
import com.devsuj.crmdesktop.client.UsuarioApi;
import com.devsuj.crmdesktop.client.UsuarioApiEnMemoria;
import com.devsuj.crmdesktop.client.UsuarioApiHttp;
import com.devsuj.crmdesktop.service.AuthService;
import com.devsuj.crmdesktop.service.CampanaService;
import com.devsuj.crmdesktop.service.PromocionService;
import com.devsuj.crmdesktop.service.ClienteService;
import com.devsuj.crmdesktop.service.ContratoService;
import com.devsuj.crmdesktop.service.FidelizacionService;
import com.devsuj.crmdesktop.service.InteraccionService;
import com.devsuj.crmdesktop.service.MetaService;
import com.devsuj.crmdesktop.service.OportunidadService;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.service.ReporteService;
import com.devsuj.crmdesktop.service.TicketService;
import com.devsuj.crmdesktop.service.UsuarioService;
import com.devsuj.crmdesktop.service.VentaService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.Tema;
import com.devsuj.crmdesktop.view.MainController;
import com.devsuj.crmdesktop.view.VentanaPrincipal;
import com.devsuj.crmdesktop.view.campanas.CampanasController;
import com.devsuj.crmdesktop.view.clientes.ClientesController;
import com.devsuj.crmdesktop.view.contratos.ContratosController;
import com.devsuj.crmdesktop.view.fidelizacion.FidelizacionController;
import com.devsuj.crmdesktop.view.interacciones.InteraccionesController;
import com.devsuj.crmdesktop.view.login.LoginController;
import com.devsuj.crmdesktop.view.metas.MetasController;
import com.devsuj.crmdesktop.view.oportunidades.OportunidadesController;
import com.devsuj.crmdesktop.view.productos.ProductosController;
import com.devsuj.crmdesktop.view.promociones.PromocionesController;
import com.devsuj.crmdesktop.view.soporte.SoporteController;
import com.devsuj.crmdesktop.view.tablero.TableroController;
import com.devsuj.crmdesktop.view.usuarios.UsuariosController;
import com.devsuj.crmdesktop.view.ventas.VentasController;

import java.time.Clock;

/**
 * "Raíz de composición": el ÚNICO lugar donde se crean (con {@code new}) los objetos compartidos
 * de la aplicación y donde se conectan entre sí.
 *
 * <p>Las pantallas no crean sus servicios; los reciben por constructor desde aquí mediante
 * {@link #crearControlador(Class)}, que se usa como {@code controllerFactory} del FXMLLoader
 * (principio de inversión de dependencias, la D de SOLID).</p>
 */
public final class Dependencias {

    private final AppConfig config;
    private final Tema tema = new Tema();
    private final Sesion sesion = new Sesion();
    private final Permisos permisos = new Permisos(sesion);
    private final AuthService authService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final OportunidadService oportunidadService;
    private final InteraccionService interaccionService;
    private final ProductoService productoService;
    private final VentaService ventaService;
    private final MetaService metaService;
    private final ReporteService reporteService;
    private final FidelizacionService fidelizacionService;
    private final CampanaService campanaService;
    private final PromocionService promocionService;
    private final ContratoService contratoService;
    private final TicketService ticketService;
    private final VentanaPrincipal ventanaPrincipal;

    public Dependencias(AppConfig config) {
        this.config = config;
        // Un solo HttpClient, un solo ObjectMapper y una sola sesión (token) para todos los clientes de la API.
        ApiHttp apiHttp = new ApiHttp(HttpConfig.crearHttpClient(), JsonConfig.crearObjectMapper(),
                config.apiBaseUrl(), sesion);
        AuthApi authApi;
        ClienteApi clienteApi;
        UsuarioApi usuarioApi;
        if (config.usarDatosDePrueba()) {
            // Modo demostración: solo login, clientes y usuarios tienen datos en memoria
            // (MainController oculta los demás módulos en este modo).
            authApi = new AuthApiEnMemoria();
            clienteApi = ClienteApiEnMemoria.conDatosDeEjemplo();
            usuarioApi = UsuarioApiEnMemoria.conDatosDeEjemplo();
        } else {
            authApi = new AuthApiHttp(apiHttp);
            clienteApi = new ClienteApiHttp(apiHttp);
            usuarioApi = new UsuarioApiHttp(apiHttp);
        }
        this.authService = new AuthService(authApi, sesion);
        this.clienteService = new ClienteService(clienteApi);
        this.usuarioService = new UsuarioService(usuarioApi);
        this.oportunidadService = new OportunidadService(new OportunidadApiHttp(apiHttp));
        this.interaccionService = new InteraccionService(new InteraccionApiHttp(apiHttp), Clock.system(Formatos.ZONA));
        this.productoService = new ProductoService(new ProductoApiHttp(apiHttp), new CategoriaApiHttp(apiHttp));
        this.ventaService = new VentaService(new VentaApiHttp(apiHttp));
        this.metaService = new MetaService(new MetaApiHttp(apiHttp));
        this.reporteService = new ReporteService(new ReporteApiHttp(apiHttp));
        this.fidelizacionService = new FidelizacionService(new FidelizacionApiHttp(apiHttp));
        this.campanaService = new CampanaService(new CampanaApiHttp(apiHttp));
        this.promocionService = new PromocionService(new PromocionApiHttp(apiHttp));
        this.contratoService = new ContratoService(new ContratoApiHttp(apiHttp), Clock.system(Formatos.ZONA));
        this.ticketService = new TicketService(new TicketApiHttp(apiHttp));
        this.ventanaPrincipal = new VentanaPrincipal(sesion, this::crearControlador);
    }

    public Tema tema() {
        return tema;
    }

    public VentanaPrincipal ventanaPrincipal() {
        return ventanaPrincipal;
    }

    /** Crea el controlador que pide un archivo FXML, pasándole sus dependencias. */
    public Object crearControlador(Class<?> tipo) {
        if (tipo == LoginController.class) {
            return new LoginController(authService, config, ventanaPrincipal::mostrarAplicacion);
        }
        if (tipo == MainController.class) {
            return new MainController(config, tema, permisos, authService, usuarioService, this::crearControlador);
        }
        if (tipo == ClientesController.class) {
            return new ClientesController(clienteService, usuarioService, permisos);
        }
        if (tipo == UsuariosController.class) {
            return new UsuariosController(usuarioService, permisos);
        }
        if (tipo == OportunidadesController.class) {
            return new OportunidadesController(oportunidadService, clienteService, usuarioService, permisos);
        }
        if (tipo == InteraccionesController.class) {
            return new InteraccionesController(interaccionService, clienteService, permisos);
        }
        if (tipo == ProductosController.class) {
            return new ProductosController(productoService, permisos);
        }
        if (tipo == VentasController.class) {
            return new VentasController(ventaService, clienteService, productoService, oportunidadService,
                    usuarioService, permisos);
        }
        if (tipo == MetasController.class) {
            return new MetasController(metaService, reporteService, usuarioService, permisos);
        }
        if (tipo == TableroController.class) {
            return new TableroController(reporteService, permisos);
        }
        if (tipo == FidelizacionController.class) {
            return new FidelizacionController(fidelizacionService, clienteService, productoService, permisos);
        }
        if (tipo == CampanasController.class) {
            return new CampanasController(campanaService, clienteService, permisos);
        }
        if (tipo == PromocionesController.class) {
            return new PromocionesController(promocionService, campanaService, productoService, permisos);
        }
        if (tipo == ContratosController.class) {
            return new ContratosController(contratoService, clienteService, permisos);
        }
        if (tipo == SoporteController.class) {
            return new SoporteController(ticketService, clienteService, usuarioService, permisos);
        }
        throw new IllegalArgumentException("Controlador no registrado en Dependencias: " + tipo.getName());
    }
}
