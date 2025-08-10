package model;

import memento.ModeloMemento;
import java.util.List;

/**
 * Interfaz de servicio para el modelo del hotel (Dependency Inversion Principle).
 * Los controladores dependen de esta abstracción, no de implementaciones concretas.
 * 
 * @author asdw
 * @version 1.0
 */
public interface IModeloService {
    
    // === OPERACIONES DE CLIENTES ===
    
    /**
     * Registra un nuevo cliente en el sistema.
     * 
     * @param cliente Cliente a registrar
     * @return true si se registró correctamente
     */
    boolean registrarCliente(Cliente cliente);
    
    /**
     * Elimina un cliente del sistema por su ID.
     * PRECAUCIÓN: Esta operación es irreversible en la base de datos.
     * Solo usar cuando se tiene respaldo en memoria para Undo/Redo.
     * 
     * @param idCliente ID del cliente a eliminar
     * @return true si se eliminó correctamente
     */
    boolean eliminarCliente(String idCliente);
    
    /**
     * Busca un cliente por su cédula.
     * 
     * @param cedula Cédula del cliente a buscar
     * @return Cliente encontrado o null si no existe
     */
    Cliente buscarClientePorCedula(String cedula);
    
    /**
     * Busca un cliente por su ID.
     * 
     * @param id ID del cliente a buscar
     * @return Cliente encontrado o null si no existe
     */
    Cliente buscarClientePorId(String id);
    
    /**
     * Obtiene todos los clientes del sistema.
     * 
     * @return Lista de todos los clientes
     */
    List<Cliente> obtenerTodosClientes();

    /**
     * Actualiza los datos de un cliente existente.
     * @param cliente Cliente con datos actualizados (debe tener ID)
     * @return true si se actualizó correctamente
     */
    boolean actualizarCliente(Cliente cliente);
    
    /**
     * Verifica si una cédula ya está registrada.
     * 
     * @param cedula Cédula a verificar
     * @return true si ya existe, false en caso contrario
     */
    boolean existeCedula(String cedula);
    
    /**
     * Verifica si un teléfono ya está registrado.
     * 
     * @param telefono Teléfono a verificar
     * @return true si ya existe, false en caso contrario
     */
    boolean existeTelefono(String telefono);
    
    // === OPERACIONES DE HABITACIONES ===
    
    /**
     * Obtiene todas las habitaciones disponibles.
     * 
     * @return Lista de habitaciones no ocupadas
     */
    List<Habitacion> obtenerHabitacionesDisponibles();
    
    /**
     * Obtiene todas las habitaciones ocupadas.
     * 
     * @return Lista de habitaciones ocupadas
     */
    List<Habitacion> obtenerHabitacionesOcupadas();
    
    /**
     * Obtiene información completa de habitaciones ocupadas con datos del cliente.
     * 
     * @return Lista de objetos con información de habitación y cliente
     */
    List<HabitacionOcupadaInfo> obtenerHabitacionesOcupadasConCliente();
    
    /**
     * Obtiene todas las habitaciones del sistema.
     * 
     * @return Lista de todas las habitaciones
     */
    List<Habitacion> obtenerTodasHabitaciones();
    
    /**
     * Busca una habitación por su número.
     * 
     * @param numero Número de habitación
     * @return Habitación encontrada o null si no existe
     */
    Habitacion buscarHabitacionPorNumero(String numero);
    
    /**
     * Actualiza el estado de ocupación de una habitación.
     * 
     * @param idHabitacion ID de la habitación
     * @param ocupada Nuevo estado de ocupación
     * @return true si se actualizó correctamente
     */
    boolean actualizarEstadoHabitacion(String idHabitacion, boolean ocupada);
    
    /**
     * Inicializa las habitaciones del hotel si no existen.
     */
    void inicializarHabitaciones();
    
    // === OPERACIONES DE RESERVAS ===
    
    /**
     * Crea una nueva reserva en el sistema.
     * 
     * @param reserva Reserva a crear
     * @return true si se creó correctamente
     */
    boolean crearReserva(Reserva reserva);
    
    /**
     * Busca una reserva activa por cédula del cliente.
     * 
     * @param cedula Cédula del cliente
     * @return Reserva activa o null si no existe
     */
    Reserva buscarReservaActivaPorCedula(String cedula);
    
    /**
     * Finaliza una reserva estableciendo la fecha de salida.
     * 
     * @param idReserva ID de la reserva a finalizar
     * @return true si se finalizó correctamente
     */
    boolean finalizarReserva(String idReserva);
    
    /**
     * Obtiene todas las reservas del sistema.
     * 
     * @return Lista de todas las reservas
     */
    List<Reserva> obtenerTodasReservas();
    
    /**
     * Obtiene las reservas más recientes del sistema.
     * 
     * @param limite Número máximo de reservas a obtener
     * @return Lista de reservas recientes
     */
    List<Reserva> obtenerReservasRecientes(int limite);
    
    // === OPERACIONES DE MEMENTO ===
    
    /**
     * Crea un memento del estado actual del sistema.
     * 
     * @return Memento con el estado actual
     */
    ModeloMemento crearMemento();
    
    /**
     * Restaura el sistema al estado guardado en el memento.
     * 
     * @param memento Memento con el estado a restaurar
     */
    void restaurarEstadoCompleto(ModeloMemento memento);
    
    /**
     * Verifica la disponibilidad del sistema.
     * 
     * @return true si el sistema está operativo
     */
    boolean verificarDisponibilidad();
}
