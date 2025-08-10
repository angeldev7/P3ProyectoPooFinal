package view;

import controller.ControladorVentanaPrincipal;
import controller.ControladorVentanaAdmin;
import model.*;
import command.*;
import singleton.GestorDisponibilidad;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Ventana de selección de interfaz del sistema hotelero.
 * Permite elegir entre la interfaz tradicional y la nueva interfaz de administración.
 * 
 * @author asdw
 * @version 1.0
 */
public class VentanaSelector extends JFrame {
    
    private static final Color BACKGROUND_COLOR = new Color(240, 242, 247);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(72, 126, 176);
    private static final Color TEXT_COLOR = new Color(52, 58, 76);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    
    // Dependencias del sistema
    private final IModeloService modeloService;
    private final ICommandInvoker commandInvoker;
    private final GestorDisponibilidad gestorDisponibilidad;
    
    private static final java.util.logging.Logger logger = 
        java.util.logging.Logger.getLogger(VentanaSelector.class.getName());

    /**
     * Constructor con inyección de dependencias.
     */
    public VentanaSelector(IModeloService modeloService, ICommandInvoker commandInvoker, 
                          GestorDisponibilidad gestorDisponibilidad) {
        this.modeloService = modeloService;
        this.commandInvoker = commandInvoker;
        this.gestorDisponibilidad = gestorDisponibilidad;
        
        initComponents();
        setupUI();
    }
    
    /**
     * Inicializa los componentes de la interfaz.
     */
    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Sistema de Gestión Hotelera - Seleccionar Interfaz");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Configurar layout principal
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = createHeader();
        add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = createContent();
        add(contentPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = createFooter();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Crea el panel del header.
     */
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(ACCENT_COLOR);
        header.setPreferredSize(new Dimension(600, 80));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("🏨 Sistema de Gestión Hotelera", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        header.setLayout(new BorderLayout());
        header.add(titleLabel, BorderLayout.CENTER);
        
        return header;
    }
    
    /**
     * Crea el panel de contenido con las opciones.
     */
    private JPanel createContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Tarjeta interfaz tradicional
        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel tradicionCard = createInterfaceCard(
            "Interfaz Tradicional",
            "Vista clásica con formularios\ny tablas estándar",
            "📋",
            () -> abrirInterfazTradicional()
        );
        content.add(tradicionCard, gbc);
        
        // Tarjeta nueva interfaz admin
        gbc.gridx = 1;
        gbc.gridy = 0;
        JPanel adminCard = createInterfaceCard(
            "Panel de Administración",
            "Dashboard moderno con\nanálisis y navegación avanzada",
            "📊",
            () -> abrirInterfazAdmin()
        );
        content.add(adminCard, gbc);
        
        return content;
    }
    
    /**
     * Crea una tarjeta de selección de interfaz.
     */
    private JPanel createInterfaceCard(String title, String description, String icon, Runnable action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(30, 20, 30, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Icono
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Título
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Descripción
        JLabel descLabel = new JLabel("<html><center>" + description.replace("\\n", "<br>") + "</center></html>", SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_COLOR);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Botón
        JButton selectButton = new JButton("Seleccionar");
        selectButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectButton.setBackground(ACCENT_COLOR);
        selectButton.setForeground(Color.WHITE);
        selectButton.setBorderPainted(false);
        selectButton.setFocusPainted(false);
        selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectButton.setMaximumSize(new Dimension(120, 35));
        selectButton.addActionListener(e -> action.run());
        
        // Efectos hover
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(248, 250, 252));
                selectButton.setBackground(SUCCESS_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(CARD_COLOR);
                selectButton.setBackground(ACCENT_COLOR);
            }
        });
        
        // Click en toda la tarjeta
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                action.run();
            }
        });
        
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(descLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(selectButton);
        
        return card;
    }
    
    /**
     * Crea el panel del footer.
     */
    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BACKGROUND_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        JLabel infoLabel = new JLabel("Arquitectura: MVC + SOLID | Patrones: Command, Memento, Builder, Singleton");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(120, 120, 120));
        
        footer.add(infoLabel);
        return footer;
    }
    
    /**
     * Configura el UI con Look and Feel moderno.
     */
    private void setupUI() {
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
     * Abre la interfaz tradicional.
     */
    private void abrirInterfazTradicional() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Crear la vista tradicional
                VentanaPrincipal vista = new VentanaPrincipal();
                
                // Crear el controlador con inyección de dependencias
                new ControladorVentanaPrincipal(vista, modeloService, commandInvoker, gestorDisponibilidad);
                
                // Mostrar la vista
                vista.setVisible(true);
                
                // Cerrar ventana selector
                this.dispose();
                
                logger.info("Interfaz tradicional iniciada exitosamente");
                
            } catch (Exception e) {
                logger.severe("Error iniciando interfaz tradicional: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Error iniciando interfaz tradicional: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Abre la nueva interfaz de administración.
     */
    private void abrirInterfazAdmin() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Crear la vista de administración
                VentanaAdmin vistaAdmin = new VentanaAdmin();
                
                // Crear el controlador con inyección de dependencias
                ControladorVentanaAdmin controladorAdmin = new ControladorVentanaAdmin(
                    vistaAdmin, modeloService, commandInvoker, gestorDisponibilidad);
                
                // Conectar vista y controlador
                vistaAdmin.setControlador(controladorAdmin);
                
                // Mostrar la vista
                vistaAdmin.setVisible(true);
                
                // Cerrar ventana selector
                this.dispose();
                
                logger.info("Panel de administración iniciado exitosamente");
                
            } catch (Exception e) {
                logger.severe("Error iniciando panel de administración: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Error iniciando panel de administración: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
