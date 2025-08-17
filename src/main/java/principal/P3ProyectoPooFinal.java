package principal;

import view.VentanaSelector;
import model.*;
import command.*;
import singleton.GestorDisponibilidad;

/**
 * Clase principal del sistema de gestión hotelera.
 * Arquitectura: MVC reforzado con principios SOLID (DIP aplicado mediante {@link model.IModeloService}).
 * Patrones implementados: Command (operaciones con Undo/Redo), Memento (snapshots completos del modelo),
 * Builder (creación segura de reservas), Singleton (gestión de disponibilidad), y extensión para Servicios a Habitación.
 * 
 * Responsabilidades clave:
 * - Inicializar capa de modelo y gestor de disponibilidad.
 * - Configurar invoker de comandos con soporte de snapshots (Memento) para operaciones atómicas con Undo/Redo.
 * - Lanzar interfaz de selección / administración moderna.
 * 
 * Historial de versiones:
 * 1.0.0: Estructura base MVC, CRUD básico de Clientes / Habitaciones / Reservas, patrón Builder inicial.
 * 2.0.0: Interfaz de administración moderna (dashboard con métricas), integración completa de Command + Memento,
 *        mejoras de ID legibles (CLI-xxxx, RES-xxxx), métricas de reservas recientes.
 * 3.0.0: Panel de Servicios a la Habitación (solo para habitaciones ocupadas o reservas activas) con nueva entidad
 *        {@link model.ServicioHabitacion}, validaciones de negocio (reserva activa + habitación ocupada),
 *        ampliación de la interfaz `IModeloService`, y preparación para futura integración de costos dinámicos.
 * 
 * Mejores prácticas aplicadas:
 * - DIP: la UI y controladores solo conocen interfaces (ej. {@link command.ICommandInvoker}, {@link model.IModeloService}).
 * - Cohesión alta: lógica de persistencia encapsulada en {@link model.ModeloServiceImpl}.
 * - Separación de responsabilidades: nuevos paneles modulares (Clientes, Reservas, Habitaciones, Servicios, Reportes).
 * - Extensibilidad: nuevos comandos pueden agregarse sin modificar el invoker.
 * 
 * Nota: Cualquier error crítico durante el arranque se notifica al usuario y se registra antes de terminar el proceso.
 * 
 * @version 3.0.0
 */
public class P3ProyectoPooFinal {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Inicializar componentes con inyección de dependencias
                // Crear servicio del modelo (implementación de DIP)
                IModeloService modeloService = new ModeloServiceImpl();
                
                // Crear el command invoker para Undo/Redo
                ICommandInvoker commandInvoker = new CommandInvoker(modeloService);
                
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
                    .severe("Error critico iniciando sistema: " + e.getMessage());
                
                System.exit(1);
            }
        });
    }
}
