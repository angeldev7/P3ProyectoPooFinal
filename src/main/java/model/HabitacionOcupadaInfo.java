package model;

/**
 * Clase que encapsula información completa de una habitación ocupada.
 * Incluye datos de la habitación y del cliente que la ocupa.
 * 
 * @author asdw
 * @version 1.0
 */
public class HabitacionOcupadaInfo {
    private String numeroHabitacion;
    private String tipoHabitacion;
    private double precioHabitacion;
    private String nombreCliente;
    private String apellidoCliente;
    private String cedulaCliente;
    private String idReserva;

    /**
     * Constructor completo.
     */
    public HabitacionOcupadaInfo(String numeroHabitacion, String tipoHabitacion, double precioHabitacion,
                                String nombreCliente, String apellidoCliente, String cedulaCliente, String idReserva) {
        this.numeroHabitacion = numeroHabitacion;
        this.tipoHabitacion = tipoHabitacion;
        this.precioHabitacion = precioHabitacion;
        this.nombreCliente = nombreCliente;
        this.apellidoCliente = apellidoCliente;
        this.cedulaCliente = cedulaCliente;
        this.idReserva = idReserva;
    }

    // Getters
    public String getNumeroHabitacion() { return numeroHabitacion; }
    public String getTipoHabitacion() { return tipoHabitacion; }
    public double getPrecioHabitacion() { return precioHabitacion; }
    public String getNombreCliente() { return nombreCliente; }
    public String getApellidoCliente() { return apellidoCliente; }
    public String getCedulaCliente() { return cedulaCliente; }
    public String getIdReserva() { return idReserva; }
    
    /**
     * Obtiene el nombre completo del cliente.
     */
    public String getNombreCompletoCliente() {
        return nombreCliente + " " + apellidoCliente;
    }
    
    /**
     * Obtiene el precio formateado.
     */
    public String getPrecioFormateado() {
        return "$" + String.format("%.2f", precioHabitacion);
    }
    
    /**
     * Obtiene el número de habitación con formato.
     */
    public String getNumeroFormateado() {
        return "#" + numeroHabitacion;
    }

    // Setters
    public void setNumeroHabitacion(String numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }
    public void setTipoHabitacion(String tipoHabitacion) { this.tipoHabitacion = tipoHabitacion; }
    public void setPrecioHabitacion(double precioHabitacion) { this.precioHabitacion = precioHabitacion; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public void setApellidoCliente(String apellidoCliente) { this.apellidoCliente = apellidoCliente; }
    public void setCedulaCliente(String cedulaCliente) { this.cedulaCliente = cedulaCliente; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }

    @Override
    public String toString() {
        return "HabitacionOcupadaInfo{" +
                "numeroHabitacion='" + numeroHabitacion + '\'' +
                ", tipoHabitacion='" + tipoHabitacion + '\'' +
                ", nombreCompleto='" + getNombreCompletoCliente() + '\'' +
                ", cedula='" + cedulaCliente + '\'' +
                '}';
    }
}
