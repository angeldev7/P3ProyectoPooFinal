package command;

import model.*;
import java.util.List;

/**
 * Comando para finalizar (checkout) una reserva activa.
 * Undo intenta reactivar la reserva si la habitación sigue disponible.
 */
public class FinalizarReservaCommand implements ICommand {
    private final IModeloService modeloService;
    private final String idReserva;
    private Reserva reservaOriginal;
    private final long executionTime;

    public FinalizarReservaCommand(IModeloService modeloService, String idReserva) {
        this.modeloService = modeloService;
        this.idReserva = idReserva;
        this.executionTime = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        // Capturar snapshot ligera
        if (reservaOriginal == null) {
            List<Reserva> todas = modeloService.obtenerTodasReservas();
            for (Reserva r : todas) if (r.getId().equals(idReserva)) {reservaOriginal = r; break;}
        }
        if (!modeloService.finalizarReserva(idReserva)) {
            throw new RuntimeException("No se pudo finalizar la reserva");
        }
    }

    @Override
    public void undo() {
        if (reservaOriginal == null) return;
        // Si la reserva original ya está activa (no debería tras execute) no hacemos nada
        if (reservaOriginal.getFechaSalida() == null) return;

        // Evitar duplicar si ya existe una reserva activa para la misma habitación
        List<Reserva> todas = modeloService.obtenerTodasReservas();
        boolean yaExisteActiva = todas.stream().anyMatch(r ->
            r.getFechaSalida() == null && r.getIdHabitacion().equals(reservaOriginal.getIdHabitacion())
        );
        if (yaExisteActiva) return;

        Reserva reactivada = new Reserva(
            null,
            reservaOriginal.getIdCliente(),
            reservaOriginal.getIdHabitacion(),
            new java.util.Date(),
            null,
            reservaOriginal.getTotal(),
            reservaOriginal.getObservaciones(),
            true
        );
        modeloService.crearReserva(reactivada);
    }

    @Override
    public String getDescription() {return "Finalizar reserva " + idReserva;}
    @Override
    public long getExecutionTime() {return executionTime;}
}
