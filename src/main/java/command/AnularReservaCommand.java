package command;

import model.*;
import singleton.GestorDisponibilidad;
import controller.ControladorVentanaPrincipal;
import java.util.Date;
import java.util.List;

/**
 * Comando para anular una reserva.
 * Implementa el patrón Command con soporte para Undo/Redo.
 * Aplica DIP al depender de la abstracción IModeloService.
 * 
 * @author asdw
 * @version 1.0
 */
public class AnularReservaCommand implements ICommand {
    
    private final String cedula;
    private final IModeloService modeloService;
    private final GestorDisponibilidad gestorDisponibilidad;
    private final ControladorVentanaPrincipal controlador;
    private final long executionTime;
    
    // Estados para undo
    private Cliente cliente;
    private Reserva reservaAnulada;

    /**
     * Constructor del comando de anular reserva.
     */
    public AnularReservaCommand(String cedula, IModeloService modeloService, 
                               GestorDisponibilidad gestorDisponibilidad,
                               ControladorVentanaPrincipal controlador) {
        this.cedula = cedula;
        this.modeloService = modeloService;
        this.gestorDisponibilidad = gestorDisponibilidad;
        this.controlador = controlador;
        this.executionTime = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        try {
            // Si ya tenemos la reserva anulada (redo), finalizar reserva y eliminar cliente de nuevo
            if (reservaAnulada != null && cliente != null) {
                // En redo: finalizar la reserva reactivada y eliminar cliente nuevamente
                if (!modeloService.finalizarReserva(reservaAnulada.getId())) {
                    throw new RuntimeException("Error al finalizar la reserva en redo.");
                }
                
                // Eliminar cliente nuevamente
                if (!modeloService.eliminarCliente(cliente.getId())) {
                    System.err.println("Advertencia: No se pudo eliminar el cliente en redo.");
                }
                
                controlador.mostrarMensaje("Reserva anulada correctamente y cliente eliminado del sistema (redo).");
                controlador.actualizarVista();
                return;
            }
            
            // Primera ejecución: buscar y anular
            // Buscar cliente por cédula
            cliente = modeloService.buscarClientePorCedula(cedula);
            if (cliente == null) {
                throw new RuntimeException("Cliente no encontrado.");
            }
            
            // Buscar reserva activa
            reservaAnulada = modeloService.buscarReservaActivaPorCedula(cedula);
            if (reservaAnulada == null) {
                throw new RuntimeException("No se encontró una reserva activa para este cliente.");
            }
            
            // Finalizar reserva (esto libera la habitación automáticamente)
            if (!modeloService.finalizarReserva(reservaAnulada.getId())) {
                throw new RuntimeException("Error al finalizar la reserva.");
            }
            
            // NUEVO: Eliminar cliente de la base de datos
            // (pero mantenemos la copia en memoria para el undo)
            if (!modeloService.eliminarCliente(cliente.getId())) {
                System.err.println("Advertencia: No se pudo eliminar el cliente de la BD, pero la reserva fue anulada.");
            }
            
            controlador.mostrarMensaje("Reserva anulada correctamente y cliente eliminado del sistema.");
            
            // Actualizar la vista para mostrar los cambios
            controlador.actualizarVista();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al anular reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public void undo() {
        try {
            if (reservaAnulada != null && cliente != null) {
                // Paso 1: Restaurar cliente en la base de datos
                if (!modeloService.registrarCliente(cliente)) {
                    throw new RuntimeException("No se pudo restaurar el cliente en la base de datos.");
                }
                
                // Paso 2: Reactivar la reserva
                List<Habitacion> todasHabitaciones = modeloService.obtenerTodasHabitaciones();
                Habitacion habitacion = todasHabitaciones.stream()
                    .filter(h -> h.getId().equals(reservaAnulada.getIdHabitacion()))
                    .findFirst()
                    .orElse(null);
                    
                if (habitacion != null && !habitacion.isOcupada()) {
                    // Crear nueva reserva para "reactivar" (con nuevo ID)
                    reservaAnulada.setId(java.util.UUID.randomUUID().toString());
                    reservaAnulada.setFechaSalida(null); // Asegurar que esté activa
                    
                    if (modeloService.crearReserva(reservaAnulada)) {
                        controlador.mostrarMensaje("✅ Anulación deshecha: Cliente restaurado y reserva reactivada.");
                    } else {
                        // Si no se pudo crear la reserva, eliminar el cliente nuevamente para mantener consistencia
                        modeloService.eliminarCliente(cliente.getId());
                        throw new RuntimeException("No se pudo reactivar la reserva - habitación no disponible.");
                    }
                } else {
                    // Si no se pudo reactivar, eliminar el cliente nuevamente para mantener consistencia
                    modeloService.eliminarCliente(cliente.getId());
                    throw new RuntimeException("No se pudo reactivar la reserva - habitación no disponible.");
                }
            }
            
            // Actualizar la vista para mostrar los cambios
            controlador.actualizarVista();
            
        } catch (Exception e) {
            System.err.println("Error al deshacer anulación: " + e.getMessage());
            controlador.mostrarMensaje("Error al deshacer anulación de reserva.");
        }
    }

    @Override
    public String getDescription() {
        return "Anular reserva de cliente con cédula " + cedula;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }
}
