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
    private double total;
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
            // En modo headless (tests CI) no se puede mostrar JOptionPane; usar valores por defecto.
            boolean headless = java.awt.GraphicsEnvironment.isHeadless();
            int noches = 1;
            java.time.LocalDate inicio = java.time.LocalDate.now();
            if (!headless) {
                String inputNoches = javax.swing.JOptionPane.showInputDialog(null, "Número de noches (1+)", "1");
                if (inputNoches != null && !inputNoches.trim().isEmpty()) {
                    try { noches = Math.max(1, Integer.parseInt(inputNoches.trim())); } catch(NumberFormatException ignored) {}
                }
                String inputDiasInicio = javax.swing.JOptionPane.showInputDialog(null, "Días hasta la llegada (0 = hoy)", "0");
                if (inputDiasInicio != null && !inputDiasInicio.trim().isEmpty()) {
                    try { int offset = Integer.parseInt(inputDiasInicio.trim()); if (offset>=0) inicio = inicio.plusDays(offset); } catch(NumberFormatException ignored) {}
                }
            }
            // Fin planificado = inicio + noches (si noches=1, fin es inicio+1, mostrando noche completa)
            java.time.LocalDate finPlan = inicio.plusDays(noches);
            java.util.Date fechaInicioPlan = java.util.Date.from(inicio.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            java.util.Date fechaFinPlan = java.util.Date.from(finPlan.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            // Recalcular total si se proporcionó uno base (precio noche estimado = total original)
            if (total > 0) {
                // Interpretar 'total' pasado como precio por noche si se creó desde UI anterior
                this.total = total * noches;
            }
            reservaCreada = new ReservaBuilder()
                .setCliente(idCliente)
                .setHabitacion(idHabitacion)
                .setTotal(this.total)
                .setFechaIngreso(fechaInicioPlan) // guardamos como inicio planificado inicial
                .setFechaSalida(null)
                .build();
            // Asignar metadatos de planificación completos
            reservaCreada.setFechaReserva(new java.util.Date());
            reservaCreada.setFechaInicioPlanificada(fechaInicioPlan);
            reservaCreada.setFechaFinPlanificada(fechaFinPlan);
            reservaCreada.setNoches(noches);
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
