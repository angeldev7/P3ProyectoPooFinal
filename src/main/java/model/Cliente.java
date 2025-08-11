package model;

import org.bson.Document;

/**
 * Entidad Cliente que representa a un huésped del hotel.
 * Implementa conversión hacia y desde MongoDB Document.
 * 
 * @author asdw
 * @version 1.0
 */
public class Cliente {
    private String id;
    private String nombre;
    private String apellido;
    private String cedula;
    private String telefono;

    /**
     * Constructor por defecto.
     */
    public Cliente() {}

    /**
     * Constructor completo para crear un cliente.
     * 
     * @param id Identificador único del cliente
     * @param nombre Nombre del cliente
     * @param apellido Apellido del cliente
     * @param cedula Cédula de identidad (debe ser única)
     * @param telefono Número telefónico (debe ser único)
     */
    public Cliente(String id, String nombre, String apellido, String cedula, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.telefono = telefono;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Convierte el cliente a un Document de MongoDB.
     * 
     * @return Document para insertar en MongoDB
     */
    public Document toDocument() {
        return new Document("_id", id)
                .append("nombre", nombre)
                .append("apellido", apellido)
                .append("cedula", cedula)
                .append("telefono", telefono);
    }

    /**
     * Crea un Cliente desde un Document de MongoDB.
     * 
     * @param doc Document obtenido de MongoDB
     * @return Nueva instancia de Cliente
     */
    public static Cliente fromDocument(Document doc) {
        return new Cliente(
            doc.getString("_id"),
            doc.getString("nombre"),
            doc.getString("apellido"),
            doc.getString("cedula"),
            doc.getString("telefono")
        );
    }

    /**
     * Obtiene el nombre completo del cliente.
     * 
     * @return Nombre y apellido concatenados
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
    // Representación amigable para listas/combo boxes
    String base = (nombre!=null?nombre:"") + " " + (apellido!=null?apellido:"").trim();
    String doc = cedula!=null?cedula:"";
    return base.trim() + (doc.isEmpty()?"":" ("+doc+")");
    }
}
