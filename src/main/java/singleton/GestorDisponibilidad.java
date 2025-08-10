package singleton;

import model.Habitacion;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

/**
 * Gestor global de disponibilidad de habitaciones usando patrón Singleton.
 * Mantiene un estado centralizado de la disponibilidad de todas las habitaciones.
 * Thread-safe para aplicaciones concurrentes.
 * 
 * @author asdw
 * @version 1.0
 */
public class GestorDisponibilidad {
    
    private static volatile GestorDisponibilidad instance;
    private final Map<String, Boolean> disponibilidadHabitaciones;
    private final Map<String, Habitacion> habitacionesCache;

    /**
     * Constructor privado para implementar Singleton.
     */
    private GestorDisponibilidad() {
        this.disponibilidadHabitaciones = new ConcurrentHashMap<>();
        this.habitacionesCache = new ConcurrentHashMap<>();
    }

    /**
     * Obtiene la instancia única del gestor (Double-checked locking).
     * 
     * @return Instancia única de GestorDisponibilidad
     */
    public static GestorDisponibilidad getInstance() {
        if (instance == null) {
            synchronized (GestorDisponibilidad.class) {
                if (instance == null) {
                    instance = new GestorDisponibilidad();
                }
            }
        }
        return instance;
    }

    /**
     * Inicializa el gestor con una lista de habitaciones.
     * 
     * @param habitaciones Lista de habitaciones del sistema
     */
    public synchronized void inicializar(List<Habitacion> habitaciones) {
        disponibilidadHabitaciones.clear();
        habitacionesCache.clear();
        
        for (Habitacion hab : habitaciones) {
            disponibilidadHabitaciones.put(hab.getId(), hab.isDisponible());
            habitacionesCache.put(hab.getId(), hab);
        }
    }

    /**
     * Verifica si una habitación está disponible.
     * 
     * @param idHabitacion ID de la habitación a verificar
     * @return true si está disponible, false en caso contrario
     */
    public boolean isHabitacionDisponible(String idHabitacion) {
        return disponibilidadHabitaciones.getOrDefault(idHabitacion, false);
    }

    /**
     * Actualiza la disponibilidad de una habitación.
     * 
     * @param idHabitacion ID de la habitación
     * @param disponible Nuevo estado de disponibilidad
     */
    public synchronized void actualizarDisponibilidad(String idHabitacion, boolean disponible) {
        disponibilidadHabitaciones.put(idHabitacion, disponible);
        
        // Actualizar también el cache de habitaciones
        Habitacion hab = habitacionesCache.get(idHabitacion);
        if (hab != null) {
            hab.setOcupada(!disponible);
        }
    }

    /**
     * Obtiene el número de habitaciones disponibles.
     * 
     * @return Cantidad de habitaciones disponibles
     */
    public int getNumeroHabitacionesDisponibles() {
        return (int) disponibilidadHabitaciones.values().stream()
                .mapToInt(disponible -> disponible ? 1 : 0)
                .sum();
    }

    /**
     * Obtiene el número total de habitaciones registradas.
     * 
     * @return Cantidad total de habitaciones
     */
    public int getNumeroTotalHabitaciones() {
        return disponibilidadHabitaciones.size();
    }

    /**
     * Reserva una habitación (la marca como no disponible).
     * 
     * @param idHabitacion ID de la habitación a reservar
     * @return true si se reservó correctamente, false si no estaba disponible
     */
    public synchronized boolean reservarHabitacion(String idHabitacion) {
        if (isHabitacionDisponible(idHabitacion)) {
            actualizarDisponibilidad(idHabitacion, false);
            return true;
        }
        return false;
    }

    /**
     * Libera una habitación (la marca como disponible).
     * 
     * @param idHabitacion ID de la habitación a liberar
     */
    public synchronized void liberarHabitacion(String idHabitacion) {
        actualizarDisponibilidad(idHabitacion, true);
    }

    /**
     * Obtiene una habitación del cache por su ID.
     * 
     * @param idHabitacion ID de la habitación
     * @return Habitación encontrada o null si no existe
     */
    public Habitacion getHabitacion(String idHabitacion) {
        return habitacionesCache.get(idHabitacion);
    }

    /**
     * Obtiene todas las habitaciones disponibles.
     * 
     * @return Lista de habitaciones disponibles
     */
    public List<Habitacion> getHabitacionesDisponibles() {
        return habitacionesCache.values().stream()
                .filter(hab -> disponibilidadHabitaciones.getOrDefault(hab.getId(), false))
                .toList();
    }

    /**
     * Obtiene todas las habitaciones ocupadas.
     * 
     * @return Lista de habitaciones ocupadas
     */
    public List<Habitacion> getHabitacionesOcupadas() {
        return habitacionesCache.values().stream()
                .filter(hab -> !disponibilidadHabitaciones.getOrDefault(hab.getId(), false))
                .toList();
    }

    /**
     * Limpia el estado del gestor (para testing).
     */
    protected synchronized void reset() {
        disponibilidadHabitaciones.clear();
        habitacionesCache.clear();
    }
}
