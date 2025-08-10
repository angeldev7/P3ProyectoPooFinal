package command;

import model.*;
import builder.ReservaBuilder;

/**
 * Comando para check-in rápido desde panel de habitaciones en admin.
 */
public class CheckinRapidoAdminCommand implements ICommand {
    private final IModeloService modeloService;
    private final Cliente cliente;
    private final Habitacion habitacion;
    private Reserva reserva;
    private final long executionTime;

    public CheckinRapidoAdminCommand(IModeloService modeloService, Cliente cliente, Habitacion habitacion) {
        this.modeloService = modeloService;
        this.cliente = cliente;
        this.habitacion = habitacion;
        this.executionTime = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        if (cliente.getId() == null || modeloService.buscarClientePorId(cliente.getId()) == null) {
            if (!modeloService.registrarCliente(cliente)) {
                throw new RuntimeException("No se pudo registrar el cliente");
            }
        }
        reserva = ReservaBuilder.paraCheckin(cliente.getId(), habitacion.getId(), habitacion.getPrecio()).build();
        if (!modeloService.crearReserva(reserva)) {
            throw new RuntimeException("No se pudo crear la reserva de check-in");
        }
    }

    @Override
    public void undo() {
        if (reserva != null) {
            modeloService.finalizarReserva(reserva.getId());
        }
    }

    @Override
    public String getDescription() {return "Check-in rápido hab " + habitacion.getNumero();}
    @Override
    public long getExecutionTime() {return executionTime;}
}
