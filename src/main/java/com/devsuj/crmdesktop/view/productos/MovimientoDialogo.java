package com.devsuj.crmdesktop.view.productos;

import com.devsuj.crmdesktop.model.MovimientoInventario;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.model.TipoMovimientoInventario;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.service.Reglas;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.Optional;

/** Ajuste manual de inventario: ENTRADA (llegó mercancía) o SALIDA (merma, conteo físico). */
public final class MovimientoDialogo {

    private MovimientoDialogo() {
    }

    public static Optional<MovimientoInventario> mostrar(Window ventana, ProductoService service, Producto producto) {
        Formulario formulario = new Formulario();
        ComboBox<TipoMovimientoInventario> tipo = formulario.campo("Tipo *", ProductoService.TIPO, new ComboBox<>());
        tipo.getItems().setAll(TipoMovimientoInventario.ENTRADA, TipoMovimientoInventario.SALIDA);
        EtiquetaListCell.aplicar(tipo, "Elige el tipo",
                t -> t == TipoMovimientoInventario.ENTRADA ? "Entrada (suma)" : "Salida (resta)");
        tipo.setValue(TipoMovimientoInventario.ENTRADA);
        TextField cantidad = formulario.campo("Cantidad *", ProductoService.CANTIDAD, new TextField());
        TextField motivo = formulario.campoAncho("Motivo *", ProductoService.MOTIVO, new TextField());
        motivo.setPromptText("Por ejemplo: Factura de proveedor F-1234");
        formulario.regla(cantidad, true, () -> Reglas.enteroValido(cantidad.getText(), true, 1, "Escribe la cantidad."));
        formulario.regla(motivo, true, () -> Reglas.obligatorio(motivo.getText(), "Escribe el motivo."));

        return DialogoGuardar.mostrar(ventana, "Movimiento de inventario",
                producto.nombre() + " · existencias: " + producto.stock(),
                formulario, "Registrar",
                () -> {
                    TipoMovimientoInventario elegido = tipo.getValue();
                    String texto = cantidad.getText();
                    String razon = motivo.getText();
                    return () -> service.registrarMovimiento(producto, elegido, texto, razon);
                });
    }
}
