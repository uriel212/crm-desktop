package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.AlcancePromocion;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Promocion;
import com.devsuj.crmdesktop.model.PromocionRequest;

import java.util.List;

/** Operaciones de {@code /api/promociones} (crear/editar: GERENTE y ADMIN; borrar: ADMIN). */
public interface PromocionApi {

    /** {@code GET /api/promociones?activa=&alcance=&campanaId=&productoId=&page=&size=} */
    Pagina<Promocion> buscar(Boolean activa, AlcancePromocion alcance, Integer campanaId, Integer productoId,
                             int pagina, int tamano);

    /** {@code GET /api/promociones/vigentes}: las que aplicarían hoy. */
    List<Promocion> vigentes();

    Promocion crear(PromocionRequest datos);

    Promocion actualizar(int id, PromocionRequest datos);

    Promocion cambiarEstado(int id, boolean activa);

    /** Solo si nunca se aplicó en una venta (si no, 409: hay que desactivarla). */
    void eliminar(int id);
}
