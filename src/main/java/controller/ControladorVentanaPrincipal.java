 package controller;

import model.*;
import view.VentanaPrincipal;
import command.*;
import singleton.GestorDisponibilidad;
import builder.ReservaBuilder;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.UUID;

/**
 * Controlador principal de la ventana del sistema hotelero.
 * Implementa MVC con inyección de dependencias y patrón Command para Undo/Redo.
 * 
 * Aplica principios SOLID:
 * - SRP: Se encarga únicamente de coordinar la vista con el modelo
 * - DIP: Depende de abstracciones (interfaces) no de implementaciones concretas
 * - OCP: Extensible mediante nuevos comandos sin modificar el controlador
 * 
 * @author asdw
 * @version 2.0
 */
public class ControladorVentanaPrincipal {
    
    private final VentanaPrincipal vista;
    private final IModeloService modeloService;
    private final ICommandInvoker commandInvoker;
    private final GestorDisponibilidad gestorDisponibilidad;
    private final ControladorHabitaciones controladorHabitaciones;

    /**
     * Constructor con inyección de dependencias.
     * Aplica DIP al recibir abstracciones como parámetros.
     * 
     * @param vista Interfaz gráfica
     * @param modeloService Servicio del modelo (abstracción)
     * @param commandInvoker Gestor de comandos para Undo/Redo
     * @param gestorDisponibilidad Singleton para gestión de disponibilidad
     */
    public ControladorVentanaPrincipal(VentanaPrincipal vista, IModeloService modeloService, 
                                     ICommandInvoker commandInvoker, 
                                     GestorDisponibilidad gestorDisponibilidad) {
        this.vista = vista;
        this.modeloService = modeloService;
        this.commandInvoker = commandInvoker;
        this.gestorDisponibilidad = gestorDisponibilidad;
        
        // Crear controlador de habitaciones con inyección de dependencias
        this.controladorHabitaciones = new ControladorHabitaciones(modeloService, gestorDisponibilidad);
        
        // Inicializar sistema
        inicializarSistema();
        inicializarEventos();
        actualizarVista();
    }

    /**
     * Inicializa el sistema cargando datos básicos.
     */
    private void inicializarSistema() {
        controladorHabitaciones.inicializarHabitaciones();
        actualizarGestorDisponibilidad();
    }

    /**
     * Actualiza el gestor de disponibilidad con el estado actual de las habitaciones.
     */
    private void actualizarGestorDisponibilidad() {
        List<Habitacion> habitaciones = modeloService.obtenerTodasHabitaciones();
        gestorDisponibilidad.inicializar(habitaciones);
    }

    /**
     * Inicializa los eventos de la interfaz gráfica.
     */
    private void inicializarEventos() {
        // Eventos principales
        vista.BtnCheckin.addActionListener(e -> ejecutarCheckin());
        vista.BtnLimpiarCampos.addActionListener(e -> limpiarCamposCheckin());
        vista.jButton2.addActionListener(e -> ejecutarAnularReserva());
        
        // Eventos de Undo/Redo
        vista.BtnUndo.addActionListener(e -> ejecutarUndo());
        vista.BtnRedo.addActionListener(e -> ejecutarRedo());
        
        // Actualizar estado de botones Undo/Redo periódicamente
        Timer timer = new Timer(500, e -> actualizarBotonesUndoRedo());
        timer.start();
    }

    /**
     * Ejecuta el comando de check-in.
     */
    private void ejecutarCheckin() {
        try {
            // Validar datos de entrada
            String nombre = vista.TxtNombre.getText().trim();
            String apellido = vista.TxtApellido.getText().trim();
            String cedula = vista.TxtCedula.getText().trim();
            String telefono = vista.TxtTelefono.getText().trim();
            String habitacionDisplay = (String) vista.CmboxHabitaciones.getSelectedItem();

            if (!validarDatosCheckin(nombre, apellido, cedula, telefono, habitacionDisplay)) {
                return;
            }

            // Crear comando de check-in
            CheckinCommand comando = new CheckinCommand(
                nombre, apellido, cedula, telefono, habitacionDisplay,
                modeloService, this
            );

            // Ejecutar comando
            commandInvoker.executeCommand(comando);
            
            // Limpiar campos y actualizar vista
            limpiarCamposCheckin();
            actualizarVista();
            
        } catch (Exception e) {
            mostrarError("Error al realizar check-in: " + e.getMessage());
        }
    }

