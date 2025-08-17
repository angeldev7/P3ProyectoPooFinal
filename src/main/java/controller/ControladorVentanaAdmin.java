package controller;

import model.*;
import view.VentanaAdmin;
import command.*;
import command.CheckinRapidoAdminCommand;
import command.CrearReservaCommand;
import command.FinalizarReservaCommand;
import singleton.GestorDisponibilidad;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.Toolkit;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import view.VentanaSelector;

/**
 * Controlador para la ventana de administración moderna del hotel.
 * Implementa MVC con inyección de dependencias siguiendo principios SOLID.
 * 
 * Aplica principios SOLID:
 * - SRP: Se encarga únicamente de coordinar la vista admin con el modelo
 * - DIP: Depende de abstracciones (interfaces) no de implementaciones concretas
 * - OCP: Extensible mediante nuevos comandos sin modificar el controlador
 * 
 * @author asdw
 * @version 1.0
 */
public class ControladorVentanaAdmin {
    
    private final VentanaAdmin vista;
    private final IModeloService modeloService;
    private final ICommandInvoker commandInvoker;
    private final GestorDisponibilidad gestorDisponibilidad;
    private final ControladorHabitaciones controladorHabitaciones;
    
    // Paneles de la interfaz
    private final view.panels.PanelClientes panelClientes;
    private final view.panels.PanelHabitaciones panelHabitaciones;
    private final view.panels.PanelReservas panelReservas;
    private final view.panels.PanelServicios panelServicios; // nuevo panel de servicios a la habitación
    private final JPanel panelDashboard; // El dashboard original
    private view.panels.PanelReportes panelReportes; // carga diferida
    
    private static final java.util.logging.Logger logger = 
        java.util.logging.Logger.getLogger(ControladorVentanaAdmin.class.getName());

    // Reglas de validación centralizadas
    private static final int LONGITUD_CEDULA_TELEFONO = 10; // Exactamente 10 dígitos

    /**
     * Constructor con inyección de dependencias.
     * Aplica DIP al recibir abstracciones como parámetros.
     * 
     * @param vista Interfaz gráfica de administración
     * @param modeloService Servicio del modelo (abstracción)
     * @param commandInvoker Gestor de comandos para Undo/Redo
     * @param gestorDisponibilidad Singleton para gestión de disponibilidad
     */
    public ControladorVentanaAdmin(VentanaAdmin vista, IModeloService modeloService, 
                                  ICommandInvoker commandInvoker, 
                                  GestorDisponibilidad gestorDisponibilidad) {
        this.vista = vista;
        this.modeloService = modeloService;
        this.commandInvoker = commandInvoker;
        this.gestorDisponibilidad = gestorDisponibilidad;
        
        // Crear controlador de habitaciones con inyección de dependencias
        this.controladorHabitaciones = new ControladorHabitaciones(modeloService, gestorDisponibilidad);
        
        // Inicializar paneles que gestionará este controlador
        this.panelDashboard = vista.getDashboardPanel(); // Guardamos la referencia al panel original
    this.panelClientes = new view.panels.PanelClientes();
    this.panelHabitaciones = new view.panels.PanelHabitaciones();
    this.panelReservas = new view.panels.PanelReservas();
    this.panelServicios = new view.panels.PanelServicios();
        
        // Inicializar sistema
        inicializarSistema();
        inicializarEventos();
    inicializarEventosClientes();
    inicializarEventosHabitaciones();
    inicializarEventosReservas();
    inicializarEventosServicios();
        actualizarDashboard();
    }

    // ===== Reportes =====
    private void inicializarPanelReportes() {
        panelReportes = new view.panels.PanelReportes();
    }

    private void cargarDatosReportes() {
        try {
            List<Reserva> reservas = modeloService.obtenerTodasReservas();
            double ingresosHoy = calcularIngresosHoy(reservas).doubleValue();
            long activas = reservas.stream().filter(r -> r.getFechaSalida()==null).count();
            panelReportes.setResumen("Ingresos hoy: $"+String.format("%.2f", ingresosHoy)+" | Reservas activas: "+activas);
        } catch (Exception e) {
            panelReportes.setResumen("Error cargando reportes");
        }
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
        // Los eventos del menu se configurarán dinámicamente
        // cuando se implementen las funcionalidades específicas
        configurarEventosMenu();
        
        // Timer para actualizar dashboard cada 30 segundos
        Timer dashboardTimer = new Timer(30000, e -> actualizarDashboard());
        dashboardTimer.start();
    }
    
    /**
     * Configura los eventos del menú de navegación.
     */
    private void configurarEventosMenu() {
        // Este método se puede expandir para agregar funcionalidad específica
        // a cada botón del menú lateral
        logger.info("Eventos del menú configurados");
    }

    /**
     * Actualiza toda la información del dashboard.
     */
    public void actualizarDashboard() {
        SwingUtilities.invokeLater(() -> {
            actualizarTarjetasInformativas();
            actualizarTablaReservas();
        });
    }

    /**
     * Actualiza las tarjetas informativas del dashboard.
     */
    private void actualizarTarjetasInformativas() {
        try {
            List<Habitacion> todasHabitaciones = modeloService.obtenerTodasHabitaciones();
            List<Reserva> todasReservas = modeloService.obtenerTodasReservas();
            
            // Calcular estadísticas
            long habitacionesDisponibles = todasHabitaciones.stream()
                .filter(h -> !h.isOcupada())
                .count();
                
            long habitacionesOcupadas = todasHabitaciones.stream()
                .filter(Habitacion::isOcupada)
                .count();
                
            long checkinHoy = contarCheckinsHoy(todasReservas);
            BigDecimal ingresosHoy = calcularIngresosHoy(todasReservas);
            
            // Actualizar las tarjetas
            vista.actualizarTarjeta("Habitaciones Disponibles", String.valueOf(habitacionesDisponibles));
            vista.actualizarTarjeta("Habitaciones Ocupadas", String.valueOf(habitacionesOcupadas));
            vista.actualizarTarjeta("Check-ins Hoy", String.valueOf(checkinHoy));
            vista.actualizarTarjeta("Ingresos del Día", String.format("$%.2f", ingresosHoy.doubleValue()));
                
        } catch (Exception e) {
            logger.severe("Error actualizando tarjetas informativas: " + e.getMessage());
            mostrarError("Error actualizando información del dashboard");
        }
    }
    
