package com.devsuj.crmdesktop.view.oportunidades;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Oportunidad;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.OportunidadFormulario;
import com.devsuj.crmdesktop.service.OportunidadService;
import com.devsuj.crmdesktop.util.Formatos;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import com.devsuj.crmdesktop.view.comun.Selectores;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;
import java.util.Optional;

/**
 * Diálogo para crear, editar o retomar (nuevo intento de una PERDIDA) una oportunidad.
 */
public final class OportunidadDialogo {

    private OportunidadDialogo() {
    }

    /**
     * @param original     oportunidad a editar, o {@code null} para crear
     * @param anterior     oportunidad PERDIDA que se retoma (el cliente queda fijo), o {@code null}
     * @param clientes     clientes que el usuario puede gestionar (solo se usa al crear)
     * @param responsables vendedores y gerentes para elegir el responsable; vacío si el usuario es VENDEDOR
     */
    public static Optional<Oportunidad> mostrar(Window ventana, OportunidadService service, Oportunidad original,
                                                Oportunidad anterior, List<Cliente> clientes,
                                                List<Usuario> responsables) {
        Formulario formulario = new Formulario();
        SearchableComboBox<Cliente> cliente = Selectores.clientes(clientes);
        if (original == null) {
            formulario.campoAncho("Cliente *", OportunidadService.CLIENTE, cliente);
            if (anterior != null) {
                clientes.stream().filter(c -> c.id().equals(anterior.clienteId())).findFirst().ifPresent(cliente::setValue);
                cliente.setDisable(true);
            }
            formulario.regla(cliente, true, () -> cliente.getValue() == null
                    ? Optional.of("Elige el cliente.") : Optional.empty());
        }
        TextField titulo = formulario.campoAncho("Título *", OportunidadService.TITULO, new TextField());
        titulo.setPromptText("Por ejemplo: Renovación de 10 laptops");
        TextField monto = formulario.campo("Monto estimado (Q) *", OportunidadService.MONTO, new TextField());
        monto.setPromptText("25000.00");
        DatePicker cierre = formulario.campo("Cierre estimado", OportunidadService.FECHA_CIERRE, new DatePicker());
        ComboBox<Usuario> responsable = new ComboBox<>();
        if (original == null && !responsables.isEmpty()) {
            formulario.campo("Responsable", "usuarioId", responsable);
            responsable.getItems().add(null);
            responsable.getItems().addAll(responsables);
            EtiquetaListCell.aplicar(responsable, "Yo (quien la registra)", Usuario::nombreConRol);
        }
        formulario.regla(titulo, true, () -> OportunidadService.reglaTitulo(titulo.getText()));
        formulario.regla(monto, true, () -> OportunidadService.reglaMonto(monto.getText()));

        if (original != null) {
            titulo.setText(original.titulo());
            monto.setText(original.montoEstimado() == null ? "" : original.montoEstimado().toPlainString());
            cierre.setValue(original.fechaCierreEstimada());
        } else if (anterior != null) {
            titulo.setText(anterior.titulo());
            monto.setText(anterior.montoEstimado() == null ? "" : anterior.montoEstimado().toPlainString());
            Label nota = new Label("Nuevo intento de la oportunidad perdida \"" + anterior.titulo()
                    + "\". Quedarán ligadas en el historial.");
            nota.getStyleClass().add("text-muted");
            nota.setWrapText(true);
            formulario.nota(nota);
        } else {
            cierre.setValue(Formatos.hoy().plusMonths(1));
        }

        String encabezado = original != null ? original.titulo()
                : anterior != null ? "Retomar con " + anterior.clienteNombre()
                : "Registrar una oportunidad de venta";
        return DialogoGuardar.mostrar(ventana,
                original == null ? "Nueva oportunidad" : "Editar oportunidad", encabezado,
                formulario, "Guardar",
                () -> {
                    var datos = new OportunidadFormulario(cliente.getValue(), titulo.getText(), monto.getText(),
                            cierre.getValue(), responsable.getValue());
                    return () -> service.guardar(original, anterior, datos);
                });
    }
}