    /**
     * Ejecuta el comando de anular reserva.
     */
    private void ejecutarAnularReserva() {
        try {
            String cedula = vista.TxtBuscarCedulaAnular.getText().trim();
            
            if (cedula.isEmpty()) {
                mostrarError("Ingrese su cédula.");
                return;
            }

            // Crear comando de anulación
            AnularReservaCommand comando = new AnularReservaCommand(
                cedula, modeloService, gestorDisponibilidad, this
            );

            // Ejecutar comando
            commandInvoker.executeCommand(comando);
            
            // Limpiar campo y actualizar vista
            vista.TxtBuscarCedulaAnular.setText("");
            actualizarVista();
            
        } catch (Exception e) {
            mostrarError("Error al anular reserva: " + e.getMessage());
        }
    }

    /**
     * Ejecuta undo del último comando.
     */
    private void ejecutarUndo() {
        // Obtener descripción del próximo comando a deshacer
        String descripcion = commandInvoker.getNextUndoDescription();
        if (descripcion == null || descripcion.isEmpty()) {
            mostrarMensaje("No hay operaciones para deshacer.");
            return;
        }
        
        // Mostrar confirmación con mensaje personalizado
        String mensaje = "¿Está seguro que desea deshacer la siguiente operación?\n\n" +
                        "⚠️ " + descripcion + "\n\n" +
                        "Esta acción liberará recursos y modificará el estado del sistema.";
        
        if (mostrarConfirmacion(mensaje, "Confirmar Deshacer")) {
            if (commandInvoker.undo()) {
                actualizarVista();
                mostrarMensaje("✅ Operación deshecha correctamente: " + descripcion);
            } else {
                mostrarError("No se pudo deshacer la operación.");
            }
        }
    }

    /**
     * Ejecuta redo del último comando deshecho.
     */
    private void ejecutarRedo() {
        // Obtener descripción del próximo comando a rehacer
        String descripcion = commandInvoker.getNextRedoDescription();
        if (descripcion == null || descripcion.isEmpty()) {
            mostrarMensaje("No hay operaciones para rehacer.");
            return;
        }
        
        // Mostrar confirmación con mensaje personalizado
        String mensaje = "¿Está seguro que desea rehacer la siguiente operación?\n\n" +
                        "🔄 " + descripcion + "\n\n" +
                        "Esta acción volverá a ejecutar la operación y ocupará recursos.";
        
        if (mostrarConfirmacion(mensaje, "Confirmar Rehacer")) {
            if (commandInvoker.redo()) {
                actualizarVista();
                mostrarMensaje("✅ Operación rehecha correctamente: " + descripcion);
            } else {
                mostrarError("No se pudo rehacer la operación.");
            }
        }
    }

    /**
     * Actualiza el estado de los botones Undo/Redo.
     */
    private void actualizarBotonesUndoRedo() {
        vista.BtnUndo.setEnabled(commandInvoker.canUndo());
        vista.BtnRedo.setEnabled(commandInvoker.canRedo());
        
        // Actualizar tooltips
        if (commandInvoker.canUndo()) {
            vista.BtnUndo.setToolTipText("Deshacer: " + commandInvoker.getNextUndoDescription());
        } else {
            vista.BtnUndo.setToolTipText("No hay operaciones para deshacer");
        }
        
        if (commandInvoker.canRedo()) {
            vista.BtnRedo.setToolTipText("Rehacer: " + commandInvoker.getNextRedoDescription());
        } else {
            vista.BtnRedo.setToolTipText("No hay operaciones para rehacer");
        }
    }

