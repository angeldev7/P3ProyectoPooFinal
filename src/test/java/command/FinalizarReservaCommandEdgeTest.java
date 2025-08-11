package command;

import org.junit.jupiter.api.Test;
import support.FakeModeloService;
import model.*;
import static org.junit.jupiter.api.Assertions.*;

public class FinalizarReservaCommandEdgeTest {

    @Test
    void undoNoDuplicaSiReservaYaActiva(){
        FakeModeloService service = new FakeModeloService();
        Cliente c = new Cliente(null,"A","B","CED-1","TEL");
        service.registrarCliente(c);
        Habitacion h = service.obtenerHabitacionesDisponibles().get(0);
        // Crear reserva activa
        Reserva r = new Reserva(null,c.getId(),h.getId(), new java.util.Date(), null, 30.0);
        service.crearReserva(r);
        FinalizarReservaCommand fin = new FinalizarReservaCommand(service, r.getId());
        fin.execute();
        assertNotNull(service.obtenerTodasReservas().get(0).getFechaSalida());
        // Crear otra reserva para el mismo cliente (activa) manualmente
        Reserva r2 = new Reserva(null,c.getId(),h.getId(), new java.util.Date(), null, 40.0);
        service.crearReserva(r2);
        long activasAntes = service.obtenerTodasReservas().stream().filter(x->x.getFechaSalida()==null).count();
        fin.undo(); // No debe crear otra porque reservaOriginal ahora ya no está activa
        long activasDespues = service.obtenerTodasReservas().stream().filter(x->x.getFechaSalida()==null).count();
        assertEquals(activasAntes, activasDespues);
    }
}
