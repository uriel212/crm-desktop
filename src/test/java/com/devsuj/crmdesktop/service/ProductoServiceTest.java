package com.devsuj.crmdesktop.service;

import com.devsuj.crmdesktop.client.CategoriaApi;
import com.devsuj.crmdesktop.client.ProductoApi;
import com.devsuj.crmdesktop.model.Categoria;
import com.devsuj.crmdesktop.model.CategoriaRequest;
import com.devsuj.crmdesktop.model.MovimientoInventario;
import com.devsuj.crmdesktop.model.MovimientoInventarioRequest;
import com.devsuj.crmdesktop.model.Pagina;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.ProductoRequest;
import com.devsuj.crmdesktop.model.TipoMovimientoInventario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoServiceTest {

    static class ProductoApiFalsa implements ProductoApi {
        final List<Object> enviados = new ArrayList<>();

        @Override
        public Pagina<Producto> buscar(String q, Integer c, Boolean a, Boolean ca, int p, int t) {
            return new Pagina<>(List.of(), p, t, 0, 0);
        }

        @Override
        public List<Producto> stockBajo() {
            return List.of();
        }

        @Override
        public Producto crear(ProductoRequest datos) {
            enviados.add(datos);
            return null;
        }

        @Override
        public Producto actualizar(int id, ProductoRequest datos) {
            enviados.add(datos);
            return null;
        }

        @Override
        public Producto cambiarEstado(int id, boolean activo) {
            return null;
        }

        @Override
        public MovimientoInventario registrarMovimiento(int productoId, MovimientoInventarioRequest datos) {
            enviados.add(datos);
            return null;
        }

        @Override
        public Pagina<MovimientoInventario> movimientos(int productoId, int pagina, int tamano) {
            return new Pagina<>(List.of(), pagina, tamano, 0, 0);
        }
    }

    static class CategoriaApiFalsa implements CategoriaApi {
        @Override
        public List<Categoria> listar() {
            return List.of(new Categoria(2, "Redes", null), new Categoria(1, "Accesorios", null));
        }

        @Override
        public Categoria crear(CategoriaRequest datos) {
            return null;
        }

        @Override
        public Categoria actualizar(int id, CategoriaRequest datos) {
            return null;
        }

        @Override
        public void eliminar(int id) {
        }
    }

    private final ProductoApiFalsa api = new ProductoApiFalsa();
    private final ProductoService service = new ProductoService(api, new CategoriaApiFalsa());
    private final Producto mouse = CarritoVentaTest.producto(9, 3, true);

    @Test
    void lasCategoriasSeOrdenanPorNombre() {
        assertEquals("Accesorios", service.categorias().getFirst().nombre());
    }

    @Test
    void unaSalidaNoPuedeSuperarLasExistencias() {
        ValidacionException error = assertThrows(ValidacionException.class,
                () -> service.registrarMovimiento(mouse, TipoMovimientoInventario.SALIDA, "5", "Merma"));

        assertTrue(error.erroresPorCampo().containsKey(ProductoService.CANTIDAD));
        assertTrue(api.enviados.isEmpty());
    }

    @Test
    void soloSeRegistranEntradasYSalidasManuales() {
        assertThrows(ValidacionException.class,
                () -> service.registrarMovimiento(mouse, TipoMovimientoInventario.VENTA, "1", "x"));
    }

    @Test
    void alEditarNoSeEnviaStockInicial() {
        var datos = new ProductoFormulario(new Categoria(1, "Accesorios", null), "Mouse", "", "199.90", "",
                "2", "50");

        service.guardar(mouse, datos);

        ProductoRequest enviado = (ProductoRequest) api.enviados.getFirst();
        assertNull(enviado.stockInicial());
        assertNull(enviado.precioPuntos());
        assertEquals(new BigDecimal("199.90"), enviado.precio());
    }
}
