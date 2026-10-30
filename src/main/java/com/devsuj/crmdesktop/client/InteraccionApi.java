package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.EstadoInteraccion;
import com.devsuj.crmdesktop.model.Interaccion;
import com.devsuj.crmdesktop.model.InteraccionActualizarRequest;
import com.devsuj.crmdesktop.model.InteraccionRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.TipoInteraccion;

import java.time.LocalDate;
import java.util.List;

/** Operaciones de interacciones y actividades (no se borran: se cancelan). */
public interface InteraccionApi {

    /** {@code GET /api/interacciones?usuarioId=&clienteId=&tipo=&estado=&desde=&hasta=&page=&size=} */
    Pagina<Interaccion> buscar(Integer usuarioId, Integer clienteId, TipoInteraccion tipo, EstadoInteraccion estado,
                               LocalDate desde, LocalDate hasta, int pagina, int tamano);

    /** {@code GET /api/interacciones/proximas?usuarioId=&dias=}: actividades PROGRAMADAS de los próximos días. */
    List<Interaccion> proximas(Integer usuarioId, int dias);

    /** {@code POST /api/clientes/{clienteId}/interacciones} */
    Interaccion crear(int clienteId, InteraccionRequest datos);

    /** {@code PUT /api/interacciones/{id}} */
    Interaccion actualizar(int id, InteraccionActualizarRequest datos);

    /** {@code PATCH /api/interacciones/{id}/estado} */
    Interaccion cambiarEstado(int id, EstadoInteraccion estado, String resultado);
}
