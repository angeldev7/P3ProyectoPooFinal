package view;

import controller.ControladorVentanaAdmin;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Ventana de administración del hotel con diseño moderno
 * @author asdw
 */
public class VentanaAdmin extends javax.swing.JFrame {
    
    // Colores del tema (simplificados para NetBeans)
    private static final Color BACKGROUND_COLOR = new Color(240, 242, 247);
    private static final Color SIDEBAR_COLOR = new Color(52, 58, 76);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(72, 126, 176);
    private static final Color TEXT_COLOR = new Color(52, 58, 76);
    private static final Color TEXT_WHITE = Color.WHITE;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaAdmin.class.getName());
    
    // Controlador
    private ControladorVentanaAdmin controlador;
    
    // Componentes principales para acceso público
    public JTable tablaReservas;
    public Map<String, JLabel> tarjetasInfo;
    public JPanel contenidoPrincipal;
    private JPanel dashboardInicial; // referencia estable al panel dashboard original

    /**
     * Creates new form VentanaAdmin
     */
    public VentanaAdmin() {
        initComponents();
        inicializarComponentes();
        setupModernUI();
    }
    
    /**
     * Inicializa los componentes adicionales.
     */
    private void inicializarComponentes() {
        tarjetasInfo = new HashMap<>();
    }
    
    /**
     * Establece el controlador para esta vista.
     * 
     * @param controlador Controlador de la ventana admin
     */
    public void setControlador(ControladorVentanaAdmin controlador) {
        this.controlador = controlador;
    }
    
    /**
     * Configura la interfaz moderna después de initComponents()
     */
    private void setupModernUI() {
        // Configurar ventana principal
        this.setTitle("Hotel Admin - Sistema de Gestión");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Crear layout principal
        createModernLayout();
        
        // Aplicar estilos
        applyModernStyles();
    }
    
    /**
     * Crea el layout moderno de la interfaz
     */
    private void createModernLayout() {
        // Limpiar contenido existente
        getContentPane().removeAll();
        
        // Layout principal
        setLayout(new BorderLayout());
        
        // Crear sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // Crear contenido principal
        JPanel mainContent = createMainContent();
        add(mainContent, BorderLayout.CENTER);
        
        revalidate();
        repaint();
    }
    
    /**
     * Crea la barra lateral de navegación
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header del sidebar
        JLabel hotelLabel = new JLabel("🏨 HOTEL ADMIN");
        hotelLabel.setForeground(TEXT_WHITE);
        hotelLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hotelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Espaciador
        sidebar.add(hotelLabel);
        sidebar.add(Box.createVerticalStrut(30));
        
        // Botones de navegación
    String[] menuItems = {"Dashboard", "Habitaciones", "Reservas", "Clientes", "Servicios", "Check-in", "Reportes"};
        
        for (String item : menuItems) {
            JButton button = createMenuButton(item);
            // Conectar el botón con el controlador
            button.addActionListener(e -> handleMenuSelection(item));
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(10));
        }
        
        // Espaciador flexible
        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }
    
    /**
     * Crea un botón de menú estilizado
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 0, 0, 0)); // Transparente
        button.setForeground(TEXT_WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(210, 35));
        button.setPreferredSize(new Dimension(210, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Agregar listener para navegación
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controlador != null) {
                    controlador.navegarA(text);
                }
            }
        });
        
        // Efectos hover (simulado)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
                button.setOpaque(true);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 0, 0, 0));
                button.setOpaque(false);
            }
        });
        
        return button;
    }
    
    /**
     * Crea el contenido principal
     */
    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel header = createHeader();
        mainPanel.add(header, BorderLayout.NORTH);
        
        // Dashboard cards
    contenidoPrincipal = createDashboard();
    // Guardar referencia inicial para navegación de regreso
    dashboardInicial = contenidoPrincipal;
    mainPanel.add(contenidoPrincipal, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    /**
     * Crea el header superior
     */
    private JLabel headerTitle; // para actualizar dinámicamente
    private JButton btnUndo; 
    private JButton btnRedo;
    private Timer undoRedoTooltipTimer;

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND_COLOR);

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
        leftGroup.setOpaque(false);
        JButton btnVolverSelector = new JButton("← Volver");
        btnVolverSelector.setFocusPainted(false);
        btnVolverSelector.setBackground(ACCENT_COLOR);
        btnVolverSelector.setForeground(Color.WHITE);
        btnVolverSelector.addActionListener(e -> {
            if (controlador!=null) controlador.volverASelector();
        });
        headerTitle = new JLabel("Dashboard");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerTitle.setForeground(TEXT_COLOR);
        leftGroup.add(btnVolverSelector);
        leftGroup.add(headerTitle);
        header.add(leftGroup, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,5));
        acciones.setOpaque(false);
        btnUndo = new JButton("⮪ Undo");
        btnRedo = new JButton("⮫ Redo");
        JButton[] arr = {btnUndo, btnRedo};
        for (JButton b: arr){
            b.setFocusPainted(false);
            b.setBackground(ACCENT_COLOR);
            b.setForeground(Color.WHITE);
        }
        btnUndo.addActionListener(e -> {
            if (controlador!=null) controlador.ejecutarUndo();
            actualizarEstadoUndoRedo();
        });
        btnRedo.addActionListener(e -> {
            if (controlador!=null) controlador.ejecutarRedo();
            actualizarEstadoUndoRedo();
        });
    btnUndo.setToolTipText("Deshacer la última operación (revierte cambios realizados, como creación o finalización de reservas).");
    btnRedo.setToolTipText("Rehacer la operación deshecha (vuelve a aplicar el cambio revertido). ");
        acciones.add(btnUndo);
        acciones.add(btnRedo);
        header.add(acciones, BorderLayout.EAST);
        iniciarActualizacionUndoRedo();
        return header;
    }

    public void actualizarTitulo(String titulo){
        SwingUtilities.invokeLater(() -> headerTitle.setText(titulo));
    }

    private void iniciarActualizacionUndoRedo(){
        undoRedoTooltipTimer = new Timer(1500, e -> actualizarEstadoUndoRedo());
        undoRedoTooltipTimer.setRepeats(true);
        undoRedoTooltipTimer.start();
    }

    private void actualizarEstadoUndoRedo(){
        if (controlador==null) return;
        String siguienteUndo = controlador.getCommandInvoker().getNextUndoDescription();
        String siguienteRedo = controlador.getCommandInvoker().getNextRedoDescription();
        boolean canUndo = controlador.puedeDeshacer();
        boolean canRedo = controlador.puedeRehacer();
        btnUndo.setEnabled(canUndo);
        btnRedo.setEnabled(canRedo);
        btnUndo.setText(canUndo?"⮪ Undo" : "⮪ Undo");
        btnRedo.setText(canRedo?"⮫ Redo" : "⮫ Redo");
    btnUndo.setToolTipText(canUndo?"Deshacer: "+siguienteUndo+" (revierte efectos)":"No hay acciones para deshacer");
    btnRedo.setToolTipText(canRedo?"Rehacer: "+siguienteRedo+" (vuelve a aplicar)":"No hay acciones para rehacer");
    }
    
    /**
     * Crea el dashboard con tarjetas de información
     */
    private JPanel createDashboard() {
        JPanel dashboard = new JPanel(new GridBagLayout());
        dashboard.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Tarjetas de información
        gbc.gridx = 0; gbc.gridy = 0;
        dashboard.add(createInfoCard("Habitaciones Disponibles", "15", "🛏️"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        dashboard.add(createInfoCard("Habitaciones Ocupadas", "5", "🔒"), gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        dashboard.add(createInfoCard("Check-ins Hoy", "3", "✅"), gbc);
        
        gbc.gridx = 3; gbc.gridy = 0;
        dashboard.add(createInfoCard("Ingresos del Día", "$1,250", "💰"), gbc);
        
        // Tabla de reservas recientes
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weighty = 2.0;
        dashboard.add(createReservationsTable(), gbc);
        
        return dashboard;
    }
    
    /**
     * Crea una tarjeta de información
     */
    private JPanel createInfoCard(String title, String value, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Icono
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Valor
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(ACCENT_COLOR);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Título
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);
        
        // Guardar referencia al valor para actualizaciones
        tarjetasInfo.put(title, valueLabel);
        
        return card;
    }
    
    /**
     * Crea la tabla de reservas
     */
    private JScrollPane createReservationsTable() {
        String[] columnNames = {"Cliente", "Habitación", "Check-in", "Check-out", "Estado"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        tablaReservas = new JTable(model);
        tablaReservas.setBackground(CARD_COLOR);
        tablaReservas.setSelectionBackground(ACCENT_COLOR);
        tablaReservas.setSelectionForeground(TEXT_WHITE);
        tablaReservas.setRowHeight(30);
        tablaReservas.getTableHeader().setBackground(SIDEBAR_COLOR);
        tablaReservas.getTableHeader().setForeground(TEXT_WHITE);
        tablaReservas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(tablaReservas);
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                "Reservas Recientes",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                TEXT_COLOR
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        return scrollPane;
    }
    
    /**
     * Aplica estilos modernos a los componentes
     */
    private void applyModernStyles() {
        // Establecer Look and Feel más moderno si está disponible
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName()) || "Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    SwingUtilities.updateComponentTreeUI(this);
                    break;
                }
            }
        } catch (Exception e) {
            logger.warning("No se pudo establecer Look and Feel moderno: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza el valor de una tarjeta informativa.
     * 
     * @param titulo Título de la tarjeta
     * @param nuevoValor Nuevo valor a mostrar
     */
    public void actualizarTarjeta(String titulo, String nuevoValor) {
        SwingUtilities.invokeLater(() -> {
            JLabel label = tarjetasInfo.get(titulo);
            if (label != null) {
                label.setText(nuevoValor);
            }
        });
    }
    
    /**
     * Actualiza la tabla de reservas con nuevos datos.
     * 
     * @param datos Matriz con los datos de las reservas
     */
    public void actualizarTablaReservas(String[][] datos) {
        SwingUtilities.invokeLater(() -> {
            if (tablaReservas != null) {
                DefaultTableModel model = (DefaultTableModel) tablaReservas.getModel();
                model.setRowCount(0); // Limpiar filas existentes
                for (String[] fila : datos) {
                    model.addRow(fila);
                }
            }
        });
    }
    
    /**
     * Cambia el contenido principal del dashboard.
     * 
     * @param nuevoContenido Panel con el nuevo contenido
     */
    public void cambiarContenidoPrincipal(JPanel nuevoContenido) {
        SwingUtilities.invokeLater(() -> {
            if (contenidoPrincipal != null && contenidoPrincipal.getParent() != null) {
                Container parent = contenidoPrincipal.getParent();
                parent.remove(contenidoPrincipal);
                contenidoPrincipal = nuevoContenido;
                parent.add(contenidoPrincipal, BorderLayout.CENTER);
                parent.revalidate();
                parent.repaint();
            }
        });
    }

    /**
     * Maneja la selección de un item del menú.
     * Notifica al controlador para que realice la acción correspondiente.
     * 
     * @param item El texto del item del menú seleccionado.
     */
    private void handleMenuSelection(String item) {
        if (controlador != null) {
            controlador.navegarHacia(item);
        } else {
            logger.warning("Controlador no está inicializado en VentanaAdmin.");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 552, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 520, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new VentanaAdmin().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    /**
     * Devuelve el panel dashboard original (no el contenedor padre),
     * permitiendo volver a mostrarlo tras navegar a otras vistas.
     */
    public JPanel getDashboardPanel() {
        return dashboardInicial;
    }
}
