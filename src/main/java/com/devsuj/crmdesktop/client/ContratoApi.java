package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.Contrato;
import com.devsuj.crmdesktop.model.ContratoRequest;
import com.devsuj.crmdesktop.model.EstadoContrato;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.RenovacionContratoRequest;
import com.devsuj.crmdesktop.model.VencimientoLote;

import java.time.LocalDate;

/** Operaciones de {@code /api/contratos}. Los contratos no se borran. */
public interface ContratoApi {

    /**
     * {@code GET /api/contratos?clienteId=&estado=&porVencerDias=&page=&size=}
     *
     * @param porVencerDias VIGENTE que vencen entre hoy y hoy + N; {@code null} = sin ese filtro
     */
    Pagina<Contrato> buscar(Integer clienteId, EstadoContrato estado, Integer porVencerDias, int pagina, int tamano);

    Contrato crear(ContratoRequest datos);

    Contrato actualizar(int id, ContratoRequest datos);

    Contrato cambiarEstado(int id, EstadoContrato estado);

    Contrato extender(int id, LocalDate nuevaFechaFin);

    /** Crea un contrato nuevo en BORRADOR ligado al anterior (201). */
    Contrato renovar(int id, RenovacionContratoRequest datos);

    /** Marca VENCIDO los VIGENTE con fecha de fin pasada (GERENTE y ADMIN). */
    VencimientoLote marcarVencidos();
}
