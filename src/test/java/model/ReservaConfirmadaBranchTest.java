package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

public class ReservaConfirmadaBranchTest {
    @Test
    void reservaNoActivaSiNoConfirmadaAunqueSinFechaSalida(){
        Reserva r = new Reserva("RID","CID","HID", new Date(), null, 20.0);
        r.setConfirmada(false);
        assertFalse(r.isActiva());
    }
}
