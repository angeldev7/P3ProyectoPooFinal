package command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommandInvokerUtilityTest {

    @Test
    void clearHistoryYLlamadasDescripcionVacias(){
        CommandInvoker invoker = new CommandInvoker();
        assertNull(invoker.getNextUndoDescription());
        invoker.clearHistory(); // no error
        assertFalse(invoker.canUndo());
        assertFalse(invoker.canRedo());
    }
}
