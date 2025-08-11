package memento;

import org.junit.jupiter.api.*;
import support.FakeModeloService;
import model.*;
import command.*;
import static org.junit.jupiter.api.Assertions.*;

public class MementoIntegrationTest {
    @Test
    void multiplesComandosUndoSecuencial(){
        FakeModeloService service = new FakeModeloService();
        Cliente c = new Cliente(null, "Eva","Diaz","444","903");
        service.registrarCliente(c);
        String habId = service.obtenerHabitacionesDisponibles().get(0).getId();
        CommandInvoker invoker = new CommandInvoker(service);
        invoker.executeCommand(new CrearReservaCommand(service, c.getId(), habId, 80.0, "R1"));
        invoker.executeCommand(new CrearReservaCommand(service, c.getId(), habId, 80.0, "R2")); // segunda reserva mismo cliente (puede ocupar misma hab si liberada, test sencillo)
        assertEquals(2, service.obtenerTodasReservas().size());
        invoker.undo();
        assertEquals(1, service.obtenerTodasReservas().size());
        invoker.undo();
        assertEquals(0, service.obtenerTodasReservas().size());
    }
}
