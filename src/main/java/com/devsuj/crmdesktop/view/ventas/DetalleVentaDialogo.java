package com.devsuj.crmdesktop.view.ventas;

import com.devsuj.crmdesktop.model.DetalleVenta;
import com.devsuj.crmdesktop.model.Venta;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.util.Recursos;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

/** Detalle de una venta (solo lectura: una venta no se edita ni se cancela). */
public final class DetalleVentaDialogo {

    private DetalleVentaDialogo() {
    }

    public static void mostrar(Window ventana, Venta venta) {
        GridPane datos = new GridPane();
        datos.setHgap(16);
        datos.setVgap(4);
        datos.addRow(0, etiqueta("Fecha"), new Label(Formatos.fechaHora(venta.fecha())));
        datos.addRow(1, etiqueta("Cliente"), new Label(venta.clienteNombre()));
        datos.addRow(2, etiqueta("Vendedor"), new Label(Formatos.textoOGuion(venta.usuarioNombre())));
        datos.addRow(3, etiqueta("Oportunidad"), new Label(venta.oportunidadId() == null ? "—" : "#" + venta.oportunidadId()));

        TableView<DetalleVenta> tabla = new TableView<>();
        Tablas.ajustable(tabla);
        TableColumn<DetalleVenta, String> producto = new TableColumn<>("Producto");
        TableColumn<DetalleVenta, String> cantidad = new TableColumn<>("Cant.");
        TableColumn<DetalleVenta, String> precio = new TableColumn<>("Precio unit.");
        TableColumn<DetalleVenta, String> descuento = new TableColumn<>("Descuento");
        TableColumn<DetalleVenta, String> promocion = new TableColumn<>("Promoción");
        TableColumn<DetalleVenta, String> subtotal = new TableColumn<>("Subtotal");
        Tablas.texto(producto, DetalleVenta::productoNombre);
        Tablas.numero(cantidad, DetalleVenta::cantidad);
        Tablas.moneda(precio, DetalleVenta::precioUnitario);
        Tablas.moneda(descuento, DetalleVenta::descuento);
        Tablas.texto(promocion, DetalleVenta::promocionNombre);
        Tablas.moneda(subtotal, DetalleVenta::subtotal);
        producto.setMinWidth(180);
        tabla.getColumns().setAll(List.of(producto, cantidad, precio, descuento, promocion, subtotal));
        tabla.getItems().setAll(venta.detalles());
        tabla.setPrefHeight(220);

        GridPane totales = new GridPane();
        totales.setHgap(16);
        totales.setVgap(4);
        totales.addRow(0, etiqueta("Subtotal"), new Label(Formatos.moneda(venta.subtotal())));
        totales.addRow(1, etiqueta("Descuento"), new Label(venta.descuento() == null || venta.descuento().signum() == 0
                ? "—" : "−" + Formatos.moneda(venta.descuento())
                + (venta.promocionNombre() == null ? "" : " (" + venta.promocionNombre() + ")")));
        Label total = new Label(Formatos.moneda(venta.total()));
        total.getStyleClass().addAll("title-3", "total-venta");
        totales.addRow(2, etiqueta("Total"), total);
        totales.addRow(3, etiqueta("Puntos ganados"), new Label(Formatos.entero(venta.puntosGanados())));

        VBox contenido = new VBox(12, datos, tabla, totales);
        contenido.setPrefWidth(760);
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.initOwner(ventana);
        dialogo.setTitle("Venta #" + venta.id());
        dialogo.setHeaderText("Venta #" + venta.id() + " · " + Formatos.moneda(venta.total()));
        dialogo.setResizable(true);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getStylesheets().add(Recursos.url("css/styles.css").toExternalForm());
        dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
        dialogo.showAndWait();
    }

    private static Label etiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("text-muted");
        return etiqueta;
    }
}
