package model;

import org.junit.jupiter.api.*;
import support.FakeModeloService;
import static org.junit.jupiter.api.Assertions.*;

public class IdGenerationTest {
    @Test
    void registrarClientesGeneraIdsLegiblesSecuenciales(){
        FakeModeloService service = new FakeModeloService();
        Cliente c1 = new Cliente(null, "A","B","111","900");
        Cliente c2 = new Cliente(null, "C","D","222","901");
        assertTrue(service.registrarCliente(c1));
        assertTrue(service.registrarCliente(c2));
        assertTrue(c1.getId().startsWith("CLI-"));
        assertTrue(c2.getId().startsWith("CLI-"));
        assertNotEquals(c1.getId(), c2.getId());
    }

    @Test
    void crearReservaGeneraIdLegibleYMarcaHabitacion(){
        FakeModeloService service = new FakeModeloService();
        Cliente c = new Cliente(null, "A","B","333","902");
        service.registrarCliente(c);
        Habitacion hab = service.obtenerHabitacionesDisponibles().get(0);
        Reserva r = new Reserva(null, c.getId(), hab.getId(), new java.util.Date(), null, 50.0);
        assertTrue(service.crearReserva(r));
        assertTrue(r.getId().startsWith("RES-"));
        assertTrue(service.obtenerHabitacionesOcupadas().stream().anyMatch(h->h.getId().equals(hab.getId())));
    }
}
