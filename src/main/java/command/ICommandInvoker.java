package command;

import java.util.List;

/**
 * Interfaz para el invoker de comandos que maneja la pila de Undo/Redo.
 * 
 * @author asdw
 * @version 1.0
 */
public interface ICommandInvoker {
    
    /**
     * Ejecuta un comando y lo añade al historial.
     * 
     * @param command Comando a ejecutar
     */
    void executeCommand(ICommand command);
    
    /**
     * Deshace el último comando ejecutado.
     * 
     * @return true si se pudo deshacer, false en caso contrario
     */
    boolean undo();
    
    /**
     * Rehace el último comando deshecho.
     * 
     * @return true si se pudo rehacer, false en caso contrario
     */
    boolean redo();
    
    /**
     * Verifica si se puede deshacer algún comando.
     * 
     * @return true si hay comandos para deshacer
     */
    boolean canUndo();
    
    /**
     * Verifica si se puede rehacer algún comando.
     * 
     * @return true si hay comandos para rehacer
     */
    boolean canRedo();
    
    /**
     * Obtiene el historial de comandos ejecutados.
     * 
     * @return Lista de descripciones de comandos
     */
    List<String> getCommandHistory();
    
    /**
     * Limpia el historial de comandos.
     */
    void clearHistory();
    
    /**
     * Obtiene la descripción del próximo comando a deshacer.
     * 
     * @return Descripción del comando o null si no hay comandos
     */
    String getNextUndoDescription();
    
    /**
     * Obtiene la descripción del próximo comando a rehacer.
     * 
     * @return Descripción del comando o null si no hay comandos
     */
    String getNextRedoDescription();
}