    /**
     * Cuenta los check-ins realizados hoy.
     */
    private long contarCheckinsHoy(List<Reserva> reservas) {
        LocalDate hoy = LocalDate.now();
        return reservas.stream()
            .filter(r -> r.getFechaIngreso().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(hoy))
            .count();
    }
    
    /**
     * Calcula los ingresos del día actual.
     */
    private BigDecimal calcularIngresosHoy(List<Reserva> reservas) {
        LocalDate hoy = LocalDate.now();
        return reservas.stream()
            .filter(r -> r.getFechaIngreso().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().equals(hoy))
            .map(r -> BigDecimal.valueOf(r.getTotal()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Actualiza la tabla de reservas recientes.
     */
    private void actualizarTablaReservas() {
        try {
            List<Reserva> reservasRecientes = modeloService.obtenerReservasRecientes(10);
            List<Habitacion> todasHabitaciones = modeloService.obtenerTodasHabitaciones();
            List<Cliente> todosClientes = modeloService.obtenerTodosClientes();

            // Para optimizar, creamos mapas de búsqueda rápida
            java.util.Map<String, Habitacion> mapaHabitaciones = todasHabitaciones.stream()
                .collect(Collectors.toMap(Habitacion::getId, h -> h));
            java.util.Map<String, Cliente> mapaClientes = todosClientes.stream()
                .collect(Collectors.toMap(Cliente::getId, c -> c));

            // Construir solo filas válidas (sin datos huérfanos)
            java.util.List<String[]> filas = new java.util.ArrayList<>();
            for (Reserva reserva : reservasRecientes) {
                Cliente cliente = mapaClientes.get(reserva.getIdCliente());
                Habitacion habitacionAsociada = mapaHabitaciones.get(reserva.getIdHabitacion());
                if (cliente == null || habitacionAsociada == null) {
                    // Saltar reservas inconsistente para no mostrar 'N/A'
                    continue;
                }
                String numeroHabitacion = habitacionAsociada.getNumero();
                String estadoReserva;
                if (reserva.getFechaSalida() != null) {
                    estadoReserva = "Finalizada";
                } else if (habitacionAsociada.isOcupada()) {
                    estadoReserva = "Check-in";
                } else {
                    estadoReserva = "Reservada";
                }
                String fechaIngreso = reserva.getFechaIngreso() != null ?
                    reserva.getFechaIngreso().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                String fechaSalida = reserva.getFechaSalida() != null ?
                    reserva.getFechaSalida().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Activa";
                filas.add(new String[]{
                    cliente.getNombre()+" "+cliente.getApellido(),
                    numeroHabitacion,
                    fechaIngreso,
                    fechaSalida,
                    estadoReserva
                });
            }
            vista.actualizarTablaReservas(filas.toArray(new String[0][0]));
            
        } catch (Exception e) {
            logger.severe("Error actualizando tabla de reservas: " + e.getMessage());
            mostrarError("Error actualizando tabla de reservas");
        }
    }

    /**
     * Navega a una sección específica del dashboard.
     * 
     * @param seccion Sección a mostrar (Dashboard, Habitaciones, Reservas, etc.)
     */
    public void navegarA(String seccion) {
        SwingUtilities.invokeLater(() -> {
            switch (seccion) {
                case "Dashboard":
                    actualizarDashboard();
                    break;
                case "Habitaciones":
                    mostrarVistaHabitaciones();
                    break;
                case "Reservas":
                    mostrarVistaReservas();
                    break;
                case "Clientes":
                    mostrarVistaClientes();
                    break;
                case "Check-in":
                    mostrarVistaCheckin();
                    break;
                case "Reportes":
                    mostrarVistaReportes();
                    break;
                default:
                    logger.warning("Sección no reconocida: " + seccion);
            }
        });
    }
    
    /**
     * Muestra la vista de gestión de habitaciones.
     */
    private void mostrarVistaHabitaciones() {
        vista.cambiarContenidoPrincipal(panelHabitaciones);
        cargarDatosHabitaciones();
        vista.actualizarTitulo("Habitaciones");
    }
    
    /**
     * Muestra la vista de gestión de reservas.
     */
    private void mostrarVistaReservas() {
        vista.cambiarContenidoPrincipal(panelReservas);
        cargarDatosReservas();
        vista.actualizarTitulo("Reservas");
    }
    
    /**
     * Muestra la vista de gestión de clientes.
     */
    private void mostrarVistaClientes() {
        vista.cambiarContenidoPrincipal(panelClientes);
        actualizarTablaClientes();
        vista.actualizarTitulo("Clientes");
    }
    
    /**
     * Muestra la vista de check-in.
     */
    private void mostrarVistaCheckin() {
        // Reutilizamos panel de habitaciones filtrando solo disponibles
        vista.cambiarContenidoPrincipal(panelHabitaciones);
        cargarDatosHabitaciones();
        vista.actualizarTitulo("Check-in Rápido");
    }
    
    /**
     * Muestra la vista de reportes.
     */
    private void mostrarVistaReportes() {
        if (panelReportes == null) inicializarPanelReportes();
        vista.cambiarContenidoPrincipal(panelReportes);
        cargarDatosReportes();
        vista.actualizarTitulo("Reportes");
    }

    /**
     * Ejecuta un comando con soporte para Undo/Redo.
     * 
     * @param comando Comando a ejecutar
     */
    public void ejecutarComando(ICommand comando) {
        try {
            commandInvoker.executeCommand(comando);
            actualizarDashboard(); // Actualizar después de cada comando
        } catch (Exception e) {
            logger.severe("Error ejecutando comando: " + e.getMessage());
            mostrarError("Error ejecutando operación: " + e.getMessage());
        }
    }

    /**
     * Ejecuta undo del último comando.
     */
    public void ejecutarUndo() {
        try {
            if (!commandInvoker.canUndo()) { mostrarMensaje("No hay operaciones para deshacer"); return; }

            String descripcion = commandInvoker.getNextUndoDescription();
            if (descripcion == null || descripcion.isBlank()) descripcion = "Operación previa";
            String mensaje = "¿Está seguro que desea deshacer la siguiente operación?\n\n" +
                             "⚠️ " + descripcion + "\n\n" +
                             "Esta acción liberará recursos y modificará el estado del sistema.";
            int respuesta = JOptionPane.showConfirmDialog(vista, mensaje, "Confirmar Deshacer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (respuesta == JOptionPane.YES_OPTION) {
                if (commandInvoker.undo()) {
                    actualizarDashboard();
                    mostrarMensaje("✅ Operación deshecha correctamente: " + descripcion);
                } else {
                    mostrarError("No se pudo deshacer la operación.");
                }
            }
        } catch (Exception e) {
            logger.severe("Error en undo: " + e.getMessage());
            mostrarError("Error deshaciendo operación: " + e.getMessage());
        }
    }

    /**
     * Ejecuta redo del último comando deshecho.
     */
    public void ejecutarRedo() {
        try {
            if (!commandInvoker.canRedo()) { mostrarMensaje("No hay operaciones para rehacer"); return; }
            String descripcion = commandInvoker.getNextRedoDescription();
            if (descripcion == null || descripcion.isBlank()) descripcion = "Operación previa";
            String mensaje = "¿Está seguro que desea rehacer la siguiente operación?\n\n" +
                             "🔄 " + descripcion + "\n\n" +
                             "Esta acción volverá a ejecutar la operación y ocupará recursos.";
            int respuesta = JOptionPane.showConfirmDialog(vista, mensaje, "Confirmar Rehacer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (respuesta == JOptionPane.YES_OPTION) {
                if (commandInvoker.redo()) {
                    actualizarDashboard();
                    mostrarMensaje("✅ Operación rehecha correctamente: " + descripcion);
                } else {
                    mostrarError("No se pudo rehacer la operación.");
                }
            }
        } catch (Exception e) {
            logger.severe("Error en redo: " + e.getMessage());
            mostrarError("Error rehaciendo operación: " + e.getMessage());
        }
    }

    /**
     * Verifica si se puede ejecutar undo.
     */
    public boolean puedeDeshacer() {
        return commandInvoker.canUndo();
    }

    /**
     * Verifica si se puede ejecutar redo.
     */
    public boolean puedeRehacer() {
        return commandInvoker.canRedo();
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
     * Obtiene el servicio del modelo para uso externo.
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

    /**
     * Obtiene el command invoker.
     */
    public ICommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    /**
     * Cierra esta ventana y regresa al selector de interfaces.
     */
    public void volverASelector(){
        SwingUtilities.invokeLater(() -> {
            try {
                VentanaSelector selector = new VentanaSelector(modeloService, commandInvoker, gestorDisponibilidad);
                selector.setVisible(true);
                vista.dispose();
            } catch (Exception ex){
                mostrarError("No se pudo abrir selector: "+ex.getMessage());
            }
        });
    }

    /**
     * Gestiona la navegación del menú principal.
     * Este es el núcleo del patrón MVC para la navegación.
     * La vista (VentanaAdmin) notifica al controlador, y el controlador
     * decide qué mostrar.
     * 
     * @param itemMenu El item del menú seleccionado.
     */
    public void navegarHacia(String itemMenu) {
        logger.info("Navegando hacia: " + itemMenu);
        switch (itemMenu) {
            case "Dashboard":
                vista.cambiarContenidoPrincipal(panelDashboard);
                actualizarDashboard(); // Asegurarse de que los datos están frescos
                break;
            case "Clientes":
                vista.cambiarContenidoPrincipal(panelClientes);
                actualizarTablaClientes();
                break;
            case "Habitaciones":
                vista.cambiarContenidoPrincipal(panelHabitaciones);
                cargarDatosHabitaciones();
                break;
            case "Reservas":
                vista.cambiarContenidoPrincipal(panelReservas);
                cargarDatosReservas();
                break;
            case "Servicios":
                vista.cambiarContenidoPrincipal(panelServicios);
                cargarHabitacionesOcupadasEnServicios();
                cargarServiciosDeHabitacionSeleccionada();
                vista.actualizarTitulo("Servicios a la Habitación");
                break;
            // Añadir casos para "Habitaciones", "Reservas", etc.
            default:
                // Opcional: mostrar un panel por defecto o un mensaje
                vista.cambiarContenidoPrincipal(panelDashboard);
                break;
        }
    }

    /**
     * Actualiza la tabla de clientes en el panel de clientes.
     */
    private void actualizarTablaClientes() {
        try {
            List<Cliente> clientes = modeloService.obtenerTodosClientes();
            DefaultTableModel model = (DefaultTableModel) panelClientes.getTablaClientes().getModel();
            model.setRowCount(0); // Limpiar tabla
            for (Cliente cliente : clientes) {
                model.addRow(new Object[]{
                    cliente.getId(),
                    cliente.getNombre(),
                    cliente.getApellido(),
                    cliente.getCedula(),
                    cliente.getTelefono()
                });
            }
        } catch (Exception e) {
            logger.severe("Error al actualizar la tabla de clientes: " + e.getMessage());
            mostrarError("No se pudo cargar la lista de clientes.");
        }
    }

    // ====== HABITACIONES ======
    private void inicializarEventosHabitaciones() {
        panelHabitaciones.getBtnRefrescar().addActionListener(e -> cargarDatosHabitaciones());
        panelHabitaciones.getBtnVolver().addActionListener(e -> navegarHacia("Dashboard"));
        panelHabitaciones.getBtnCheckinRapido().addActionListener(e -> checkinRapidoDesdeHabitaciones());
        panelHabitaciones.getBtnLiberar().addActionListener(e -> liberarHabitacionSeleccionada());

        // Sincronizar selección tabla -> combo (check-in rápido)
        panelHabitaciones.getTablaDisponibles().getSelectionModel().addListSelectionListener((javax.swing.event.ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int fila = panelHabitaciones.getTablaDisponibles().getSelectedRow();
            if (fila >= 0) {
                String numero = panelHabitaciones.getTablaDisponibles().getValueAt(fila,0).toString().trim(); // Ej: #001
                JComboBox<String> combo = panelHabitaciones.getComboSeleccionHabitacion();
                for (int i=0;i<combo.getItemCount();i++) {
                    String item = combo.getItemAt(i);
                    if (item != null && item.startsWith(numero)) { // coincide con inicio
                        if (combo.getSelectedIndex() != i) combo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        // Sincronizar combo -> selección tabla
        panelHabitaciones.getComboSeleccionHabitacion().addActionListener(ev -> {
            String seleccion = (String) panelHabitaciones.getComboSeleccionHabitacion().getSelectedItem();
            if (seleccion == null) return;
            String numero = seleccion.split(" \\|")[0].trim(); // Ej: #001 (split por '|')
            JTable tabla = panelHabitaciones.getTablaDisponibles();
            for (int r=0; r<tabla.getRowCount(); r++) {
                Object val = tabla.getValueAt(r,0);
                if (val != null && val.toString().trim().equals(numero)) {
                    if (tabla.getSelectedRow() != r) {
                        tabla.getSelectionModel().setSelectionInterval(r,r);
                        tabla.scrollRectToVisible(tabla.getCellRect(r,0,true));
                    }
                    break;
                }
            }
        });
    }

    private void cargarDatosHabitaciones() {
        try {
            controladorHabitaciones.cargarHabitaciones(
                panelHabitaciones.getTablaDisponibles(),
                panelHabitaciones.getTablaOcupadas(),
                panelHabitaciones.getComboSeleccionHabitacion()
            );
        } catch (Exception ex) {
            logger.severe("Error cargando habitaciones: " + ex.getMessage());
            mostrarError("No se pudieron cargar las habitaciones.");
        }
    }

    private void checkinRapidoDesdeHabitaciones() {
        String seleccion = (String) panelHabitaciones.getComboSeleccionHabitacion().getSelectedItem();
        if (seleccion == null) {
            mostrarMensaje("No hay habitaciones disponibles para check-in.");
            return;
        }
        // Solicitar datos mínimos del cliente
    JTextField nombre = crearCampoSoloLetras();
    JTextField apellido = crearCampoSoloLetras();
    JTextField cedula = crearCampoSoloDigitos(LONGITUD_CEDULA_TELEFONO);
    JTextField telefono = crearCampoSoloDigitos(LONGITUD_CEDULA_TELEFONO);
        Object[] msg = {"Nombre:", nombre, "Apellido:", apellido, "Cédula:", cedula, "Teléfono:", telefono};
        int op = JOptionPane.showConfirmDialog(vista, msg, "Check-in Rápido", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            try {
                // Formato esperado: "#001 | Tipo | $Precio"
                String numeroHab = seleccion.split(" \\|")[0].replace("#", "").trim();
                Habitacion hab = modeloService.buscarHabitacionPorNumero(numeroHab);
                if (hab == null) {mostrarError("Habitación no encontrada."); return;}
        String nombreVal = capitalizarNombre(nombre.getText().trim());
        String apellidoVal = capitalizarNombre(apellido.getText().trim());
        String cedulaVal = cedula.getText().trim();
        String telefonoVal = telefono.getText().trim();
        String err = validarDatosCliente(nombreVal, apellidoVal, cedulaVal, telefonoVal, true);
        if (err != null) {mostrarError(err); return;}
        Cliente existente = modeloService.buscarClientePorCedula(cedulaVal);
        Cliente cli = existente != null ? existente : new Cliente(java.util.UUID.randomUUID().toString(), nombreVal, apellidoVal, cedulaVal, telefonoVal);
                ejecutarComando(new CheckinRapidoAdminCommand(modeloService, cli, hab));
                mostrarMensaje("Check-in registrado.");
                cargarDatosHabitaciones();
            } catch (Exception ex) {
                mostrarError("Error en check-in: " + ex.getMessage());
            }
        }
    }

    private void liberarHabitacionSeleccionada() {
        int fila = panelHabitaciones.getTablaOcupadas().getSelectedRow();
        if (fila == -1) {mostrarMensaje("Seleccione una habitación ocupada."); return;}
        String numero = panelHabitaciones.getTablaOcupadas().getValueAt(fila,0).toString().replace("#"," ").trim();
        Habitacion hab = modeloService.buscarHabitacionPorNumero(numero.trim());
        if (hab == null) {mostrarError("No se encontró la habitación."); return;}
        // Buscar reserva activa asociada
        List<Reserva> reservas = modeloService.obtenerTodasReservas();
        Reserva activa = reservas.stream().filter(r -> r.getIdHabitacion().equals(hab.getId()) && r.getFechaSalida()==null).findFirst().orElse(null);
        if (activa == null) {mostrarMensaje("No hay reserva activa."); return;}
    ejecutarComando(new FinalizarReservaCommand(modeloService, activa.getId()));
    mostrarMensaje("Habitación liberada.");
    cargarDatosHabitaciones();
    }

    // ====== RESERVAS ======
    private void inicializarEventosReservas() {
        panelReservas.getBtnRefrescar().addActionListener(e -> cargarDatosReservas());
        panelReservas.getBtnVolver().addActionListener(e -> navegarHacia("Dashboard"));
        panelReservas.getBtnNuevaReserva().addActionListener(e -> crearNuevaReserva());
        panelReservas.getBtnFinalizarReserva().addActionListener(e -> finalizarReservaSeleccionada());
        panelReservas.getBtnAjustarReserva().addActionListener(e -> ajustarReservaSeleccionada());
        panelReservas.getBtnAplicarFiltros().addActionListener(e -> cargarDatosReservasFiltradas());
        panelReservas.getBtnLimpiarFiltros().addActionListener(e -> {
            // Reset fechas (desde = hoy -7, hasta = hoy +30) y estado
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DATE, -7);
            panelReservas.getSpFechaDesde().setValue(cal.getTime());
            cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DATE, 30);
            panelReservas.getSpFechaHasta().setValue(cal.getTime());
            panelReservas.getComboEstado().setSelectedIndex(0);
            cargarDatosReservas();
        });
        // Inicializar valores por defecto de filtros
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DATE, -7);
            panelReservas.getSpFechaDesde().setValue(cal.getTime());
            cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DATE, 30);
            panelReservas.getSpFechaHasta().setValue(cal.getTime());
        } catch (Exception ignore) {}
    }

    private void cargarDatosReservas() {
        try {
            List<Reserva> reservas = modeloService.obtenerTodasReservas();
            List<Cliente> clientes = modeloService.obtenerTodosClientes();
            List<Habitacion> habitaciones = modeloService.obtenerTodasHabitaciones();
            java.util.Map<String,Cliente> mapCli = clientes.stream().collect(java.util.stream.Collectors.toMap(Cliente::getId,c->c));
            java.util.Map<String,Habitacion> mapHab = habitaciones.stream().collect(java.util.stream.Collectors.toMap(Habitacion::getId,h->h));
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) panelReservas.getTablaReservas().getModel();
            model.setRowCount(0);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            for (Reserva r: reservas) {
                Cliente c = mapCli.get(r.getIdCliente());
                Habitacion h = mapHab.get(r.getIdHabitacion());
                String estado = r.getFechaSalida()!=null?"Finalizada":(h!=null && h.isOcupada()?"Check-in":"Reservada");
                String fechaReservaStr = r.getFechaReserva()!=null? sdf.format(r.getFechaReserva()):"--";
                String inicioPlanStr = r.getFechaInicioPlanificada()!=null? sdf.format(r.getFechaInicioPlanificada()):"--";
                String finPlanStr = r.getFechaFinPlanificada()!=null? sdf.format(r.getFechaFinPlanificada()):"--";
                String ingresoRealStr = r.getFechaIngreso()!=null? sdf.format(r.getFechaIngreso()):"--";
                String salidaRealStr = r.getFechaSalida()!=null? sdf.format(r.getFechaSalida()):"--";
                int noches = r.getNoches()>0? r.getNoches():1;
                model.addRow(new Object[]{
                    r.getId(),
                    c!=null?c.getNombre()+" "+c.getApellido():"?",
                    h!=null?h.getNumero():"?",
                    fechaReservaStr,
                    inicioPlanStr,
                    finPlanStr,
                    ingresoRealStr,
                    salidaRealStr,
                    noches,
                    estado,
                    String.format("$%.2f", r.getTotal())
                });
            }
        } catch (Exception ex) {
            mostrarError("Error cargando reservas.");
        }
    }

    private void cargarDatosReservasFiltradas() {
        try {
            java.util.Date desde = (java.util.Date) panelReservas.getSpFechaDesde().getValue();
            java.util.Date hasta = (java.util.Date) panelReservas.getSpFechaHasta().getValue();
            if (desde != null && hasta != null && desde.after(hasta)) {
                mostrarError("Rango de fechas inválido: 'Desde' es posterior a 'Hasta'");
                return;
            }
            String estadoFiltro = (String) panelReservas.getComboEstado().getSelectedItem();
            List<Reserva> reservas = modeloService.obtenerTodasReservas();
            java.util.List<Reserva> filtradas = new java.util.ArrayList<>();
            for (Reserva r : reservas) {
                // Determinar fecha para filtrar (usamos fechaReserva si existe, sino ingreso)
                java.util.Date base = r.getFechaReserva()!=null? r.getFechaReserva(): r.getFechaIngreso();
                if (base == null) continue;
                if (desde != null && base.before(normalizarInicioDia(desde))) continue;
                if (hasta != null && base.after(normalizarFinDia(hasta))) continue;
                String estado = calcularEstadoReserva(r);
                if (!"Todos".equalsIgnoreCase(estadoFiltro) && !estadoFiltro.equalsIgnoreCase(estado)) continue;
                filtradas.add(r);
            }
            poblarTablaReservas(filtradas);
        } catch (Exception ex) {
            mostrarError("Error aplicando filtros.");
        }
    }

    private java.util.Date normalizarInicioDia(java.util.Date d){
        java.util.Calendar c = java.util.Calendar.getInstance(); c.setTime(d);
        c.set(java.util.Calendar.HOUR_OF_DAY,0); c.set(java.util.Calendar.MINUTE,0); c.set(java.util.Calendar.SECOND,0); c.set(java.util.Calendar.MILLISECOND,0);
        return c.getTime();
    }
    private java.util.Date normalizarFinDia(java.util.Date d){
        java.util.Calendar c = java.util.Calendar.getInstance(); c.setTime(d);
        c.set(java.util.Calendar.HOUR_OF_DAY,23); c.set(java.util.Calendar.MINUTE,59); c.set(java.util.Calendar.SECOND,59); c.set(java.util.Calendar.MILLISECOND,999);
        return c.getTime();
    }

    private String calcularEstadoReserva(Reserva r){
        try {
            List<Habitacion> habitaciones = modeloService.obtenerTodasHabitaciones();
            Habitacion h = habitaciones.stream().filter(x -> x.getId().equals(r.getIdHabitacion())).findFirst().orElse(null);
            if (r.getFechaSalida()!=null) return "Finalizada";
            if (h!=null && h.isOcupada()) return "Check-in";
            return "Reservada"; // Pendiente de check-in
        } catch (Exception e){return "Reservada";}
    }

    private void poblarTablaReservas(List<Reserva> reservas){
        try {
            List<Cliente> clientes = modeloService.obtenerTodosClientes();
            List<Habitacion> habitaciones = modeloService.obtenerTodasHabitaciones();
            java.util.Map<String,Cliente> mapCli = clientes.stream().collect(java.util.stream.Collectors.toMap(Cliente::getId,c->c));
            java.util.Map<String,Habitacion> mapHab = habitaciones.stream().collect(java.util.stream.Collectors.toMap(Habitacion::getId,h->h));
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) panelReservas.getTablaReservas().getModel();
            model.setRowCount(0);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            for (Reserva r: reservas) {
                Cliente c = mapCli.get(r.getIdCliente());
                Habitacion h = mapHab.get(r.getIdHabitacion());
                String estado = calcularEstadoReserva(r);
                String fechaReservaStr = r.getFechaReserva()!=null? sdf.format(r.getFechaReserva()):"--";
                String inicioPlanStr = r.getFechaInicioPlanificada()!=null? sdf.format(r.getFechaInicioPlanificada()):"--";
                String finPlanStr = r.getFechaFinPlanificada()!=null? sdf.format(r.getFechaFinPlanificada()):"--";
                String ingresoRealStr = r.getFechaIngreso()!=null? sdf.format(r.getFechaIngreso()):"--";
                String salidaRealStr = r.getFechaSalida()!=null? sdf.format(r.getFechaSalida()):"--";
                int noches = r.getNoches()>0? r.getNoches():1;
                model.addRow(new Object[]{
                    r.getId(),
                    c!=null?c.getNombre()+" "+c.getApellido():"?",
                    h!=null?h.getNumero():"?",
                    fechaReservaStr,
                    inicioPlanStr,
                    finPlanStr,
                    ingresoRealStr,
                    salidaRealStr,
                    noches,
                    estado,
                    String.format("$%.2f", r.getTotal())
                });
            }
        } catch (Exception ex){ mostrarError("Error mostrando reservas."); }
    }

    private void ajustarReservaSeleccionada() {
        int fila = panelReservas.getTablaReservas().getSelectedRow();
        if (fila == -1) {mostrarMensaje("Seleccione una reserva."); return;}
        String id = panelReservas.getTablaReservas().getValueAt(fila,0).toString();
        // Buscar reserva
        Reserva reserva = modeloService.obtenerTodasReservas().stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if (reserva == null) {mostrarError("Reserva no encontrada."); return;}
        if (reserva.getFechaSalida()!=null) {mostrarMensaje("No se puede ajustar una reserva finalizada."); return;}
        // Formulario
        JSpinner spInicio = new JSpinner(new javax.swing.SpinnerDateModel(reserva.getFechaInicioPlanificada()!=null?reserva.getFechaInicioPlanificada():new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor ed = new JSpinner.DateEditor(spInicio, "dd/MM/yyyy"); spInicio.setEditor(ed);
        JSpinner spNoches = new JSpinner(new javax.swing.SpinnerNumberModel(reserva.getNoches()>0?reserva.getNoches():1,1,60,1));
        JTextArea obs = new JTextArea(3,25); obs.setLineWrap(true); obs.setWrapStyleWord(true); obs.setText(reserva.getObservaciones()!=null?reserva.getObservaciones():"");
        JPanel p = new JPanel(new java.awt.GridBagLayout()); java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints(); gbc.insets=new java.awt.Insets(4,4,4,4); gbc.anchor=java.awt.GridBagConstraints.WEST; gbc.gridx=0; gbc.gridy=0; p.add(new JLabel("Inicio Planificado:"),gbc); gbc.gridx=1; p.add(spInicio,gbc); gbc.gridx=0; gbc.gridy=1; p.add(new JLabel("Noches:"),gbc); gbc.gridx=1; p.add(spNoches,gbc); gbc.gridx=0; gbc.gridy=2; gbc.anchor=java.awt.GridBagConstraints.NORTHWEST; p.add(new JLabel("Observaciones:"),gbc); gbc.gridx=1; p.add(new JScrollPane(obs),gbc);
        int opt = JOptionPane.showConfirmDialog(vista, p, "Ajustar Reserva "+id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;
        java.util.Date inicio = (java.util.Date) spInicio.getValue();
        int noches = (Integer) spNoches.getValue();
        if (!modeloService.actualizarPlanificacionReserva(id, inicio, noches, obs.getText())) {
            mostrarError("No se pudo actualizar la reserva.");
            return;
        }
        mostrarMensaje("Reserva actualizada.");
        cargarDatosReservasFiltradas();
    }

    private void crearNuevaReserva() {
        try {
            java.util.List<Cliente> clientes = modeloService.obtenerTodosClientes();
            if (clientes.isEmpty()) {mostrarMensaje("No hay clientes. Cree uno primero."); return;}
            java.util.List<Habitacion> disponibles = modeloService.obtenerHabitacionesDisponibles();
            if (disponibles.isEmpty()) {mostrarMensaje("No hay habitaciones disponibles."); return;}

            JComboBox<Cliente> comboCliente = new JComboBox<>(clientes.toArray(new Cliente[0]));
            JComboBox<Habitacion> comboHab = new JComboBox<>(disponibles.toArray(new Habitacion[0]));
            JSpinner spinnerNoches = new JSpinner(new SpinnerNumberModel(1,1,30,1));
            JTextArea observaciones = new JTextArea(3,25);
            observaciones.setLineWrap(true);
            observaciones.setWrapStyleWord(true);
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(4,4,4,4);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Cliente:"), gbc);
            gbc.gridx=1; panel.add(comboCliente, gbc);
            gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("Habitación:"), gbc);
            gbc.gridx=1; panel.add(comboHab, gbc);
            gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("Noches:"), gbc);
            gbc.gridx=1; panel.add(spinnerNoches, gbc);
            gbc.gridx=0; gbc.gridy=3; gbc.anchor=GridBagConstraints.NORTHWEST; panel.add(new JLabel("Observaciones:"), gbc);
            gbc.gridx=1; panel.add(new JScrollPane(observaciones), gbc);

            int opt = JOptionPane.showConfirmDialog(vista, panel, "Nueva Reserva", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) return;

            Cliente seleccionado = (Cliente) comboCliente.getSelectedItem();
            Habitacion hab = (Habitacion) comboHab.getSelectedItem();
            int noches = (Integer) spinnerNoches.getValue();
            double total = hab.getPrecio() * noches;
            String obs = observaciones.getText()!=null?observaciones.getText().trim():"";
            ejecutarComando(new CrearReservaCommand(modeloService, seleccionado.getId(), hab.getId(), total, obs));
            mostrarMensaje("Reserva creada correctamente. Total: $"+String.format("%.2f", total));
            cargarDatosReservas();
        } catch (NumberFormatException nfe) {
            mostrarError("Número de noches inválido.");
        } catch (Exception ex) {
            mostrarError("Error creando reserva: "+ex.getMessage());
        }
    }

    private void finalizarReservaSeleccionada() {
        int fila = panelReservas.getTablaReservas().getSelectedRow();
        if (fila==-1) {mostrarMensaje("Seleccione una reserva."); return;}
        String id = panelReservas.getTablaReservas().getValueAt(fila,0).toString();
        ejecutarComando(new FinalizarReservaCommand(modeloService, id));
        mostrarMensaje("Reserva finalizada.");
        cargarDatosReservas();
    }

    // ====== SERVICIOS A HABITACIÓN ======
    private void inicializarEventosServicios() {
        panelServicios.getBtnRefrescar().addActionListener(e -> {
            cargarHabitacionesOcupadasEnServicios();
            cargarServiciosDeHabitacionSeleccionada();
        });
        panelServicios.getBtnVolver().addActionListener(e -> navegarHacia("Dashboard"));
        panelServicios.getComboHabitacionesOcupadas().addActionListener(e -> cargarServiciosDeHabitacionSeleccionada());
        panelServicios.getBtnAgregarServicio().addActionListener(e -> agregarServicioAHabitacion());
    }

    private void cargarHabitacionesOcupadasEnServicios() {
        try {
            java.util.List<Habitacion> ocupadas = modeloService.obtenerHabitacionesOcupadas();
            javax.swing.JComboBox<String> combo = panelServicios.getComboHabitacionesOcupadas();
            combo.removeAllItems();
            for (Habitacion h: ocupadas) combo.addItem(h.getNumero());
            if (combo.getItemCount()==0) mostrarMensaje("No hay habitaciones ocupadas.");
        } catch (Exception ex) {
            mostrarError("Error cargando habitaciones ocupadas.");
        }
    }

    private void cargarServiciosDeHabitacionSeleccionada() {
        String numero = (String) panelServicios.getComboHabitacionesOcupadas().getSelectedItem();
        if (numero == null) {
            javax.swing.table.DefaultTableModel m = (javax.swing.table.DefaultTableModel) panelServicios.getTablaServicios().getModel();
            m.setRowCount(0);
            return;
        }
        Habitacion hab = modeloService.buscarHabitacionPorNumero(numero);
        if (hab == null) return;
        // Buscar reserva activa para esa habitación
        Reserva activa = modeloService.obtenerTodasReservas().stream()
                .filter(r -> r.getIdHabitacion().equals(hab.getId()) && r.getFechaSalida()==null)
                .findFirst().orElse(null);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) panelServicios.getTablaServicios().getModel();
        model.setRowCount(0);
        if (activa == null) return; // sin reserva activa no hay servicios
        java.util.List<ServicioHabitacion> servicios = modeloService.obtenerServiciosPorReserva(activa.getId());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM HH:mm");
        for (ServicioHabitacion s: servicios) {
            model.addRow(new Object[]{s.getId(), sdf.format(s.getFecha()), s.getTipo(), s.getDescripcion(), String.format("$%.2f", s.getCosto())});
        }
    }

    private void agregarServicioAHabitacion() {
        String numero = (String) panelServicios.getComboHabitacionesOcupadas().getSelectedItem();
        if (numero == null) {mostrarMensaje("Seleccione habitación ocupada."); return;}
        Habitacion hab = modeloService.buscarHabitacionPorNumero(numero);
        if (hab == null || !hab.isOcupada()) {mostrarError("Habitación no válida u ocupación inconsistente."); return;}
        // Reserva activa
        Reserva activa = modeloService.obtenerTodasReservas().stream()
                .filter(r -> r.getIdHabitacion().equals(hab.getId()) && r.getFechaSalida()==null)
                .findFirst().orElse(null);
        if (activa == null) {mostrarError("No hay reserva activa para la habitación."); return;}

        String[] tipos = {ServicioHabitacion.TIPO_COMIDA, ServicioHabitacion.TIPO_BEBIDA, ServicioHabitacion.TIPO_LIMPIEZA};
        JComboBox<String> comboTipo = new JComboBox<>(tipos);
        JTextField descripcion = new JTextField();
        JTextField especiasField = new JTextField(); // lista separada por comas
        JLabel especiasLabel = new JLabel("Especias (solo comida, coma separadas):");
        JPanel panel = new JPanel(new java.awt.GridLayout(0,1,4,4));
        panel.add(new JLabel("Tipo:")); panel.add(comboTipo);
        panel.add(new JLabel("Descripción (opcional):")); panel.add(descripcion);
        panel.add(especiasLabel); panel.add(especiasField);

        Runnable toggleEspecias = () -> {
            boolean visible = ServicioHabitacion.TIPO_COMIDA.equals(comboTipo.getSelectedItem());
            especiasLabel.setVisible(visible);
            especiasField.setVisible(visible);
            especiasField.setEnabled(visible);
            especiasField.setEditable(visible);
            if (!visible) especiasField.setText("");
            panel.revalidate();
            panel.repaint();
        };
        comboTipo.addActionListener(e -> toggleEspecias.run());
        toggleEspecias.run();

        int op = JOptionPane.showConfirmDialog(vista, panel, "Nuevo Servicio", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;
        String tipoSeleccion = (String) comboTipo.getSelectedItem();
        ServicioHabitacion servicio;
        if (ServicioHabitacion.TIPO_COMIDA.equals(tipoSeleccion)) {
            java.util.List<String> especias = new java.util.ArrayList<>();
            for (String s: especiasField.getText().split(",")) {
                String trim = s.trim(); if (!trim.isEmpty()) especias.add(trim);
            }
            servicio = ServicioHabitacion.crearComida(activa.getId(), hab.getId(), descripcion.getText(), especias);
        } else if (ServicioHabitacion.TIPO_BEBIDA.equals(tipoSeleccion)) {
            servicio = ServicioHabitacion.crearBebida(activa.getId(), hab.getId(), descripcion.getText());
        } else { // LIMPIEZA
            servicio = ServicioHabitacion.crearLimpieza(activa.getId(), hab.getId());
        }
        boolean ok = modeloService.registrarServicioHabitacion(servicio);
        if (ok) {
            mostrarMensaje("Servicio registrado (costo fijo: $"+String.format("%.2f", servicio.getCosto())+")");
            cargarServiciosDeHabitacionSeleccionada();
        } else {
            mostrarError("No se pudo registrar el servicio (validación fallida).");
        }
    }

    /**
     * Inicializa los eventos para el panel de gestión de clientes.
     * Siguiendo MVC, el controlador asigna los listeners a los componentes de la vista.
     */
    private void inicializarEventosClientes() {
        panelClientes.getBtnNuevoCliente().addActionListener(e -> agregarNuevoCliente());
        panelClientes.getBtnEditarCliente().addActionListener(e -> editarClienteSeleccionado());
        panelClientes.getBtnEliminarCliente().addActionListener(e -> eliminarClienteSeleccionado());
    panelClientes.getBtnBuscarCedula().addActionListener(e -> buscarClientePorCedula());
    panelClientes.getBtnLimpiarBusqueda().addActionListener(e -> {panelClientes.getTxtBuscarCedula().setText(""); actualizarTablaClientes();});
    }

    /**
     * Muestra un formulario para agregar un nuevo cliente.
     */
    private void agregarNuevoCliente() {
        // Usamos JOptionPane para una entrada de datos simple
    JTextField nombreField = crearCampoSoloLetras();
    JTextField apellidoField = crearCampoSoloLetras();
    JTextField cedulaField = crearCampoSoloDigitos(LONGITUD_CEDULA_TELEFONO);
    JTextField telefonoField = crearCampoSoloDigitos(LONGITUD_CEDULA_TELEFONO);

        Object[] message = {
            "Nombre:", nombreField,
            "Apellido:", apellidoField,
            "Cédula:", cedulaField,
            "Teléfono:", telefonoField
        };

    int option = JOptionPane.showConfirmDialog(vista, message, "Agregar Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
    String nombre = capitalizarNombre(nombreField.getText().trim());
    String apellido = capitalizarNombre(apellidoField.getText().trim());
        String cedula = cedulaField.getText().trim();
        String telefono = telefonoField.getText().trim();

        String validacion = validarDatosCliente(nombre, apellido, cedula, telefono, true);
        if (validacion != null) {mostrarError(validacion); return;}

        Cliente nuevoCliente = new Cliente(null, nombre, apellido, cedula, telefono);
        boolean ok = modeloService.registrarCliente(nuevoCliente);
        if (ok) {actualizarTablaClientes(); mostrarMensaje("Cliente agregado exitosamente.");}
        else {mostrarError("No se pudo registrar. Cédula o teléfono ya existentes.");}

            } catch (Exception ex) {
                logger.severe("Error al agregar cliente: " + ex.getMessage());
                mostrarError("No se pudo agregar el cliente. Verifique los datos.");
            }
        }
    }

    private String validarDatosCliente(String nombre, String apellido, String cedula, String telefono, boolean nuevo){
        if (nombre.isEmpty() || apellido.isEmpty() || cedula.isEmpty() || telefono.isEmpty()) return "Todos los campos son obligatorios";
        if (contieneDigitos(nombre) || contieneDigitos(apellido)) return "Nombre y Apellido no deben contener números";
        if (!cedula.matches("\\d{"+LONGITUD_CEDULA_TELEFONO+"}")) return "La cédula debe tener exactamente "+LONGITUD_CEDULA_TELEFONO+" dígitos";
        if (!telefono.matches("\\d{"+LONGITUD_CEDULA_TELEFONO+"}")) return "El teléfono debe tener exactamente "+LONGITUD_CEDULA_TELEFONO+" dígitos";
        if (nuevo && modeloService.existeCedula(cedula)) return "La cédula ya está registrada";
        if (nuevo && modeloService.existeTelefono(telefono)) return "El teléfono ya está registrado";
        return null;
    }

    private void buscarClientePorCedula(){
        String cedula = panelClientes.getTxtBuscarCedula().getText().trim();
        if (cedula.isEmpty()) {mostrarMensaje("Ingrese una cédula para buscar"); return;}
        Cliente c = modeloService.buscarClientePorCedula(cedula);
        DefaultTableModel model = (DefaultTableModel) panelClientes.getTablaClientes().getModel();
        model.setRowCount(0);
        if (c != null) {
            model.addRow(new Object[]{c.getId(), c.getNombre(), c.getApellido(), c.getCedula(), c.getTelefono()});
        } else {
            mostrarMensaje("No se encontró cliente con esa cédula");
        }
    }

    /**
     * Muestra un formulario para editar el cliente seleccionado en la tabla.
     */
    private void editarClienteSeleccionado() {
        int filaSeleccionada = panelClientes.getTablaClientes().getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(vista, "Por favor, seleccione un cliente para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener datos de la tabla
        String id = (String) panelClientes.getTablaClientes().getValueAt(filaSeleccionada, 0);
        String nombre = (String) panelClientes.getTablaClientes().getValueAt(filaSeleccionada, 1);
        String apellido = (String) panelClientes.getTablaClientes().getValueAt(filaSeleccionada, 2);
        String cedula = (String) panelClientes.getTablaClientes().getValueAt(filaSeleccionada, 3);
        String telefono = (String) panelClientes.getTablaClientes().getValueAt(filaSeleccionada, 4);

        // Pre-llenar el formulario de edición
    JTextField nombreField = crearCampoSoloLetras(); nombreField.setText(nombre);
    JTextField apellidoField = crearCampoSoloLetras(); apellidoField.setText(apellido);
    JTextField cedulaField = crearCampoSoloDigitos(LONGITUD_CEDULA_TELEFONO); cedulaField.setText(cedula);
    JTextField telefonoField = crearCampoSoloDigitos(LONGITUD_CEDULA_TELEFONO); telefonoField.setText(telefono);

        Object[] message = {
            "Nombre:", nombreField,
            "Apellido:", apellidoField,
            "Cédula:", cedulaField,
            "Teléfono:", telefonoField
        };

        int option = JOptionPane.showConfirmDialog(vista, message, "Editar Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String nombreVal = capitalizarNombre(nombreField.getText().trim());
                String apellidoVal = capitalizarNombre(apellidoField.getText().trim());
                String cedulaVal = cedulaField.getText().trim();
                String telefonoVal = telefonoField.getText().trim();
                String err = validarDatosCliente(nombreVal, apellidoVal, cedulaVal, telefonoVal, false);
                if (err != null) {mostrarError(err); return;}
                Cliente clienteActualizado = new Cliente(id, nombreVal, apellidoVal, cedulaVal, telefonoVal);

                boolean ok = modeloService.actualizarCliente(clienteActualizado);
                if (ok) {
                    actualizarTablaClientes();
                    JOptionPane.showMessageDialog(vista, "Cliente actualizado exitosamente.");
                } else {
                    mostrarError("No se pudo actualizar el cliente.");
                }

            } catch (Exception ex) {
                logger.severe("Error al actualizar cliente: " + ex.getMessage());
                mostrarError("No se pudo actualizar el cliente.");
            }
        }
    }

    // ====== UTILIDADES DE VALIDACIÓN Y FORMATEO ======
    private JTextField crearCampoSoloLetras(){
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter(){
            @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string==null) return; replace(fb, offset, 0, string, attr);
            }
            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text!=null && text.matches(".*[0-9].*")) {Toolkit.getDefaultToolkit().beep(); return;}
                fb.replace(offset, length, text, attrs);
            }
        });
        return tf;
    }

    private JTextField crearCampoSoloDigitos(int maxLen){
        JTextField tf = new JTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter(){
            @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException { replace(fb, offset, 0, string, attr); }
            @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text==null) return; String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String nuevo = actual.substring(0, offset) + text + actual.substring(offset+length);
                if (!nuevo.matches("\\d{0,"+maxLen+"}")) {Toolkit.getDefaultToolkit().beep(); return;}
                fb.replace(offset, length, text.replaceAll("[^0-9]", ""), attrs);
            }
        });
        return tf;
    }

    private boolean contieneDigitos(String s){ return s != null && s.matches(".*\\d.*"); }

    private String capitalizarNombre(String input){
        if (input == null) return "";
        String[] partes = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p: partes){
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.length()>1? p.substring(1): "").append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * Elimina el cliente seleccionado en la tabla.
     */
    private void eliminarClienteSeleccionado() {
        int filaSeleccionada = panelClientes.getTablaClientes().getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(vista, "Por favor, seleccione un cliente para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(vista, "¿Está seguro de que desea eliminar a este cliente?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                String id = (String) panelClientes.getTablaClientes().getValueAt(filaSeleccionada, 0);
                boolean ok = modeloService.eliminarCliente(id);
                if (ok) {
                    actualizarTablaClientes();
                    JOptionPane.showMessageDialog(vista, "Cliente eliminado exitosamente.");
                } else {
                    mostrarError("No se pudo eliminar el cliente.");
                }

            } catch (Exception ex) {
                logger.severe("Error al eliminar cliente: " + ex.getMessage());
                mostrarError("No se pudo eliminar el cliente.");
            }
        }
    }
}
