package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.CanalCampana;
import com.devsuj.crmdesktop.model.Campana;
import com.devsuj.crmdesktop.model.CampanaRequest;
import com.devsuj.crmdesktop.model.Destinatario;
import com.devsuj.crmdesktop.model.EstadoCampana;
import com.devsuj.crmdesktop.model.GeneracionDestinatarios;
import com.devsuj.crmdesktop.model.MetricasCampana;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.ResultadoCampana;

/** Operaciones de {@code /api/campanas} (modificar: GERENTE y ADMIN). No se envían mensajes reales. */
public interface CampanaApi {

    /** {@code GET /api/campanas?estado=&canal=&q=&page=&size=} */
    Pagina<Campana> buscar(EstadoCampana estado, CanalCampana canal, String texto, int pagina, int tamano);

    Campana crear(CampanaRequest datos);

    Campana actualizar(int id, CampanaRequest datos);

    Campana cambiarEstado(int id, EstadoCampana estado);

    /** {@code GET /api/campanas/{id}/destinatarios?resultado=&page=&size=} */
    Pagina<Destinatario> destinatarios(int campanaId, ResultadoCampana resultado, int pagina, int tamano);

    /** Agrega los clientes del segmento que faltan (idempotente). */
    GeneracionDestinatarios generarDestinatarios(int campanaId);

    Destinatario agregarDestinatario(int campanaId, int clienteId);

    /** Solo en campañas PLANEADA (204). */
    void quitarDestinatario(int campanaId, int clienteId);

    /** Solo avanza ENVIADO → ABIERTO → RESPONDIO; la campaña debe estar ACTIVA o FINALIZADA. */
    Destinatario cambiarResultado(int campanaId, int clienteId, ResultadoCampana resultado);

    /** Solo GERENTE y ADMIN. */
    MetricasCampana metricas(int campanaId);
}
