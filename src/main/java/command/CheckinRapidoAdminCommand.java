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
        int noches = 1;
        String inputNoches = javax.swing.JOptionPane.showInputDialog(null, "Noches de hospedaje (check-in rápido)", "1");
        if (inputNoches != null && !inputNoches.trim().isEmpty()) {
            try { noches = Math.max(1, Integer.parseInt(inputNoches.trim())); } catch(NumberFormatException ignored) {}
        }
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDate finPlan = hoy.plusDays(noches);
        java.util.Date fechaInicioPlan = java.util.Date.from(hoy.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        java.util.Date fechaFinPlan = java.util.Date.from(finPlan.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        reserva = ReservaBuilder.paraCheckin(cliente.getId(), habitacion.getId(), habitacion.getPrecio() * noches).build();
        reserva.setFechaReserva(new java.util.Date());
        reserva.setFechaInicioPlanificada(fechaInicioPlan);
        reserva.setFechaFinPlanificada(fechaFinPlan);
        reserva.setNoches(noches);
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
