package command;

/**
 * Interfaz base para implementar el patrón Command.
 * Define las operaciones básicas para comandos que soportan Undo/Redo.
 * 
 * @author asdw
 * @version 1.0
 */
public interface ICommand {
    
    /**
     * Ejecuta el comando.
     */
    void execute();
    
    /**
     * Deshace la operación del comando.
     */
    void undo();
    
    /**
     * Obtiene una descripción del comando para mostrar al usuario.
     * 
     * @return Descripción legible del comando
     */
    String getDescription();
    
    /**
     * Verifica si el comando puede ser deshecho.
     * 
     * @return true si puede ejecutar undo(), false en caso contrario
     */
    default boolean canUndo() {
        return true;
    }
    
    /**
     * Obtiene el timestamp de cuando se ejecutó el comando.
     * 
     * @return Tiempo de ejecución en milisegundos
     */
    long getExecutionTime();
}
