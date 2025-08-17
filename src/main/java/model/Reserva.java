package model;

import org.bson.Document;
import java.util.Date;

/**
 * Entidad Reserva que representa una reserva de habitación.
 * Maneja las fechas de ingreso y salida, así como el total de la reserva.
 * 
 * @author asdw
 * @version 1.0
 */
public class Reserva {
    private String id;
    private String idCliente;
    private String idHabitacion;
    private Date fechaIngreso;
    private Date fechaSalida;
    private double total;
    private String observaciones;
    private boolean confirmada;
    // Nuevos campos de planificación
    private Date fechaReserva; // cuando se registró la reserva
    private Date fechaInicioPlanificada; // día que el cliente planea llegar
    private Date fechaFinPlanificada; // día que el cliente planea irse
    private int noches; // número de noches planificadas

    /**
     * Constructor por defecto.
     */
    public Reserva() {}

    /**
     * Constructor básico para crear una reserva.
     * 
     * @param id Identificador único de la reserva
     * @param idCliente ID del cliente que realiza la reserva
     * @param idHabitacion ID de la habitación reservada
     * @param fechaIngreso Fecha de ingreso al hotel
     * @param fechaSalida Fecha de salida del hotel (puede ser null para reservas activas)
     * @param total Monto total de la reserva
     */
    public Reserva(String id, String idCliente, String idHabitacion, Date fechaIngreso, Date fechaSalida, double total) {
        this.id = id;
        this.idCliente = idCliente;
        this.idHabitacion = idHabitacion;
        this.fechaIngreso = fechaIngreso;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.observaciones = "";
        this.confirmada = true;
    // Defaults planificación (compatibilidad hacia atrás)
    this.fechaReserva = fechaIngreso != null ? new Date(fechaIngreso.getTime()) : new Date();
    this.fechaInicioPlanificada = fechaIngreso;
    this.fechaFinPlanificada = fechaSalida;
    this.noches = 1;
    }

    /**
     * Constructor completo para crear una reserva con todos los atributos.
     * 
     * @param id Identificador único de la reserva
     * @param idCliente ID del cliente que realiza la reserva
     * @param idHabitacion ID de la habitación reservada
     * @param fechaIngreso Fecha de ingreso al hotel
     * @param fechaSalida Fecha de salida del hotel
     * @param total Monto total de la reserva
     * @param observaciones Observaciones adicionales
     * @param confirmada Estado de confirmación de la reserva
     */
    public Reserva(String id, String idCliente, String idHabitacion, Date fechaIngreso, 
                   Date fechaSalida, double total, String observaciones, boolean confirmada) {
        this.id = id;
        this.idCliente = idCliente;
        this.idHabitacion = idHabitacion;
        this.fechaIngreso = fechaIngreso;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.observaciones = observaciones;
        this.confirmada = confirmada;
        this.fechaReserva = fechaIngreso != null ? new Date(fechaIngreso.getTime()) : new Date();
        this.fechaInicioPlanificada = fechaIngreso;
        this.fechaFinPlanificada = fechaSalida;
        this.noches = 1;
    }

