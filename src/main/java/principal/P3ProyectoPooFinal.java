package principal;

import controller.ControladorVentanaPrincipal;
import view.VentanaPrincipal;
import view.VentanaSelector;
import model.*;
import command.*;
import singleton.GestorDisponibilidad;

/**
 * Clase principal del sistema de gestión hotelera.
 * Implementa arquitectura MVC con patrones Command, Memento, Builder y Singleton.
 * Aplica principio DIP inyectando abstracciones en lugar de implementaciones concretas.
 * 
 * Versión 2.0 - Incluye nueva interfaz de administración moderna.
 * 
 * @version 2.0
 */
public class P3ProyectoPooFinal {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Inicializar componentes con inyección de dependencias
                // Crear servicio del modelo (implementación de DIP)
                IModeloService modeloService = new ModeloServiceImpl();
                
                // Crear el command invoker para Undo/Redo
                ICommandInvoker commandInvoker = new CommandInvoker();
                
                // Inicializar el gestor de disponibilidad (Singleton)
                GestorDisponibilidad gestor = GestorDisponibilidad.getInstance();
                
                // Mostrar ventana de selección de interfaz
                VentanaSelector selector = new VentanaSelector(modeloService, commandInvoker, gestor);
                selector.setVisible(true);
                
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "Error iniciando el sistema: " + e.getMessage(),
                    "Error Crítico", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                
                java.util.logging.Logger.getLogger(P3ProyectoPooFinal.class.getName())
                    .severe("Error crítico iniciando sistema: " + e.getMessage());
                
                System.exit(1);
            }
        });
    }
}
