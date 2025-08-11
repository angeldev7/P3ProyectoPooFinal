package command;

import org.junit.jupiter.api.*;
import support.FakeModeloService;
import model.*;

import static org.junit.jupiter.api.Assertions.*;

public class CommandPatternsTest {
    private FakeModeloService service;
    private CommandInvoker invoker;
    private String clienteId;
    private String habId;

    @BeforeEach
    void setup(){
        service = new FakeModeloService();
        invoker = new CommandInvoker(service); // con memento
        Cliente c = new Cliente(null, "Ana", "Lopez", "12345678", "999111222");
        assertTrue(service.registrarCliente(c));
        clienteId = c.getId();
        habId = service.obtenerHabitacionesDisponibles().get(0).getId();
    }

    @Test
    void crearReservaCreaYOcupaHabitacion(){
        double total = 100.0;
        ICommand cmd = new CrearReservaCommand(service, clienteId, habId, total, "Reserva test");
        invoker.executeCommand(cmd);
        assertEquals(1, service.obtenerTodasReservas().size());
        assertTrue(service.obtenerTodasHabitaciones().stream().filter(h->h.getId().equals(habId)).findFirst().get().isOcupada());
    }

    @Test
    void undoConSnapshotsRestauraEstadoPrevio(){
        ICommand cmd = new CrearReservaCommand(service, clienteId, habId, 50.0, "R1");
        invoker.executeCommand(cmd);
        assertEquals(1, service.obtenerTodasReservas().size());
        assertTrue(invoker.canUndo());
        invoker.undo();
        // Debe revertir reserva y liberar habitación
        assertEquals(0, service.obtenerTodasReservas().size());
        assertFalse(service.obtenerTodasHabitaciones().stream().filter(h->h.getId().equals(habId)).findFirst().get().isOcupada());
    }

    @Test
    void redoRestauraReservaTrasUndo(){
        ICommand cmd = new CrearReservaCommand(service, clienteId, habId, 60.0, "R2");
        invoker.executeCommand(cmd);
        invoker.undo();
        assertTrue(invoker.canRedo());
        invoker.redo();
        assertEquals(1, service.obtenerTodasReservas().size());
        assertTrue(service.obtenerTodasHabitaciones().stream().filter(h->h.getId().equals(habId)).findFirst().get().isOcupada());
    }

    @Test
    void finalizarReservaConUndoViaSnapshot(){
        ICommand crear = new CrearReservaCommand(service, clienteId, habId, 70.0, "R3");
        invoker.executeCommand(crear);
        String reservaId = service.obtenerTodasReservas().get(0).getId();
        ICommand fin = new FinalizarReservaCommand(service, reservaId);
        invoker.executeCommand(fin);
        assertNotNull(service.obtenerTodasReservas().get(0).getFechaSalida());
        invoker.undo(); // undo finalizar -> vuelve a estado anterior (activa)
        assertNull(service.obtenerTodasReservas().get(0).getFechaSalida());
    }
}
