package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.ApiException;
import com.devsuj.crmdesktop.client.ClienteApiEnMemoria;
import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoCliente;
import com.devsuj.crmdesktop.model.Pagina;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba la lógica del service con la API en memoria: sin red y sin JavaFX. */
class ClienteServiceTest {

    private ClienteApiEnMemoria api;
    private ClienteService service;

    @BeforeEach
    void preparar() {
        api = ClienteApiEnMemoria.conDatosDeEjemplo();
        service = new ClienteService(api);
    }

    @Test
    void buscaPorTextoYEstado() {
        Pagina<Cliente> prospectos = service.buscar(null, EstadoCliente.PROSPECTO, 0);
        assertEquals(4, prospectos.totalElementos());

        Pagina<Cliente> porEmpresa = service.buscar("  costa azul ", null, 0);
        assertEquals("Gabriela Soto", porEmpresa.contenido().getFirst().nombre());
    }

    @Test
    void creaUnClienteLimpiandoLosEspaciosYConvirtiendoVaciosEnNull() {
        var datos = new ClienteFormulario("  Ana Ruiz ", "  ", "ana@ruiz.test", "", null, EstadoCliente.PROSPECTO);

        Cliente creado = service.guardar(null, datos);

        assertEquals("Ana Ruiz", creado.nombre());
        assertNull(creado.empresa());
        assertNull(creado.telefono());
        assertEquals(EstadoCliente.PROSPECTO, creado.estado());
    }

    @Test
    void siSeEligeOtroEstadoAlCrearSeCambiaConElEndpointDeEstado() {
        var datos = new ClienteFormulario("Ana", null, "ana@ruiz.test", null, null, EstadoCliente.ACTIVO);

        assertEquals(EstadoCliente.ACTIVO, service.guardar(null, datos).estado());
    }

    @Test
    void alEditarConservaElVendedorAsignado() {
        Cliente roberto = service.buscar("rgarza", null, 0).contenido().getFirst();
        var datos = new ClienteFormulario("Roberto Garza Jr.", roberto.empresa(), roberto.email(),
                roberto.telefono(), roberto.direccion(), roberto.estado());

        Cliente editado = service.guardar(roberto, datos);

        assertEquals("Roberto Garza Jr.", editado.nombre());
        assertEquals(roberto.vendedorId(), editado.vendedorId());
    }

    @Test
    void asignarUnoUsaElPatchYVariosLaReasignacionEnLote() {
        var jorge = new com.devsuj.crmdesktop.model.Usuario(4, "Jorge Pérez", "jorge@test.com",
                com.devsuj.crmdesktop.model.Rol.VENDEDOR, 2, null, true, null);
        var clientes = service.buscar(null, EstadoCliente.PROSPECTO, 0).contenido();

        assertEquals(1, service.asignarVendedor(clientes.subList(0, 1), jorge));
        assertEquals(clientes.size(), service.asignarVendedor(clientes, jorge));
        assertEquals(clientes.size(), service.buscar(null, EstadoCliente.PROSPECTO, 4, 0).totalElementos());
    }

    @Test
    void noSeQuitaElResponsableDeVariosALaVez() {
        var clientes = service.buscar(null, EstadoCliente.PROSPECTO, 0).contenido();

        assertThrows(IllegalStateException.class, () -> service.asignarVendedor(clientes, null));
    }

    @Test
    void laVista360TraeElCliente() {
        Cliente roberto = service.buscar("rgarza", null, 0).contenido().getFirst();

        var resumen = service.resumen(roberto);

        assertEquals(roberto, resumen.cliente());
        assertTrue(resumen.ultimasVentas().isEmpty());
    }

    @Test
    void noLlamaALaApiSiElFormularioEsInvalido() {
        int antes = api.todos().size();
        var datos = new ClienteFormulario("", null, "no-es-email", null, null, null);

        ValidacionException error = assertThrows(ValidacionException.class, () -> service.guardar(null, datos));

        assertTrue(error.erroresPorCampo().containsKey(ValidadorCliente.NOMBRE));
        assertEquals(antes, api.todos().size());
    }

    @Test
    void propagaElConflictoDeEmailDeLaApi() {
        var datos = new ClienteFormulario("Otro", null, "rgarza@gin.test", null, null, null);

        ApiException error = assertThrows(ApiException.class, () -> service.guardar(null, datos));

        assertEquals(409, error.status());
        assertTrue(error.erroresPorCampo().containsKey("email"));
    }

    @Test
    void darDeBajaPasaElClienteAInactivo() {
        Cliente activo = service.buscar(null, EstadoCliente.ACTIVO, 0).contenido().getFirst();

        assertEquals(EstadoCliente.INACTIVO, service.darDeBaja(activo).estado());
    }

    @Test
    void noSePuedeDarDeBajaDosVeces() {
        Cliente inactivo = service.buscar(null, EstadoCliente.INACTIVO, 0).contenido().getFirst();

        assertThrows(IllegalStateException.class, () -> service.darDeBaja(inactivo));
    }
}
