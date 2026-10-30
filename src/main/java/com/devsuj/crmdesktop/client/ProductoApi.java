package com.devsuj.crmdesktop.client;

import com.devsuj.crmdesktop.model.MovimientoInventario;
import com.devsuj.crmdesktop.model.MovimientoInventarioRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.ProductoRequest;

import java.util.List;

/** Operaciones de {@code /api/productos} e inventario (modificar: GERENTE y ADMIN). */
public interface ProductoApi {

    /** {@code GET /api/productos?categoriaId=&activo=&canjeables=&q=&page=&size=} */
    Pagina<Producto> buscar(String texto, Integer categoriaId, Boolean activo, Boolean canjeables,
                            int pagina, int tamano);

    /** {@code GET /api/productos/stock-bajo} */
    List<Producto> stockBajo();

    /** {@code POST /api/productos} */
    Producto crear(ProductoRequest datos);

    /** {@code PUT /api/productos/{id}} */
    Producto actualizar(int id, ProductoRequest datos);

    /** {@code PATCH /api/productos/{id}/estado} */
    Producto cambiarEstado(int id, boolean activo);

    /** {@code POST /api/productos/{id}/movimientos}: entrada o salida manual. */
    MovimientoInventario registrarMovimiento(int productoId, MovimientoInventarioRequest datos);

    /** {@code GET /api/productos/{id}/movimientos}: kardex, del más reciente al más antiguo. */
    Pagina<MovimientoInventario> movimientos(int productoId, int pagina, int tamano);
}
