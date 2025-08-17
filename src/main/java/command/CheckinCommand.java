package command;

import model.*;
import controller.ControladorVentanaPrincipal;
import builder.ReservaBuilder;

/**
 * Comando para realizar check-in de un cliente.
 * Implementa el patrón Command con soporte para Undo/Redo.
 * Aplica DIP al depender de la abstracción IModeloService.
 * 
 * @author asdw
 * @version 1.0
 */
public class CheckinCommand implements ICommand {
    
    private final String nombre;
    private final String apellido;
    private final String cedula;
    private final String telefono;
    private final String habitacionDisplay;
    private final IModeloService modeloService;
    private final ControladorVentanaPrincipal controlador;
    private final long executionTime;
    // nuevos campos de planificación
    private final int offsetDiasLlegada; // 0 = hoy
    private final int nochesPlanificadas; // >=1
    
    // Estados para undo
    private Cliente clienteCreado;
    private Reserva reservaCreada;
    private String idHabitacion;
    private boolean primeraEjecucion = true;

    /**
     * Constructor del comando de check-in.
     */
    public CheckinCommand(String nombre, String apellido, String cedula, String telefono,
                         String habitacionDisplay, IModeloService modeloService,
                         ControladorVentanaPrincipal controlador,
                         int offsetDiasLlegada, int nochesPlanificadas) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.telefono = telefono;
        this.habitacionDisplay = habitacionDisplay;
        this.modeloService = modeloService;
        this.controlador = controlador;
        this.executionTime = System.currentTimeMillis();
        this.offsetDiasLlegada = Math.max(0, offsetDiasLlegada);
        this.nochesPlanificadas = Math.max(1, nochesPlanificadas);
    }

    @Override
    public void execute() {
        try {
            // Extraer número de habitación del display (formato: "#001 | Suite | $120.00")
            String numeroHabitacion = habitacionDisplay.split(" \\|")[0].replace("#", "").trim();
            
            // Buscar habitación
            Habitacion habitacion = modeloService.buscarHabitacionPorNumero(numeroHabitacion);
            if (habitacion == null) {
                throw new RuntimeException("Habitación no encontrada.");
            }
            
            idHabitacion = habitacion.getId();
            
            // Si es la primera ejecución, crear cliente nuevo
            if (primeraEjecucion) {
                // Verificar si ya existe un cliente con esta cédula
                Cliente clienteExistente = modeloService.buscarClientePorCedula(cedula);
                if (clienteExistente != null) {
                    clienteCreado = clienteExistente;
                } else {
                    // Crear cliente nuevo
                    String idCliente = java.util.UUID.randomUUID().toString();
                    clienteCreado = new Cliente(idCliente, nombre, apellido, cedula, telefono);
                    
                    if (!modeloService.registrarCliente(clienteCreado)) {
                        throw new RuntimeException("Error al registrar cliente.");
                    }
                }
                primeraEjecucion = false;
            }
            
            // Calcular fechas planificadas usando offset y noches
            java.time.LocalDate hoy = java.time.LocalDate.now();
            java.time.LocalDate inicioPlan = hoy.plusDays(offsetDiasLlegada);
            java.time.LocalDate finPlan = inicioPlan.plusDays(nochesPlanificadas);
            java.util.Date fechaInicioPlan = java.util.Date.from(inicioPlan.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            java.util.Date fechaFinPlan = java.util.Date.from(finPlan.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());

            // Crear nueva reserva: si llegada es hoy => checkin inmediato, si no => reserva futura (sin ocupar aún)
            ReservaBuilder rb = new ReservaBuilder()
                .setCliente(clienteCreado.getId())
                .setHabitacion(idHabitacion)
                .setTotal(habitacion.getPrecio() * nochesPlanificadas);
            if (offsetDiasLlegada == 0) {
                rb.setCheckinInmediato();
            } else {
                // futura: fijamos fechaIngreso planificada pero no ocupamos todavía (fechaIngreso null)
                rb.setFechaIngreso(fechaInicioPlan); // Registro como planificada
            }
            reservaCreada = rb.build();
            reservaCreada.setFechaReserva(new java.util.Date());
            reservaCreada.setFechaInicioPlanificada(fechaInicioPlan);
            reservaCreada.setFechaFinPlanificada(fechaFinPlan);
            reservaCreada.setNoches(nochesPlanificadas);
                
            if (!modeloService.crearReserva(reservaCreada)) {
                throw new RuntimeException("Error al crear reserva.");
            }
            
            // Mostrar factura
            controlador.mostrarFactura(clienteCreado, habitacion, reservaCreada);
            
            // Actualizar la vista para mostrar los cambios
            controlador.actualizarVista();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al ejecutar check-in: " + e.getMessage(), e);
        }
    }

    @Override
    public void undo() {
        try {
            if (reservaCreada != null) {
                // Finalizar reserva (esto libera la habitación automáticamente)
                if (!modeloService.finalizarReserva(reservaCreada.getId())) {
                    System.err.println("No se pudo finalizar la reserva para undo");
                }
            }
            
            // Note: En un sistema real, probablemente no eliminaríamos el cliente
            // sino que solo cancelaríamos la reserva. Pero para este ejemplo de undo completo:
            // No eliminamos el cliente ya que podría tener otras reservas
            
            controlador.mostrarMensaje("Check-in deshecho correctamente.");
            
            // Actualizar la vista para mostrar los cambios
            controlador.actualizarVista();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al deshacer check-in: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDescription() {
    String accion = (offsetDiasLlegada==0?"Check-in":"Reserva planificada");
    return accion+" de " + nombre + " " + apellido + " en habitación " + 
           habitacionDisplay.split(" \\|")[0] +
           (offsetDiasLlegada>0?" (en "+offsetDiasLlegada+" días, "+nochesPlanificadas+" noches)":"");
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }
}
