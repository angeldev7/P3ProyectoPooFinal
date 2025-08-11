package command;

import org.junit.jupiter.api.Test;
import support.FakeModeloService;
import model.*;
import static org.junit.jupiter.api.Assertions.*;

public class CheckinRapidoAdminCommandTest {

    @Test
    void checkinRapidoCreaReservaYOcupaHabitacionYUndoLibera(){
        FakeModeloService service = new FakeModeloService();
        // Cliente sin id para forzar rama de registrarCliente dentro del comando
        Cliente cliente = new Cliente(null, "Juan", "Perez", "C-XYZ", "999");
        Habitacion hab = service.obtenerHabitacionesDisponibles().get(0);
        CheckinRapidoAdminCommand cmd = new CheckinRapidoAdminCommand(service, cliente, hab);
        cmd.execute();
        assertEquals(1, service.obtenerTodasReservas().size());
        assertTrue(service.obtenerTodasHabitaciones().get(0).isOcupada());
        // Deshacer
        cmd.undo();
        assertFalse(service.obtenerTodasHabitaciones().get(0).isOcupada(), "Habitación debe quedar libre tras undo");
        assertNotNull(service.obtenerTodasReservas().get(0).getFechaSalida(), "Reserva debe marcar salida");
    }
}
