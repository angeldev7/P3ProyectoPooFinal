package command;

import org.junit.jupiter.api.*;
import support.FakeModeloService;
import model.*;
import static org.junit.jupiter.api.Assertions.*;

public class CommandInvokerFallbackTest {

    @Test
    void undoUsandoUndoLogicoCuandoSinMemento(){
        FakeModeloService service = new FakeModeloService();
        Cliente c = new Cliente(null, "Z","K","555","904");
        service.registrarCliente(c);
        String hab = service.obtenerHabitacionesDisponibles().get(0).getId();
        // Invoker sin modelo -> sin snapshots
        CommandInvoker invoker = new CommandInvoker();
        ICommand crear = new CrearReservaCommand(service, c.getId(), hab, 40.0, "Fx");
        invoker.executeCommand(crear);
        assertEquals(1, service.obtenerTodasReservas().size());
        assertTrue(invoker.canUndo());
        invoker.undo();
        // Como undo lógico finaliza reserva (fechaSalida != null)
        assertEquals(1, service.obtenerTodasReservas().size());
        assertNotNull(service.obtenerTodasReservas().get(0).getFechaSalida());
        assertFalse(invoker.canUndo(), "Ya no hay comandos con canUndo true tras undo lógico");
    }

    @Test
    void redoSinHistorialDevuelveFalse(){
        CommandInvoker invoker = new CommandInvoker();
        assertFalse(invoker.redo());
    }
}
