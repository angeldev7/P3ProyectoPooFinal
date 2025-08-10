package memento;

/**
 * Interfaz para el Caretaker del patrón Memento.
 * Define las operaciones para guardar y restaurar estados del modelo.
 * 
 * @author asdw
 * @version 1.0
 */
public interface ICaretaker {
    
    /**
     * Guarda el estado actual del modelo.
     * 
     * @return Memento con el estado guardado
     */
    ModeloMemento saveState();
    
    /**
     * Restaura un estado previo del modelo.
     * 
     * @param memento Memento con el estado a restaurar
     */
    void restoreState(ModeloMemento memento);
    
    /**
     * Verifica si se puede deshacer (hay estados guardados).
     * 
     * @return true si hay estados para restaurar
     */
    boolean canUndo();
    
    /**
     * Verifica si se puede rehacer (hay estados en la pila de redo).
     * 
     * @return true si hay estados para rehacer
     */
    boolean canRedo();
    
    /**
     * Obtiene el último estado guardado para deshacer.
     * 
     * @return Memento del estado a restaurar
     */
    ModeloMemento undo();
    
    /**
     * Obtiene el último estado deshecho para rehacer.
     * 
     * @return Memento del estado a restaurar
     */
    ModeloMemento redo();
}
