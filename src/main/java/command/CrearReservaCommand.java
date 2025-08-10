package command;

import model.*;
import builder.ReservaBuilder;

/**
 * Comando para creación de una nueva reserva (con soporte básico de undo liberando la habitación).
 */
public class CrearReservaCommand implements ICommand {
    private final IModeloService modeloService;
    private final String idCliente;
    private final String idHabitacion;
    private final double total;
    private final String descripcion;
    private Reserva reservaCreada;
    private final long executionTime;

    public CrearReservaCommand(IModeloService modeloService, String idCliente, String idHabitacion, double total, String descripcion) {
        this.modeloService = modeloService;
        this.idCliente = idCliente;
        this.idHabitacion = idHabitacion;
        this.total = total;
        this.descripcion = descripcion;
        this.executionTime = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        if (reservaCreada == null) {
            reservaCreada = new ReservaBuilder()
                .setCliente(idCliente)
                .setHabitacion(idHabitacion)
                .setTotal(total)
                .setFechaIngreso(new java.util.Date())
                .build();
        }
        if (!modeloService.crearReserva(reservaCreada)) {
            throw new RuntimeException("No se pudo crear la reserva");
        }
    }

    @Override
    public void undo() {
        if (reservaCreada != null) {
            modeloService.finalizarReserva(reservaCreada.getId());
        }
    }

    @Override
    public String getDescription() {
        return "Crear reserva: " + descripcion;
    }

    @Override
    public long getExecutionTime() {return executionTime;}
}
