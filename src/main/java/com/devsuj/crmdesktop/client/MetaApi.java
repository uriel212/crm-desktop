package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.MetaVenta;
import com.devsuj.crmdesktop.model.MetaVentaRequest;
import com.devsuj.crmdesktop.model.Pagina;

/** Operaciones de {@code /api/metas} (modificar: GERENTE de su equipo y ADMIN). */
public interface MetaApi {

    /** {@code GET /api/metas?usuarioId=&anio=&mes=&page=&size=} */
    Pagina<MetaVenta> buscar(Integer usuarioId, Integer anio, Integer mes, int pagina, int tamano);

    MetaVenta crear(MetaVentaRequest datos);

    MetaVenta actualizar(int id, MetaVentaRequest datos);

    void eliminar(int id);
}
