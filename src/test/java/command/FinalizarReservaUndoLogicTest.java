package command;

import org.junit.jupiter.api.*;
import support.FakeModeloService;
import model.*;
import static org.junit.jupiter.api.Assertions.*;

public class FinalizarReservaUndoLogicTest {
    @Test
    void undoReactivaReservaConLogicaInterna(){
        FakeModeloService service = new FakeModeloService();
        Cliente c = new Cliente(null, "R","S","666","905");
        service.registrarCliente(c);
        Habitacion hab = service.obtenerHabitacionesDisponibles().get(0);
        CommandInvoker invoker = new CommandInvoker(); // sin snapshots
        ICommand crear = new CrearReservaCommand(service, c.getId(), hab.getId(), 55.0, "Rz");
        invoker.executeCommand(crear);
        Reserva r = service.obtenerTodasReservas().get(0);
        ICommand fin = new FinalizarReservaCommand(service, r.getId());
        invoker.executeCommand(fin);
        assertNotNull(service.obtenerTodasReservas().get(0).getFechaSalida());
        invoker.undo(); // reactivar (crea nueva reserva activa)
        long activas = service.obtenerTodasReservas().stream().filter(x->x.getFechaSalida()==null).count();
        assertTrue(activas >= 1);
    }
}
