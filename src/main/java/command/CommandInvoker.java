package command;

import java.util.*;
import model.IModeloService;
import memento.ModeloMemento;

/**
 * Implementación del invoker de comandos con soporte para Undo/Redo.
 * Mantiene una pila de comandos ejecutados y otra de comandos deshechos.
 * 
 * @author asdw
 * @version 1.0
 */
public class CommandInvoker implements ICommandInvoker {

    // Snapshot de comandos para integrar Memento
    private static class CommandSnapshot {
        final ModeloMemento preState;
        final ModeloMemento postState;
        CommandSnapshot(ModeloMemento pre, ModeloMemento post){
            this.preState = pre; this.postState = post; }
    }

    private final Deque<CommandSnapshot> undoSnapshots = new ArrayDeque<>();
    private final Deque<CommandSnapshot> redoSnapshots = new ArrayDeque<>();
    private final Stack<ICommand> undoStack; // histórico clásico (para descripción)
    private final Stack<ICommand> redoStack; // histórico clásico (para descripción)
    private final List<String> history;
    private final int maxHistorySize;
    private final IModeloService modeloService; // Puede ser null si no se desea Memento

    /**
     * Constructor que inicializa las pilas de comandos.
     * 
     * @param maxHistorySize Tamaño máximo del historial (0 = ilimitado)
     */
    public CommandInvoker(int maxHistorySize, IModeloService modeloService) {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.history = new ArrayList<>();
        this.maxHistorySize = maxHistorySize;
        this.modeloService = modeloService;
    }

    public CommandInvoker(int maxHistorySize){
        this(maxHistorySize, null);
    }

    /**
     * Constructor con tamaño de historial por defecto (100).
     */
    public CommandInvoker(){this(100, null);}    

    public CommandInvoker(IModeloService modeloService){this(100, modeloService);}    

    @Override
    public void executeCommand(ICommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        
        try {
            ModeloMemento pre = (modeloService!=null)? modeloService.crearMemento(): null;
            command.execute();
            ModeloMemento post = (modeloService!=null)? modeloService.crearMemento(): null;
            undoStack.push(command);
            redoStack.clear();
            if (modeloService!=null){
                undoSnapshots.push(new CommandSnapshot(pre, post));
                redoSnapshots.clear();
            }
            history.add(command.getDescription());
            if (maxHistorySize > 0 && undoStack.size() > maxHistorySize) {
                undoStack.remove(0);
                history.remove(0);
                if (modeloService!=null && !undoSnapshots.isEmpty()) undoSnapshots.removeLast();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al ejecutar comando: " + command.getDescription(), e);
        }
    }

    @Override
    public boolean undo() {
        if (!canUndo()) {
            return false;
        }
        
        try {
            ICommand command = undoStack.pop();
            redoStack.push(command);
            if (modeloService!=null && !undoSnapshots.isEmpty()) {
                CommandSnapshot snap = undoSnapshots.pop();
                // Guardar estado actual para posibilitar redo
                redoSnapshots.push(snap);
                modeloService.restaurarEstadoCompleto(snap.preState);
            } else {
                // Fallback a undo lógico del comando
                command.undo();
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al deshacer comando", e);
        }
    }

    @Override
    public boolean redo() {
        if (!canRedo()) {
            return false;
        }
        
        try {
            ICommand command = redoStack.pop();
            undoStack.push(command);
            if (modeloService!=null && !redoSnapshots.isEmpty()) {
                CommandSnapshot snap = redoSnapshots.pop();
                // Restaurar estado post comando
                modeloService.restaurarEstadoCompleto(snap.postState);
                undoSnapshots.push(snap); // vuelve a la pila de undo
            } else {
                // Reejecutar comando si no hay snapshot
                command.execute();
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error al rehacer comando", e);
        }
    }

    @Override
    public boolean canUndo() {
    return !undoStack.isEmpty() && undoStack.peek().canUndo();
    }

    @Override
    public boolean canRedo() {
    return !redoStack.isEmpty();
    }

    @Override
    public List<String> getCommandHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        history.clear();
    }

    @Override
    public String getNextUndoDescription() {
        if (canUndo()) {
            return undoStack.peek().getDescription();
        }
        return null;
    }

    @Override
    public String getNextRedoDescription() {
        if (canRedo()) {
            return redoStack.peek().getDescription();
        }
        return null;
    }

    /**
     * Obtiene el número de comandos en la pila de undo.
     * 
     * @return Cantidad de comandos que pueden deshacerse
     */
    public int getUndoStackSize() {
        return undoStack.size();
    }

    /**
     * Obtiene el número de comandos en la pila de redo.
     * 
     * @return Cantidad de comandos que pueden rehacerse
     */
    public int getRedoStackSize() {
        return redoStack.size();
    }
}
