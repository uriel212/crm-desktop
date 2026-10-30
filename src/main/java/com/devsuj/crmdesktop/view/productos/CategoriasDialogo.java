package com.devsuj.crmdesktop.view.productos;

import com.devsuj.crmdesktop.model.Categoria;
import com.devsuj.crmdesktop.service.Permisos;
import com.devsuj.crmdesktop.service.ProductoService;
import com.devsuj.crmdesktop.util.Alertas;
import com.devsuj.crmdesktop.util.EstadoVacio;
import com.devsuj.crmdesktop.util.MensajesError;
import com.devsuj.crmdesktop.util.Notificaciones;
import com.devsuj.crmdesktop.util.Recursos;
import com.devsuj.crmdesktop.util.TareaEnSegundoPlano;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.Formulario;
import com.devsuj.crmdesktop.view.comun.Tablas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

/** Administración de categorías: alta y edición (GERENTE y ADMIN) y eliminación (solo ADMIN). */
public final class CategoriasDialogo {

    private CategoriasDialogo() {
    }

    /** @return {@code true} si hubo cambios (para recargar los filtros de la pantalla) */
    public static boolean mostrar(Window ventana, ProductoService service, Permisos permisos) {
        boolean[] huboCambios = {false};
        TableView<Categoria> tabla = new TableView<>();
        Tablas.ajustable(tabla);
        TableColumn<Categoria, String> nombre = new TableColumn<>("Nombre");
        TableColumn<Categoria, String> descripcion = new TableColumn<>("Descripción");
        Tablas.texto(nombre, Categoria::nombre);
        Tablas.texto(descripcion, Categoria::descripcion);
        tabla.getColumns().setAll(List.of(nombre, descripcion));
        VBox.setVgrow(tabla, Priority.ALWAYS);

        Runnable recargar = () -> TareaEnSegundoPlano.ejecutar(service::categorias,
                lista -> tabla.getItems().setAll(lista),
                error -> tabla.setPlaceholder(EstadoVacio.crear("fth-alert-triangle", MensajesError.de(error), null, null)),
                null);

        Button nueva = new Button("Nueva", new FontIcon("fth-plus"));
        nueva.getStyleClass().add("accent");
        Button editar = new Button("Editar", new FontIcon("fth-edit-2"));
        Button eliminar = new Button("Eliminar", new FontIcon("fth-trash-2"));
        eliminar.getStyleClass().add("danger");
        editar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        eliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        eliminar.setVisible(permisos.puedeEliminarCategorias());
        eliminar.setManaged(permisos.puedeEliminarCategorias());

        nueva.setOnAction(e -> {
            if (editarCategoria(tabla.getScene().getWindow(), service, null)) {
                huboCambios[0] = true;
                recargar.run();
            }
        });
        editar.setOnAction(e -> {
            Categoria elegida = tabla.getSelectionModel().getSelectedItem();
            if (elegida != null && editarCategoria(tabla.getScene().getWindow(), service, elegida)) {
                huboCambios[0] = true;
                recargar.run();
            }
        });
        Tablas.alDobleClic(tabla, editar::fire);
        eliminar.setOnAction(e -> {
            Categoria elegida = tabla.getSelectionModel().getSelectedItem();
            if (elegida == null || !Alertas.confirmar(tabla, "Eliminar categoría",
                    "¿Eliminar la categoría " + elegida.nombre() + "?\n\nSolo se puede si no tiene productos.",
                    "Eliminar")) {
                return;
            }
            TareaEnSegundoPlano.ejecutar(() -> {
                        service.eliminarCategoria(elegida);
                        return Boolean.TRUE;
                    },
                    ok -> {
                        huboCambios[0] = true;
                        Notificaciones.exito(tabla, "Categoría eliminada.");
                        recargar.run();
                    },
                    error -> Alertas.error(tabla, MensajesError.de(error)),
                    null);
        });

        VBox contenido = new VBox(12, new HBox(8, nueva, editar, eliminar), tabla);
        contenido.setPrefSize(560, 400);
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.initOwner(ventana);
        dialogo.setTitle("Categorías");
        dialogo.setHeaderText("Categorías del catálogo");
        dialogo.setResizable(true);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getStylesheets().add(Recursos.url("css/styles.css").toExternalForm());
        dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
        recargar.run();
        dialogo.showAndWait();
        return huboCambios[0];
    }

    private static boolean editarCategoria(Window ventana, ProductoService service, Categoria original) {
        Formulario formulario = new Formulario();
        TextField nombre = formulario.campoAncho("Nombre *", ProductoService.NOMBRE, new TextField());
        TextField descripcion = formulario.campoAncho("Descripción", ProductoService.DESCRIPCION, new TextField());
        if (original != null) {
            nombre.setText(original.nombre());
            descripcion.setText(original.descripcion());
        }
        return DialogoGuardar.mostrar(ventana, original == null ? "Nueva categoría" : "Editar categoría",
                original == null ? "Agregar una categoría" : original.nombre(), formulario, "Guardar",
                () -> {
                    String n = nombre.getText();
                    String d = descripcion.getText();
                    return () -> service.guardarCategoria(original, n, d);
                }).isPresent();
    }
}
