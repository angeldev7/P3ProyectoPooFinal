package model;

import org.bson.Document;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un servicio a la habitación (room service) asociado a una reserva/habitación ocupada.
 * Mantiene información básica para cálculo y auditoría.
 */
public class ServicioHabitacion {
    public static final String TIPO_COMIDA = "COMIDA";
    public static final String TIPO_BEBIDA = "BEBIDA";
    public static final String TIPO_LIMPIEZA = "LIMPIEZA";

    private static final double PRECIO_COMIDA = 15.0;
    private static final double PRECIO_BEBIDA = 5.0;
    private static final double PRECIO_LIMPIEZA = 10.0;

    private String id;               // SRV-0001
    private String idReserva;        // Reserva asociada (activa al momento del registro)
    private String idHabitacion;     // Redundante para búsquedas rápidas
    private Date fecha;              // Timestamp del servicio
    private String tipo;             // COMIDA | BEBIDA | LIMPIEZA
    private String descripcion;      // Texto libre (ej. detalle de pedido)
    private double costo;            // Costo individual fijo por tipo
    private List<String> especias;   // Solo aplica a COMIDA

    public ServicioHabitacion() {}

    public ServicioHabitacion(String id, String idReserva, String idHabitacion, Date fecha,
                              String tipo, String descripcion, double costo, List<String> especias) {
        this.id = id;
        this.idReserva = idReserva;
        this.idHabitacion = idHabitacion;
        this.fecha = fecha;
        this.tipo = tipo != null ? tipo.toUpperCase() : null;
        this.descripcion = descripcion;
        // Forzar costo fijo por tipo ignorando parámetro variable
        this.costo = costoPorTipo(this.tipo);
        this.especias = especias != null ? new ArrayList<>(especias) : new ArrayList<>();
    }

    // Fábricas estáticas para claridad
    public static ServicioHabitacion crearComida(String idReserva, String idHabitacion, String descripcion, List<String> especias){
        return new ServicioHabitacion(null, idReserva, idHabitacion, new Date(), TIPO_COMIDA, descripcion, PRECIO_COMIDA, especias);
    }
    public static ServicioHabitacion crearBebida(String idReserva, String idHabitacion, String descripcion){
        return new ServicioHabitacion(null, idReserva, idHabitacion, new Date(), TIPO_BEBIDA, descripcion, PRECIO_BEBIDA, null);
    }
    public static ServicioHabitacion crearLimpieza(String idReserva, String idHabitacion){
        return new ServicioHabitacion(null, idReserva, idHabitacion, new Date(), TIPO_LIMPIEZA, "Servicio de limpieza", PRECIO_LIMPIEZA, null);
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getIdReserva() {return idReserva;}
    public void setIdReserva(String idReserva) {this.idReserva = idReserva;}
    public String getIdHabitacion() {return idHabitacion;}
    public void setIdHabitacion(String idHabitacion) {this.idHabitacion = idHabitacion;}
    public Date getFecha() {return fecha;}
    public void setFecha(Date fecha) {this.fecha = fecha;}
    public String getTipo() {return tipo;}
    public void setTipo(String tipo) {this.tipo = tipo;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public double getCosto() {return costo;}
    public void setCosto(double costo) {this.costo = costoPorTipo(this.tipo);} // ignora costo externo
    public List<String> getEspecias(){return especias;}
    public void setEspecias(List<String> especias){this.especias = especias;}

    public static boolean tipoValido(String tipo){
        if (tipo == null) return false;
        String t = tipo.toUpperCase();
        return TIPO_COMIDA.equals(t) || TIPO_BEBIDA.equals(t) || TIPO_LIMPIEZA.equals(t);
    }

    public static double costoPorTipo(String tipo){
        if (tipo == null) return 0.0;
        switch (tipo.toUpperCase()){
            case TIPO_COMIDA: return PRECIO_COMIDA;
            case TIPO_BEBIDA: return PRECIO_BEBIDA;
            case TIPO_LIMPIEZA: return PRECIO_LIMPIEZA;
            default: return 0.0;
        }
    }

    public Document toDocument(){
    return new Document("_id", id)
        .append("idReserva", idReserva)
        .append("idHabitacion", idHabitacion)
        .append("fecha", fecha)
        .append("tipo", tipo)
        .append("descripcion", descripcion)
        .append("costo", costo)
        .append("especias", especias);
    }

    public static ServicioHabitacion fromDocument(Document d){
        List<String> especias = new ArrayList<>();
        Object raw = d.get("especias");
        if (raw instanceof List<?>) {
            for (Object o: (List<?>)raw){
                if (o!=null) especias.add(o.toString());
            }
        }
        ServicioHabitacion s = new ServicioHabitacion(
                d.getString("_id"),
                d.getString("idReserva"),
                d.getString("idHabitacion"),
                d.getDate("fecha"),
                d.getString("tipo"),
                d.getString("descripcion"),
                costoPorTipo(d.getString("tipo")),
                especias
        );
        return s;
    }
}
