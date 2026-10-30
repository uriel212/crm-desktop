package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.EtapaOportunidad;
import com.devsuj.crmdesktop.model.HistorialEtapa;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.model.OportunidadActualizarRequest;
import com.devsuj.crmdesktop.model.OportunidadRequest;
import com.devsuj.crmdesktop.model.Pagina;

import java.util.List;

/** Operaciones de {@code /api/oportunidades}. */
public interface OportunidadApi {

    /**
     * {@code GET /api/oportunidades?etapa=&usuarioId=&clienteId=&abiertas=&page=&size=}
     *
     * @param abiertas {@code true} = solo abiertas, {@code false} = solo cerradas, {@code null} = todas
     */
    Pagina<Oportunidad> buscar(EtapaOportunidad etapa, Integer usuarioId, Integer clienteId, Boolean abiertas,
                               int pagina, int tamano);

    /** {@code GET /api/oportunidades/{id}/historial}: línea de tiempo del proceso. */
    List<HistorialEtapa> historial(int id);

    /** {@code POST /api/oportunidades} */
    Oportunidad crear(OportunidadRequest datos);

    /** {@code PUT /api/oportunidades/{id}} */
    Oportunidad actualizar(int id, OportunidadActualizarRequest datos);

    /** {@code PATCH /api/oportunidades/{id}/etapa} */
    Oportunidad cambiarEtapa(int id, EtapaOportunidad etapa, String comentario);
}
