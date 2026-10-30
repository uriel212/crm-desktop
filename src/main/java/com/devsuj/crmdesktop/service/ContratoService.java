package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.ContratoApi;
import com.devsuj.crmdesktop.model.Contrato;
import com.devsuj.crmdesktop.model.ContratoRequest;
import com.devsuj.crmdesktop.model.EstadoContrato;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.RenovacionContratoRequest;
import com.devsuj.crmdesktop.model.VencimientoLote;
import com.devsuj.crmdesktop.util.Formatos;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contratos: alta, edición (solo BORRADOR), cambio de estado, extensión, renovación y vencimiento
 * en lote. No se borran. Sin JavaFX.
 *
 * <p>Reglas del backend: BORRADOR → VIGENTE | CANCELADO; VIGENTE → VENCIDO | CANCELADO, y VENCIDO solo
 * si la fecha de fin ya pasó. Un VIGENTE se extiende; la renovación crea un contrato nuevo ligado,
 * una vez por contrato, desde VIGENTE o VENCIDO.</p>
 */
public class ContratoService {

    public static final int TAMANO_PAGINA = 20;
    public static final int DIAS_POR_VENCER = 30;
    public static final String CLIENTE = "clienteId";
    public static final String VENTA = "ventaId";
    public static final String TITULO = "titulo";
    public static final String FECHA_INICIO = "fechaInicio";
    public static final String FECHA_FIN = "fechaFin";
    public static final String MONTO = "monto";

    private final ContratoApi api;
    private final Clock reloj;

    public ContratoService(ContratoApi api, Clock reloj) {
        this.api = api;
        this.reloj = reloj;
    }

    /** @param soloPorVencer VIGENTE que vencen en los próximos {@value #DIAS_POR_VENCER} días */
    public Pagina<Contrato> buscar(EstadoContrato estado, boolean soloPorVencer, int pagina) {
        return api.buscar(null, estado, soloPorVencer ? DIAS_POR_VENCER : null, pagina, TAMANO_PAGINA);
    }

    /** Estados a los que puede pasar el contrato hoy (VENCIDO solo si la fecha de fin ya pasó). */
    public List<EstadoContrato> destinosPermitidos(Contrato contrato) {
        LocalDate hoy = LocalDate.now(reloj);
        return Arrays.stream(EstadoContrato.values())
                .filter(contrato.estado()::puedeCambiarA)
                .filter(e -> e != EstadoContrato.VENCIDO || contrato.fechaFin().isBefore(hoy))
                .toList();
    }

    public static boolean sePuedeRenovar(Contrato c) {
        return c != null && (c.estado() == EstadoContrato.VIGENTE || c.estado() == EstadoContrato.VENCIDO);
    }

    public Contrato guardar(Contrato original, ContratoFormulario datos) {
        if (original != null && original.estado() != EstadoContrato.BORRADOR) {
            throw new IllegalStateException("Solo se edita un contrato en borrador. Un vigente se extiende o se renueva.");
        }
        Map<String, String> errores = new LinkedHashMap<>();
        if (original == null) {
            Reglas.revisar(errores, CLIENTE, () -> Reglas.obligatorio(datos.cliente(), "Elige el cliente."));
        }
        Reglas.revisar(errores, VENTA, () -> Reglas.enteroValido(datos.ventaId(), false, 1, ""));
        Reglas.revisar(errores, TITULO, () -> Reglas.obligatorio(datos.titulo(), "Escribe el título del contrato.")
                .or(() -> Reglas.maximo(datos.titulo(), 150)));
        Reglas.rangoFechas(errores, FECHA_INICIO, datos.fechaInicio(), FECHA_FIN, datos.fechaFin());
        Reglas.revisar(errores, MONTO, () -> Reglas.montoValido(datos.monto(), "Escribe el monto del contrato.", 10));
        Reglas.exigir(errores);

        Integer clienteId = original == null ? datos.cliente().id() : original.clienteId();
        ContratoRequest request = new ContratoRequest(clienteId, Reglas.entero(datos.ventaId()).orElse(null),
                datos.titulo().strip(), datos.fechaInicio(), datos.fechaFin(), Reglas.monto(datos.monto()).orElseThrow());
        return original == null ? api.crear(request) : api.actualizar(original.id(), request);
    }

    public Contrato cambiarEstado(Contrato contrato, EstadoContrato destino) {
        if (!destinosPermitidos(contrato).contains(destino)) {
            throw new ValidacionException(Map.of("estado", destino == EstadoContrato.VENCIDO
                    ? "Solo se marca vencido cuando su fecha de fin ya pasó."
                    : "Un contrato " + contrato.estado().etiqueta().toLowerCase() + " no puede pasar a ese estado."));
        }
        return api.cambiarEstado(contrato.id(), destino);
    }

    public Contrato extender(Contrato contrato, LocalDate nuevaFechaFin) {
        if (contrato.estado() != EstadoContrato.VIGENTE) {
            throw new IllegalStateException("Solo se extiende un contrato vigente.");
        }
        if (nuevaFechaFin == null || !nuevaFechaFin.isAfter(contrato.fechaFin())) {
            throw new ValidacionException(Map.of(FECHA_FIN, "Elige una fecha posterior al "
                    + Formatos.fecha(contrato.fechaFin()) + "."));
        }
        return api.extender(contrato.id(), nuevaFechaFin);
    }

    public Contrato renovar(Contrato contrato, String titulo, LocalDate inicio, LocalDate fin, String monto) {
        if (!sePuedeRenovar(contrato)) {
            throw new IllegalStateException("Solo se renueva un contrato vigente o vencido.");
        }
        Map<String, String> errores = new LinkedHashMap<>();
        Reglas.revisar(errores, TITULO, () -> Reglas.maximo(titulo, 150));
        Reglas.rangoFechas(errores, FECHA_INICIO, inicio, FECHA_FIN, fin);
        Reglas.revisar(errores, MONTO, () -> Reglas.montoValido(monto, "Escribe el monto del nuevo contrato.", 10));
        Reglas.exigir(errores);
        return api.renovar(contrato.id(), new RenovacionContratoRequest(Reglas.limpiar(titulo), inicio, fin,
                Reglas.monto(monto).orElseThrow()));
    }

    public VencimientoLote marcarVencidos() {
        return api.marcarVencidos();
    }
}
