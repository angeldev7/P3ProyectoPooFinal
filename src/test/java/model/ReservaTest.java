package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

public class ReservaTest {
    @Test
    void reservaActivaSinSalida(){
        Date ahora = new Date();
        Reserva r = new Reserva("RES-0001","CLI-0001","HAB-001",ahora,null,100.0);
        assertTrue(r.isActiva());
    }

    @Test
    void reservaNoActivaTrasFinalizar(){
        Date ahora = new Date();
        Reserva r = new Reserva("RES-0002","CLI-0001","HAB-001",ahora,null,100.0);
        r.finalizar();
        assertFalse(r.isActiva());
        assertNotNull(r.getFechaSalida());
    }
}
