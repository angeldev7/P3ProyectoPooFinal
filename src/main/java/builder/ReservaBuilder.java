package builder;

import model.Reserva;
import java.util.Date;
import java.util.UUID;

/**
 * Builder para construir reservas con múltiples atributos.
 * Implementa el patrón Builder para facilitar la creación de objetos Reserva
 * con diferentes combinaciones de atributos opcionales.
 */
public class ReservaBuilder {
    private String id;
    private String idCliente;
    private String idHabitacion;
    private Date fechaIngreso;
    private Date fechaSalida;
    private double total;
    private String observaciones;
    private boolean confirmada;

    /**
     * Constructor que inicializa valores por defecto.
     */
    public ReservaBuilder() {
        this.id = UUID.randomUUID().toString();
        this.fechaIngreso = new Date();
        this.fechaSalida = null;
        this.total = 0.0;
        this.observaciones = "";
        this.confirmada = true;
    }

    /**
     * Establece el ID de la reserva.
     * 
     * @param id Identificador único de la reserva
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Establece el ID del cliente.
     * 
     * @param idCliente Identificador del cliente
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setCliente(String idCliente) {
        this.idCliente = idCliente;
        return this;
    }

    /**
     * Establece el ID de la habitación.
     * 
     * @param idHabitacion Identificador de la habitación
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setHabitacion(String idHabitacion) {
        this.idHabitacion = idHabitacion;
        return this;
    }

    /**
     * Establece la fecha de ingreso.
     * 
     * @param fechaIngreso Fecha de entrada al hotel
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
        return this;
    }

    /**
     * Establece la fecha de salida.
     * 
     * @param fechaSalida Fecha de salida del hotel
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setFechaSalida(Date fechaSalida) {
        this.fechaSalida = fechaSalida;
        return this;
    }

    /**
     * Establece el total de la reserva.
     * 
     * @param total Monto total a pagar
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setTotal(double total) {
        this.total = total;
        return this;
    }

    /**
     * Establece observaciones adicionales.
     * 
     * @param observaciones Comentarios o notas especiales
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setObservaciones(String observaciones) {
        this.observaciones = observaciones;
        return this;
    }

    /**
     * Establece el estado de confirmación.
     * 
     * @param confirmada true si la reserva está confirmada
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setConfirmada(boolean confirmada) {
        this.confirmada = confirmada;
        return this;
    }

    /**
     * Establece la fecha de ingreso como ahora.
     * 
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setFechaIngresoAhora() {
        this.fechaIngreso = new Date();
        return this;
    }

    /**
     * Marca la reserva como check-in inmediato (sin fecha de salida).
     * 
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setCheckinInmediato() {
        this.fechaIngreso = new Date();
        this.fechaSalida = null;
        this.confirmada = true;
        return this;
    }

    /**
     * Configura una reserva temporal (para testing o reservas no confirmadas).
     * 
     * @return ReservaBuilder para encadenamiento
     */
    public ReservaBuilder setTemporal() {
        this.confirmada = false;
        this.observaciones = "Reserva temporal";
        return this;
    }

    /**
     * Valida que los datos mínimos requeridos estén presentes.
     * 
     * @throws IllegalStateException si faltan datos requeridos
     */
    private void validar() {
        if (idCliente == null || idCliente.trim().isEmpty()) {
            throw new IllegalStateException("El ID del cliente es requerido");
        }
        if (idHabitacion == null || idHabitacion.trim().isEmpty()) {
            throw new IllegalStateException("El ID de la habitación es requerido");
        }
        if (fechaIngreso == null) {
            throw new IllegalStateException("La fecha de ingreso es requerida");
        }
        if (total < 0) {
            throw new IllegalStateException("El total no puede ser negativo");
        }
    }

    /**
     * Construye la reserva con los datos configurados.
     * 
     * @return Nueva instancia de Reserva
     * @throws IllegalStateException si faltan datos requeridos
     */
    public Reserva build() {
        validar();
        
        return new Reserva(
            id,
            idCliente,
            idHabitacion,
            fechaIngreso,
            fechaSalida,
            total,
            observaciones,
            confirmada
        );
    }

    /**
     * Crea un builder preconfigurado para check-in.
     * 
     * @param idCliente ID del cliente
     * @param idHabitacion ID de la habitación
     * @param precioNoche Precio por noche
     * @return ReservaBuilder configurado para check-in
     */
    public static ReservaBuilder paraCheckin(String idCliente, String idHabitacion, double precioNoche) {
        return new ReservaBuilder()
                .setCliente(idCliente)
                .setHabitacion(idHabitacion)
                .setTotal(precioNoche)
                .setCheckinInmediato();
    }

    /**
     * Crea un builder preconfigurado para reserva futura.
     * 
     * @param idCliente ID del cliente
     * @param idHabitacion ID de la habitación
     * @param fechaIngreso Fecha de ingreso planeada
     * @param total Total de la reserva
     * @return ReservaBuilder configurado para reserva futura
     */
    public static ReservaBuilder paraReservaFutura(String idCliente, String idHabitacion, 
                                                   Date fechaIngreso, double total) {
        return new ReservaBuilder()
                .setCliente(idCliente)
                .setHabitacion(idHabitacion)
                .setFechaIngreso(fechaIngreso)
                .setTotal(total)
                .setConfirmada(true);
    }
}