    /**
     * Valida los datos de entrada para check-in.
     */
    private boolean validarDatosCheckin(String nombre, String apellido, String cedula, 
                                       String telefono, String habitacionDisplay) {
        if (nombre.isEmpty() || apellido.isEmpty() || cedula.isEmpty() || 
            telefono.isEmpty() || habitacionDisplay == null) {
            mostrarError("Por favor, complete todos los campos.");
            return false;
        }

        if (!cedula.matches("\\d{10}")) {
            mostrarError("La cédula debe tener exactamente 10 dígitos numéricos.");
            return false;
        }

        if (!telefono.matches("\\d{10}")) {
            mostrarError("El teléfono debe tener exactamente 10 dígitos numéricos.");
            return false;
        }

        // Validar unicidad
        if (existeCedula(cedula)) {
            mostrarError("La cédula ya está registrada.");
            return false;
        }

        if (existeTelefono(telefono)) {
            mostrarError("El teléfono ya está registrado a otro cliente.");
            return false;
        }

        return true;
    }

    /**
     * Verifica si una cédula ya existe en el sistema.
     */
    private boolean existeCedula(String cedula) {
        return modeloService.existeCedula(cedula);
    }

    /**
     * Verifica si un teléfono ya existe en el sistema.
     */
    private boolean existeTelefono(String telefono) {
        return modeloService.existeTelefono(telefono);
    }

    /**
     * Actualiza toda la vista con los datos actuales.
     */
    public void actualizarVista() {
        controladorHabitaciones.cargarHabitaciones(
            vista.TablaHabitacionesDisponibles, 
            vista.TablaHabitacionesOcupadas, 
            vista.CmboxHabitaciones
        );
        actualizarGestorDisponibilidad();
    }

    /**
     * Limpia los campos del formulario de check-in.
     */
    private void limpiarCamposCheckin() {
        vista.TxtNombre.setText("");
        vista.TxtApellido.setText("");
        vista.TxtCedula.setText("");
        vista.TxtTelefono.setText("");
        vista.CmboxHabitaciones.setSelectedIndex(-1);
    }

    /**
     * Muestra un mensaje de error al usuario.
     */
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje informativo al usuario.
     */
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(vista, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Muestra un diálogo de confirmación al usuario.
     * 
     * @param mensaje El mensaje a mostrar
     * @param titulo El título del diálogo
     * @return true si el usuario confirma, false si cancela
     */
    public boolean mostrarConfirmacion(String mensaje, String titulo) {
        int opcion = JOptionPane.showConfirmDialog(
            vista, 
            mensaje, 
            titulo, 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE
        );
        return opcion == JOptionPane.YES_OPTION;
    }

    /**
     * Muestra la factura de check-in.
     */
    public void mostrarFactura(Cliente cliente, Habitacion habitacion, Reserva reserva) {
        StringBuilder sb = new StringBuilder();
        sb.append("========= FACTURA DE CHECK-IN =========\n");
        sb.append("Cliente: ").append(cliente.getNombreCompleto()).append("\n");
        sb.append("Cédula: ").append(cliente.getCedula()).append("\n");
        sb.append("Teléfono: ").append(cliente.getTelefono()).append("\n");
        sb.append("---------------------------------------\n");
        sb.append("Habitación: ").append(habitacion.getDisplayText()).append("\n");
        sb.append("Fecha ingreso: ").append(reserva.getFechaIngreso()).append("\n");
        sb.append("---------------------------------------\n");
        sb.append("TOTAL: $").append(String.format("%.2f", habitacion.getPrecio())).append("\n");
        sb.append("=======================================");
        
        JOptionPane.showMessageDialog(vista, sb.toString(), "Factura", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Obtiene el servicio del modelo para uso de los comandos.
     */
    public IModeloService getModeloService() {
        return modeloService;
    }

    /**
     * Obtiene el gestor de disponibilidad.
     */
    public GestorDisponibilidad getGestorDisponibilidad() {
        return gestorDisponibilidad;
    }
}
