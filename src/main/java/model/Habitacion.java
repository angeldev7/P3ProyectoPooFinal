package model;

import org.bson.Document;

/**
 * Entidad Habitación que representa una habitación del hotel.
 * Maneja estados de ocupación y conversión a MongoDB.
 * 
 * @author asdw
 * @version 1.0
 */
public class Habitacion {
    private String id;
    private String numero;
    private String tipo;
    private boolean ocupada;
    private double precio;

    /**
     * Constructor por defecto.
     */
    public Habitacion() {}

    /**
     * Constructor completo para crear una habitación.
     * 
     * @param id Identificador único de la habitación
     * @param numero Número de la habitación
     * @param tipo Tipo de habitación (Suite, Doble, Simple)
     * @param ocupada Estado de ocupación
     * @param precio Precio por noche
     */
    public Habitacion(String id, String numero, String tipo, boolean ocupada, double precio) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.ocupada = ocupada;
        this.precio = precio;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public boolean isOcupada() { return ocupada; }
    public void setOcupada(boolean ocupada) { this.ocupada = ocupada; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    /**
     * Convierte la habitación a un Document de MongoDB.
     * 
     * @return Document para insertar en MongoDB
     */
    public Document toDocument() {
        return new Document("_id", id)
                .append("numero", numero)
                .append("tipo", tipo)
                .append("ocupada", ocupada)
                .append("precio", precio);
    }

    /**
     * Crea una Habitación desde un Document de MongoDB.
     * 
     * @param doc Document obtenido de MongoDB
     * @return Nueva instancia de Habitación
     * @throws NullPointerException si el documento es nulo
     */
    public static Habitacion fromDocument(Document doc) {
        if (doc == null) {
            throw new NullPointerException("El documento no puede ser nulo");
        }
        
        return new Habitacion(
            doc.getString("_id"),
            doc.getString("numero"),
            doc.getString("tipo"),
            doc.getBoolean("ocupada", false),
            doc.getDouble("precio") != null ? doc.getDouble("precio") : 0.0
        );
    }

    /**
     * Obtiene una representación para mostrar en la interfaz.
     * 
     * @return String formateado para mostrar en ComboBox o tablas
     */
    public String getDisplayText() {
        return "#" + numero + " | " + tipo + " | $" + String.format("%.2f", precio);
    }

    /**
     * Verifica si la habitación está disponible para reserva.
     * 
     * @return true si está disponible, false si está ocupada
     */
    public boolean isDisponible() {
        return !ocupada;
    }

    @Override
    public String toString() {
    return "#"+numero+" - "+tipo+" - $"+String.format("%.2f",precio)+(ocupada?" (Ocupada)":"");
    }
}
