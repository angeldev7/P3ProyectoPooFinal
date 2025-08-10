package memento;

import model.Cliente;
import model.Habitacion;
import model.Reserva;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * Memento que guarda el estado completo del modelo del hotel.
 * Implementa el patrón Memento para permitir restaurar estados anteriores.
 * 
 * @author asdw
 * @version 1.0
 */
public class ModeloMemento {
    
    private final List<Cliente> clientes;
    private final List<Habitacion> habitaciones;
    private final List<Reserva> reservas;
    private final Date timestamp;

    /**
     * Constructor que crea un snapshot del estado actual.
     * 
     * @param clientes Lista actual de clientes
     * @param habitaciones Lista actual de habitaciones
     * @param reservas Lista actual de reservas
     */
    public ModeloMemento(List<Cliente> clientes, List<Habitacion> habitaciones, List<Reserva> reservas) {
        // Crear copias profundas para evitar modificaciones externas
        this.clientes = new ArrayList<>();
        for (Cliente cliente : clientes) {
            this.clientes.add(new Cliente(cliente.getId(), cliente.getNombre(), 
                                        cliente.getApellido(), cliente.getCedula(), cliente.getTelefono()));
        }
        
        this.habitaciones = new ArrayList<>();
        for (Habitacion hab : habitaciones) {
            this.habitaciones.add(new Habitacion(hab.getId(), hab.getNumero(), 
                                               hab.getTipo(), hab.isOcupada(), hab.getPrecio()));
        }
        
        this.reservas = new ArrayList<>();
        for (Reserva reserva : reservas) {
            Date fechaIngreso = reserva.getFechaIngreso() != null ? new Date(reserva.getFechaIngreso().getTime()) : null;
            Date fechaSalida = reserva.getFechaSalida() != null ? new Date(reserva.getFechaSalida().getTime()) : null;
            this.reservas.add(new Reserva(reserva.getId(), reserva.getIdCliente(), reserva.getIdHabitacion(),
                                        fechaIngreso, fechaSalida, reserva.getTotal(), 
                                        reserva.getObservaciones(), reserva.isConfirmada()));
        }
        
        this.timestamp = new Date();
    }

    /**
     * Obtiene la lista de clientes del memento.
     * 
     * @return Copia de la lista de clientes
     */
    public List<Cliente> getClientes() {
        List<Cliente> copy = new ArrayList<>();
        for (Cliente cliente : clientes) {
            copy.add(new Cliente(cliente.getId(), cliente.getNombre(), 
                               cliente.getApellido(), cliente.getCedula(), cliente.getTelefono()));
        }
        return copy;
    }

    /**
     * Obtiene la lista de habitaciones del memento.
     * 
     * @return Copia de la lista de habitaciones
     */
    public List<Habitacion> getHabitaciones() {
        List<Habitacion> copy = new ArrayList<>();
        for (Habitacion hab : habitaciones) {
            copy.add(new Habitacion(hab.getId(), hab.getNumero(), 
                                  hab.getTipo(), hab.isOcupada(), hab.getPrecio()));
        }
        return copy;
    }

    /**
     * Obtiene la lista de reservas del memento.
     * 
     * @return Copia de la lista de reservas
     */
    public List<Reserva> getReservas() {
        List<Reserva> copy = new ArrayList<>();
        for (Reserva reserva : reservas) {
            Date fechaIngreso = reserva.getFechaIngreso() != null ? new Date(reserva.getFechaIngreso().getTime()) : null;
            Date fechaSalida = reserva.getFechaSalida() != null ? new Date(reserva.getFechaSalida().getTime()) : null;
            copy.add(new Reserva(reserva.getId(), reserva.getIdCliente(), reserva.getIdHabitacion(),
                               fechaIngreso, fechaSalida, reserva.getTotal(), 
                               reserva.getObservaciones(), reserva.isConfirmada()));
        }
        return copy;
    }

    /**
     * Obtiene el timestamp de cuando se creó el memento.
     * 
     * @return Fecha y hora de creación
     */
    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    /**
     * Obtiene una descripción del memento.
     * 
     * @return Descripción con estadísticas del estado guardado
     */
    public String getDescription() {
        return String.format("Estado guardado: %d clientes, %d habitaciones, %d reservas - %s",
                           clientes.size(), habitaciones.size(), reservas.size(), timestamp.toString());
    }

    /**
     * Verifica si el memento está vacío.
     * 
     * @return true si no contiene datos
     */
    public boolean isEmpty() {
        return clientes.isEmpty() && habitaciones.isEmpty() && reservas.isEmpty();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
