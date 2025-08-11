package command;

import org.junit.jupiter.api.Test;
import support.FakeModeloService;
import model.*;
import static org.junit.jupiter.api.Assertions.*;

public class CommandInvokerRedoSnapshotsTest {

    @Test
    void redoRestauraEstadosMultiplesSnapshots(){
        FakeModeloService service = new FakeModeloService();
        Cliente c = new Cliente(null,"AA","BB","CED-AA","TEL");
        service.registrarCliente(c);
        String hab1 = service.obtenerHabitacionesDisponibles().get(0).getId();
        String hab2 = service.obtenerHabitacionesDisponibles().get(1).getId();
        CommandInvoker invoker = new CommandInvoker(service);
        invoker.executeCommand(new CrearReservaCommand(service, c.getId(), hab1, 10.0, "R1"));
        invoker.executeCommand(new CrearReservaCommand(service, c.getId(), hab2, 12.0, "R2"));
        assertEquals(2, service.obtenerTodasReservas().size());
        // Undo dos veces
        invoker.undo();
        invoker.undo();
        assertEquals(0, service.obtenerTodasReservas().size());
        // Redo dos veces
        invoker.redo();
        assertEquals(1, service.obtenerTodasReservas().size());
        invoker.redo();
        assertEquals(2, service.obtenerTodasReservas().size());
    }
}