    /**
     * Constructor completo con planificación explícita.
     */
    public Reserva(String id, String idCliente, String idHabitacion,
                   Date fechaIngreso, Date fechaSalida, double total,
                   String observaciones, boolean confirmada,
                   Date fechaReserva, Date fechaInicioPlanificada, Date fechaFinPlanificada, int noches) {
        this.id = id;
        this.idCliente = idCliente;
        this.idHabitacion = idHabitacion;
        this.fechaIngreso = fechaIngreso;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.observaciones = observaciones;
        this.confirmada = confirmada;
        this.fechaReserva = fechaReserva;
        this.fechaInicioPlanificada = fechaInicioPlanificada;
        this.fechaFinPlanificada = fechaFinPlanificada;
        this.noches = noches;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public String getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(String idHabitacion) { this.idHabitacion = idHabitacion; }
    public Date getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(Date fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public Date getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(Date fechaSalida) { this.fechaSalida = fechaSalida; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public boolean isConfirmada() { return confirmada; }
    public void setConfirmada(boolean confirmada) { this.confirmada = confirmada; }
    public Date getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(Date fechaReserva) { this.fechaReserva = fechaReserva; }
    public Date getFechaInicioPlanificada() { return fechaInicioPlanificada; }
    public void setFechaInicioPlanificada(Date fechaInicioPlanificada) { this.fechaInicioPlanificada = fechaInicioPlanificada; }
    public Date getFechaFinPlanificada() { return fechaFinPlanificada; }
    public void setFechaFinPlanificada(Date fechaFinPlanificada) { this.fechaFinPlanificada = fechaFinPlanificada; }
    public int getNoches() { return noches; }
    public void setNoches(int noches) { this.noches = noches; }

    /**
     * Convierte la reserva a un Document de MongoDB.
     * 
     * @return Document para insertar en MongoDB
     */
    public Document toDocument() {
        return new Document("_id", id)
                .append("idCliente", idCliente)
                .append("idHabitacion", idHabitacion)
                .append("fechaIngreso", fechaIngreso)
                .append("fechaSalida", fechaSalida)
                .append("total", total)
                .append("observaciones", observaciones)
                .append("confirmada", confirmada)
                .append("fechaReserva", fechaReserva)
                .append("fechaInicioPlanificada", fechaInicioPlanificada)
                .append("fechaFinPlanificada", fechaFinPlanificada)
                .append("noches", noches);
    }

    /**
     * Crea una Reserva desde un Document de MongoDB.
     * 
     * @param doc Document obtenido de MongoDB
     * @return Nueva instancia de Reserva
     */
    public static Reserva fromDocument(Document doc) {
        Reserva r = new Reserva(
            doc.getString("_id"),
            doc.getString("idCliente"),
            doc.getString("idHabitacion"),
            doc.getDate("fechaIngreso"),
            doc.getDate("fechaSalida"),
            doc.getDouble("total") != null ? doc.getDouble("total") : 0.0,
            doc.getString("observaciones") != null ? doc.getString("observaciones") : "",
            doc.getBoolean("confirmada", true)
        );
        // cargar campos nuevos si existen
        if (doc.containsKey("fechaReserva")) r.setFechaReserva(doc.getDate("fechaReserva"));
        if (doc.containsKey("fechaInicioPlanificada")) r.setFechaInicioPlanificada(doc.getDate("fechaInicioPlanificada"));
        if (doc.containsKey("fechaFinPlanificada")) r.setFechaFinPlanificada(doc.getDate("fechaFinPlanificada"));
        if (doc.containsKey("noches")) {
            Object n = doc.get("noches");
            if (n instanceof Number) r.setNoches(((Number) n).intValue());
        }
        return r;
    }

    /**
     * Verifica si la reserva está activa (sin fecha de salida).
     * 
     * @return true si la reserva está activa, false en caso contrario
     */
    public boolean isActiva() {
        return fechaSalida == null && confirmada;
    }

    /**
     * Finaliza la reserva estableciendo la fecha de salida.
     */
    public void finalizar() {
        this.fechaSalida = new Date();
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id='" + id + '\'' +
                ", idCliente='" + idCliente + '\'' +
                ", idHabitacion='" + idHabitacion + '\'' +
                ", fechaIngreso=" + fechaIngreso +
                ", fechaSalida=" + fechaSalida +
                ", total=" + total +
                ", observaciones='" + observaciones + '\'' +
                ", confirmada=" + confirmada +
                ", fechaReserva=" + fechaReserva +
                ", fechaInicioPlanificada=" + fechaInicioPlanificada +
                ", fechaFinPlanificada=" + fechaFinPlanificada +
                ", noches=" + noches +
                '}';
    }
}
