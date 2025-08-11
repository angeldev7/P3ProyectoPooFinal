package command;

import org.junit.jupiter.api.Test;
import support.FakeModeloService;
import model.*;
import static org.junit.jupiter.api.Assertions.*;

public class CommandInvokerAdvancedTest {

    @Test
    void excederMaxHistoryEliminaPrimerComando(){
        FakeModeloService service = new FakeModeloService();
        Cliente c = new Cliente(null,"J","K","CED-9","T");
        service.registrarCliente(c);
        String hab = service.obtenerHabitacionesDisponibles().get(0).getId();
        CommandInvoker invoker = new CommandInvoker(2, service); // max 2
        invoker.executeCommand(new CrearReservaCommand(service, c.getId(), hab, 10.0, "R1"));
        invoker.executeCommand(new CrearReservaCommand(service, c.getId(), hab, 11.0, "R2"));
        invoker.executeCommand(new CrearReservaCommand(service, c.getId(), hab, 12.0, "R3")); // expulsa R1
        assertEquals(2, invoker.getUndoStackSize());
        assertFalse(invoker.getCommandHistory().get(0).contains("R1"));
    }

    @Test
    void executeCommandNullLanzaExcepcion(){
        CommandInvoker invoker = new CommandInvoker();
        assertThrows(IllegalArgumentException.class, ()->invoker.executeCommand(null));
    }
}
