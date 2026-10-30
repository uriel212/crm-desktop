package com.devsuj.crmdesktop.view.productos;

import com.devsuj.crmdesktop.model.Categoria;
import com.devsuj.crmdesktop.model.Producto;
import com.devsuj.crmdesktop.service.ProductoFormulario;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

/** Diálogo para crear o editar un producto (GERENTE y ADMIN). El stock no se edita aquí. */
public final class ProductoDialogo {

    private ProductoDialogo() {
    }

    public static Optional<Producto> mostrar(Window ventana, ProductoService service, Producto original,
                                             List<Categoria> categorias) {
        Formulario formulario = new Formulario();
        TextField nombre = formulario.campo("Nombre *", ProductoService.NOMBRE, new TextField());
        ComboBox<Categoria> categoria = formulario.campo("Categoría *", ProductoService.CATEGORIA, new ComboBox<>());
        categoria.getItems().setAll(categorias);
        EtiquetaListCell.aplicar(categoria, "Elige la categoría", Categoria::nombre);
        TextField precio = formulario.campo("Precio (Q) *", ProductoService.PRECIO, new TextField());
        precio.setPromptText("1299.00");
        TextField puntos = formulario.campo("Precio en puntos", ProductoService.PRECIO_PUNTOS, new TextField());
        puntos.setPromptText("Vacío = no canjeable");
        TextField minimo = formulario.campo("Stock mínimo *", ProductoService.STOCK_MINIMO, new TextField());
        minimo.setPromptText("Alerta de resurtir");
        TextField inicial = new TextField();
        if (original == null) {
            formulario.campo("Stock inicial", ProductoService.STOCK_INICIAL, inicial);
            inicial.setPromptText("0");
        }
        TextArea descripcion = formulario.campoAncho("Descripción", ProductoService.DESCRIPCION, new TextArea());
        descripcion.setPrefRowCount(3);
        descripcion.setWrapText(true);
        formulario.regla(nombre, true, () -> ProductoService.reglaNombre(nombre.getText()));
        formulario.regla(categoria, true, () -> categoria.getValue() == null
                ? Optional.of("Elige la categoría.") : Optional.empty());
        formulario.regla(precio, true, () -> ProductoService.reglaPrecio(precio.getText()));
        formulario.regla(puntos, false, () -> ProductoService.reglaPuntos(puntos.getText()));
        formulario.regla(minimo, true, () -> ProductoService.reglaStockMinimo(minimo.getText()));

        if (original != null) {
            nombre.setText(original.nombre());
            categorias.stream().filter(c -> c.id().equals(original.categoriaId())).findFirst().ifPresent(categoria::setValue);
            precio.setText(original.precio() == null ? "" : original.precio().toPlainString());
            puntos.setText(original.precioPuntos() == null ? "" : original.precioPuntos().toString());
            minimo.setText(String.valueOf(original.stockMinimo()));
            descripcion.setText(original.descripcion());
            Label nota = new Label("Existencias actuales: " + original.stock()
                    + ". Para cambiarlas registra una entrada o salida de inventario.");
            nota.getStyleClass().add("text-muted");
            nota.setWrapText(true);
            formulario.nota(nota);
        }

        return DialogoGuardar.mostrar(ventana, original == null ? "Nuevo producto" : "Editar producto",
                original == null ? "Agregar un producto al catálogo" : original.nombre(),
                formulario, "Guardar",
                () -> {
                    var datos = new ProductoFormulario(categoria.getValue(), nombre.getText(), descripcion.getText(),
                            precio.getText(), puntos.getText(), minimo.getText(), inicial.getText());
                    return () -> service.guardar(original, datos);
                });
    }
}
