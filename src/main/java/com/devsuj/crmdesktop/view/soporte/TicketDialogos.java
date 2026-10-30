package com.devsuj.crmdesktop.view.soporte;

import com.devsuj.crmdesktop.model.Cliente;
import com.devsuj.crmdesktop.model.EstadoTicket;
import com.devsuj.crmdesktop.model.PrioridadTicket;
import com.devsuj.crmdesktop.model.Ticket;
import com.devsuj.crmdesktop.model.Usuario;
import com.devsuj.crmdesktop.service.TicketService;
import com.devsuj.crmdesktop.view.comun.DialogoGuardar;
import com.devsuj.crmdesktop.view.comun.EtiquetaListCell;
import com.devsuj.crmdesktop.view.comun.Formulario;
import com.devsuj.crmdesktop.view.comun.Selectores;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;
import java.util.Optional;

/** Diálogos de soporte: abrir/editar, seguimiento, asignar responsable y cambiar estado. */
public final class TicketDialogos {

    private TicketDialogos() {
    }

    /**
     * Abrir ({@code original == null}) o editar un ticket.
     *
     * @param clientes     clientes que el usuario gestiona (solo al abrir)
     * @param responsables a quién se puede asignar al abrir (para un VENDEDOR, solo él mismo)
     */
    public static Optional<Ticket> abrirOEditar(Window ventana, TicketService service, Ticket original,
                                                List<Cliente> clientes, List<Usuario> responsables) {
        Formulario formulario = new Formulario();
        SearchableComboBox<Cliente> cliente = Selectores.clientes(clientes);
        ComboBox<Usuario> responsable = new ComboBox<>();
        if (original == null) {
            formulario.campoAncho("Cliente *", TicketService.CLIENTE, cliente);
            formulario.regla(cliente, true, () -> cliente.getValue() == null ? Optional.of("Elige el cliente.") : Optional.empty());
        }
        Campos campos = campos(formulario);
        if (original == null) {
            formulario.campo("Responsable", "usuarioId", responsable);
            responsable.getItems().add(null);
            responsable.getItems().addAll(responsables);
            EtiquetaListCell.aplicar(responsable, "Sin asignar", Usuario::nombreConRol);
            campos.prioridad().setValue(PrioridadTicket.MEDIA);
        } else {
            campos.llenar(original);
        }
        return DialogoGuardar.mostrar(ventana, original == null ? "Nuevo ticket" : "Editar ticket",
                original == null ? "Registrar un problema o solicitud de un cliente" : "#" + original.id() + " · " + original.clienteNombre(),
                formulario, "Guardar",
                () -> {
                    Cliente c = cliente.getValue();
                    String asunto = campos.asunto().getText();
                    String descripcion = campos.descripcion().getText();
                    PrioridadTicket prioridad = campos.prioridad().getValue();
                    Usuario quien = responsable.getValue();
                    return () -> service.guardar(original, c, asunto, descripcion, prioridad, quien);
                });
    }

    /** Ticket nuevo ligado a uno cerrado (un cerrado no se reabre). */
    public static Optional<Ticket> seguimiento(Window ventana, TicketService service, Ticket cerrado) {
        Formulario formulario = new Formulario();
        Campos campos = campos(formulario);
        campos.llenar(cerrado);
        campos.asunto().setText("Seguimiento: " + cerrado.asunto());
        return DialogoGuardar.mostrar(ventana, "Crear seguimiento",
                "Nuevo ticket ligado al #" + cerrado.id() + " (" + cerrado.clienteNombre() + ")", formulario, "Crear seguimiento",
                () -> {
                    String asunto = campos.asunto().getText();
                    String descripcion = campos.descripcion().getText();
                    PrioridadTicket prioridad = campos.prioridad().getValue();
                    return () -> service.seguimiento(cerrado, asunto, descripcion, prioridad);
                });
    }

    /** Solo GERENTE y ADMIN. */
    public static Optional<Ticket> asignar(Window ventana, TicketService service, Ticket ticket, List<Usuario> responsables) {
        Formulario formulario = new Formulario();
        ComboBox<Usuario> responsable = formulario.campoAncho("Responsable", "usuarioId", new ComboBox<>());
        responsable.getItems().add(null);
        responsable.getItems().addAll(responsables);
        EtiquetaListCell.aplicar(responsable, "Sin asignar", Usuario::nombreConRol);
        responsable.setValue(responsables.stream().filter(u -> u.id().equals(ticket.usuarioId())).findFirst().orElse(null));
        return DialogoGuardar.mostrar(ventana, "Asignar ticket", "#" + ticket.id() + " · " + ticket.asunto(), formulario, "Asignar",
                () -> {
                    Usuario elegido = responsable.getValue();
                    return () -> service.asignar(ticket, elegido);
                });
    }

    public static Optional<Ticket> cambiarEstado(Window ventana, TicketService service, Ticket ticket) {
        Formulario formulario = new Formulario();
        ComboBox<EstadoTicket> estado = formulario.campoAncho("Nuevo estado *", "estado", new ComboBox<>());
        estado.getItems().setAll(TicketService.destinosPermitidos(ticket));
        EtiquetaListCell.aplicar(estado, "Elige el estado");
        return DialogoGuardar.mostrar(ventana, "Cambiar estado", "#" + ticket.id() + " · " + ticket.estado().etiqueta()
                        + (ticket.usuarioId() == null ? " (sin responsable: asígnalo para ponerlo en proceso)" : ""),
                formulario, "Cambiar estado",
                () -> {
                    EstadoTicket elegido = estado.getValue();
                    return () -> service.cambiarEstado(ticket, elegido);
                });
    }

    /** Asunto, prioridad y descripción: los mismos campos al abrir, editar o dar seguimiento (DRY). */
    private record Campos(TextField asunto, ComboBox<PrioridadTicket> prioridad, TextArea descripcion) {
        void llenar(Ticket t) {
            asunto.setText(t.asunto());
            prioridad.setValue(t.prioridad());
            descripcion.setText(t.descripcion());
        }
    }

    private static Campos campos(Formulario formulario) {
        TextField asunto = formulario.campoAncho("Asunto *", TicketService.ASUNTO, new TextField());
        ComboBox<PrioridadTicket> prioridad = formulario.campo("Prioridad *", TicketService.PRIORIDAD, new ComboBox<>());
        prioridad.getItems().setAll(PrioridadTicket.values());
        EtiquetaListCell.aplicar(prioridad, "Elige la prioridad");
        TextArea descripcion = formulario.campoAncho("Descripción", TicketService.DESCRIPCION, new TextArea());
        descripcion.setPrefRowCount(4);
        descripcion.setWrapText(true);
        formulario.regla(asunto, true, () -> asunto.getText().isBlank()
                ? Optional.of("Describe el problema en pocas palabras.") : Optional.empty());
        return new Campos(asunto, prioridad, descripcion);
    }
}
