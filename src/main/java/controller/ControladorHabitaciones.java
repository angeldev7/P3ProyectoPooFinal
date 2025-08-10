package controller;

import model.IModeloService;
import model.Habitacion;
import model.HabitacionOcupadaInfo;
import singleton.GestorDisponibilidad;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Controlador especializado para la gestión de habitaciones.
 * Aplica SRP al encargarse exclusivamente de la lógica relacionada con habitaciones.
 * Implementa DIP dependiendo de la abstracción IModeloService.
 * 
 * @author asdw
 * @version 2.0
 */
public class ControladorHabitaciones {

    private final IModeloService modeloService;
    private final GestorDisponibilidad gestorDisponibilidad;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param modeloService Servicio del modelo (abstracción)
     * @param gestorDisponibilidad Gestor de disponibilidad singleton
     */
    public ControladorHabitaciones(IModeloService modeloService, 
                                 GestorDisponibilidad gestorDisponibilidad) {
        this.modeloService = modeloService;
        this.gestorDisponibilidad = gestorDisponibilidad;
    }

    /**
     * Inicializa habitaciones si no existen en el sistema.
     */
    public void inicializarHabitaciones() {
        modeloService.inicializarHabitaciones();
    }

    /**
     * Carga habitaciones en tablas y ComboBox.
     * 
     * @param tablaDisponibles Tabla de habitaciones disponibles
     * @param tablaOcupadas Tabla de habitaciones ocupadas
     * @param comboBox ComboBox de selección de habitaciones
     */
    public void cargarHabitaciones(JTable tablaDisponibles, JTable tablaOcupadas, 
                                 JComboBox<String> comboBox) {
        DefaultTableModel modeloDisp = (DefaultTableModel) tablaDisponibles.getModel();
        DefaultTableModel modeloOcup = (DefaultTableModel) tablaOcupadas.getModel();
        
        modeloDisp.setRowCount(0);
        modeloOcup.setRowCount(0);
        comboBox.removeAllItems();

        List<model.Habitacion> habitacionesDisponibles = modeloService.obtenerHabitacionesDisponibles();
        List<HabitacionOcupadaInfo> habitacionesOcupadas = modeloService.obtenerHabitacionesOcupadasConCliente();
        
        for (model.Habitacion h : habitacionesDisponibles) {
            String display = "#" + h.getNumero() + " | " + h.getTipo() + " | $" + String.format("%.2f", h.getPrecio());
            
            modeloDisp.addRow(new Object[]{
                "#" + h.getNumero(), 
                h.getTipo(), 
                "$" + String.format("%.2f", h.getPrecio())
            });
            comboBox.addItem(display);
        }
        
        for (HabitacionOcupadaInfo info : habitacionesOcupadas) {
            modeloOcup.addRow(new Object[]{
                info.getNumeroFormateado(), 
                info.getTipoHabitacion(), 
                info.getPrecioFormateado(),
                info.getNombreCompletoCliente()
            });
        }
        
        // Si no hay habitaciones disponibles, deshabilitar el ComboBox
        comboBox.setEnabled(comboBox.getItemCount() > 0);
    }

    /**
     * Busca una habitación por su número.
     * 
     * @param numero Número de habitación
     * @return Habitación encontrada o null
     */
    public Habitacion buscarHabitacionPorNumero(String numero) {
        return modeloService.buscarHabitacionPorNumero(numero);
    }

    /**
     * Obtiene todas las habitaciones disponibles.
     * 
     * @return Lista de habitaciones disponibles
     */
    public List<Habitacion> obtenerHabitacionesDisponibles() {
        return gestorDisponibilidad.getHabitacionesDisponibles();
    }

    /**
     * Obtiene todas las habitaciones ocupadas.
     * 
     * @return Lista de habitaciones ocupadas
     */
    public List<Habitacion> obtenerHabitacionesOcupadas() {
        return gestorDisponibilidad.getHabitacionesOcupadas();
    }
}
