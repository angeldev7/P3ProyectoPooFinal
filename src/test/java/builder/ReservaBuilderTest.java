package builder;

import org.junit.jupiter.api.*;
import model.Reserva;
import static org.junit.jupiter.api.Assertions.*;

public class ReservaBuilderTest {
    @Test
    void buildCheckinInmediatoValidaCampos(){
        Reserva r = new ReservaBuilder()
                .setCliente("CLI-1")
                .setHabitacion("HAB-001")
                .setTotal(150)
                .setCheckinInmediato()
                .build();
        assertNotNull(r.getId());
        assertNotNull(r.getFechaIngreso());
        assertNull(r.getFechaSalida());
        assertEquals(150, r.getTotal());
        assertTrue(r.isConfirmada());
    }

    @Test
    void builderReservaFutura(){
        java.util.Date futura = new java.util.Date(System.currentTimeMillis()+86400000);
        Reserva r = ReservaBuilder.paraReservaFutura("CLI-2","HAB-002", futura, 200.0).build();
        assertEquals(futura, r.getFechaIngreso());
        assertEquals(200.0, r.getTotal());
        assertTrue(r.isConfirmada());
    }

    @Test
    void validarFaltaClienteLanzaExcepcion(){
        ReservaBuilder b = new ReservaBuilder().setHabitacion("HAB-003");
        assertThrows(IllegalStateException.class, b::build);
    }
}
