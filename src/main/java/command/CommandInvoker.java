package command;

import java.util.*;

/**
 * Implementación del invoker de comandos con soporte para Undo/Redo.
 * Mantiene una pila de comandos ejecutados y otra de comandos deshechos.
 * 
 * @author asdw
 * @version 1.0
 */
public class CommandInvoker implements ICommandInvoker {
    
    private final Stack<ICommand> undoStack;
    private final Stack<ICommand> redoStack;
    private final List<String> history;
    private final int maxHistorySize;

    /**
     * Constructor que inicializa las pilas de comandos.
     * 
     * @param maxHistorySize Tamaño máximo del historial (0 = ilimitado)
     */
    public CommandInvoker(int maxHistorySize) {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.history = new ArrayList<>();
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Constructor con tamaño de historial por defecto (100).
     */
    public CommandInvoker() {
        this(100);
    }

    @Override
    public void executeCommand(ICommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        
        try {
            command.execute();
            undoStack.push(command);
            redoStack.clear(); // Limpiar redo stack al ejecutar nuevo comando
            
            // Añadir al historial
            history.add(command.getDescription());
            
            // Limitar tamaño del historial si es necesario
            if (maxHistorySize > 0 && undoStack.size() > maxHistorySize) {
                undoStack.remove(0);
                history.remove(0);
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
            command.undo();
            redoStack.push(command);
            return true;
        } catch (Exception e) {
            // Si falla el undo, volver a poner el comando en la pila
            if (!undoStack.isEmpty()) {
                ICommand command = redoStack.pop();
                undoStack.push(command);
            }
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
            command.execute();
            undoStack.push(command);
            return true;
        } catch (Exception e) {
            // Si falla el redo, volver a poner el comando en la pila de redo
            if (!redoStack.isEmpty()) {
                ICommand command = undoStack.pop();
                redoStack.push(command);
            }
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
