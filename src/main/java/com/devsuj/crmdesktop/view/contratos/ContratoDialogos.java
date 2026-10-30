package com.devsuj.crmdesktop.view.contratos;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.Contrato;
import com.devsuj.crmdesktop.model.EstadoContrato;
import com.devsuj.crmdesktop.service.ContratoFormulario;
import com.devsuj.crmdesktop.service.ContratoService;
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

/** Diálogos del módulo de contratos: alta/edición, cambio de estado, extensión y renovación. */
public final class ContratoDialogos {

    private ContratoDialogos() {
    }

    /**
     * @param original contrato a editar (solo BORRADOR), o {@code null} para crear
     * @param clientes clientes que el usuario puede gestionar (solo al crear)
     */
    public static Optional<Contrato> alta(Window ventana, ContratoService service, Contrato original,
                                          List<Cliente> clientes) {
        Formulario formulario = new Formulario();
        SearchableComboBox<Cliente> cliente = Selectores.clientes(clientes);
        if (original == null) {
            formulario.campoAncho("Cliente *", ContratoService.CLIENTE, cliente);
            formulario.regla(cliente, true, () -> cliente.getValue() == null
                    ? Optional.of("Elige el cliente.") : Optional.empty());
        }
        TextField titulo = formulario.campoAncho("Título *", ContratoService.TITULO, new TextField());
        titulo.setPromptText("Por ejemplo: Soporte anual de equipos");
        DatePicker inicio = formulario.campo("Inicio *", ContratoService.FECHA_INICIO, new DatePicker());
        DatePicker fin = formulario.campo("Fin *", ContratoService.FECHA_FIN, new DatePicker());
        TextField monto = formulario.campo("Monto (Q) *", ContratoService.MONTO, new TextField());
        TextField venta = formulario.campo("Venta relacionada (n.º)", ContratoService.VENTA, new TextField());
        venta.setPromptText("Opcional");
        formulario.regla(titulo, true, () -> titulo.getText().isBlank()
                ? Optional.of("Escribe el título del contrato.") : Optional.empty());

        if (original != null) {
            titulo.setText(original.titulo());
            inicio.setValue(original.fechaInicio());
            fin.setValue(original.fechaFin());
            monto.setText(original.monto() == null ? "" : original.monto().toPlainString());
            venta.setText(original.ventaId() == null ? "" : original.ventaId().toString());
        } else {
            inicio.setValue(Formatos.hoy());
            fin.setValue(Formatos.hoy().plusYears(1));
        }
        return DialogoGuardar.mostrar(ventana, original == null ? "Nuevo contrato" : "Editar contrato",
                original == null ? "El contrato nace como borrador" : original.clienteNombre(), formulario, "Guardar",
                () -> {
                    var datos = new ContratoFormulario(cliente.getValue(), venta.getText(), titulo.getText(),
                            inicio.getValue(), fin.getValue(), monto.getText());
                    return () -> service.guardar(original, datos);
                });
    }

    public static Optional<Contrato> cambiarEstado(Window ventana, ContratoService service, Contrato contrato) {
        Formulario formulario = new Formulario();
        ComboBox<EstadoContrato> estado = formulario.campoAncho("Nuevo estado *", "estado", new ComboBox<>());
        estado.getItems().setAll(service.destinosPermitidos(contrato));
        EtiquetaListCell.aplicar(estado, "Elige el estado");
        nota(formulario, "Borrador → vigente o cancelado. Vigente → vencido (solo si su fecha de fin ya pasó) o cancelado. "
                + "Un contrato vencido o cancelado ya no cambia.");
        return DialogoGuardar.mostrar(ventana, "Cambiar estado", contrato.titulo() + " · " + contrato.estado().etiqueta(),
                formulario, "Cambiar estado",
                () -> {
                    EstadoContrato elegido = estado.getValue();
                    return () -> service.cambiarEstado(contrato, elegido);
                });
    }

    public static Optional<Contrato> extender(Window ventana, ContratoService service, Contrato contrato) {
        Formulario formulario = new Formulario();
        DatePicker fin = formulario.campoAncho("Nueva fecha de fin *", ContratoService.FECHA_FIN, new DatePicker());
        fin.setValue(contrato.fechaFin().plusYears(1));
        nota(formulario, "Hoy vence el " + Formatos.fecha(contrato.fechaFin()) + ". La nueva fecha debe ser posterior.");
        return DialogoGuardar.mostrar(ventana, "Extender contrato", contrato.titulo(), formulario, "Extender",
                () -> {
                    var nueva = fin.getValue();
                    return () -> service.extender(contrato, nueva);
                });
    }

    public static Optional<Contrato> renovar(Window ventana, ContratoService service, Contrato contrato) {
        Formulario formulario = new Formulario();
        TextField titulo = formulario.campoAncho("Título", ContratoService.TITULO, new TextField(contrato.titulo()));
        DatePicker inicio = formulario.campo("Inicio *", ContratoService.FECHA_INICIO, new DatePicker());
        DatePicker fin = formulario.campo("Fin *", ContratoService.FECHA_FIN, new DatePicker());
        TextField monto = formulario.campo("Monto (Q) *", ContratoService.MONTO,
                new TextField(contrato.monto() == null ? "" : contrato.monto().toPlainString()));
        inicio.setValue(contrato.fechaFin().plusDays(1));
        fin.setValue(contrato.fechaFin().plusYears(1));
        nota(formulario, "Se crea un contrato nuevo en borrador, ligado a este. Cada contrato se renueva una sola vez.");
        return DialogoGuardar.mostrar(ventana, "Renovar contrato", contrato.titulo() + " · " + contrato.clienteNombre(),
                formulario, "Renovar",
                () -> {
                    String t = titulo.getText();
                    var i = inicio.getValue();
                    var f = fin.getValue();
                    String m = monto.getText();
                    return () -> service.renovar(contrato, t, i, f, m);
                });
    }

    private static void nota(Formulario formulario, String texto) {
        Label nota = new Label(texto);
        nota.getStyleClass().addAll("text-muted", "text-small");
        nota.setWrapText(true);
        formulario.nota(nota);
    }
}
