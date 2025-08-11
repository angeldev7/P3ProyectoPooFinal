package model;

import org.junit.jupiter.api.Test;
import org.bson.Document;
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

    @Test
    void toDocumentFromDocumentRoundtrip(){
        Date ingreso = new Date();
        Reserva r = new Reserva("RES-X","CLI-1","HAB-1", ingreso, null, 123.45);
        r.setObservaciones("Obs");
        r.setConfirmada(true);
        Document d = r.toDocument();
        assertEquals("RES-X", d.getString("_id"));
        Reserva copia = Reserva.fromDocument(d);
        assertEquals(r.getIdCliente(), copia.getIdCliente());
        assertEquals(r.getIdHabitacion(), copia.getIdHabitacion());
        assertEquals(r.getTotal(), copia.getTotal());
        assertTrue(copia.isConfirmada());
        assertTrue(copia.isActiva());
    }

    @Test
    void finalizarMarcaFechaSalidaYDesactivaActiva(){
        Reserva r = new Reserva("RES-Y","CLI-2","HAB-2", new Date(), null, 50.0);
        assertTrue(r.isActiva());
        r.finalizar();
        assertNotNull(r.getFechaSalida());
        assertFalse(r.isActiva());
    }

    @Test
    void reservaConFechaSalidaNoActiva(){
        Date ingreso = new Date();
        Date salida = new Date(ingreso.getTime()+3600000);
        Reserva r = new Reserva("RES-Z","CLI-3","HAB-3", ingreso, salida, 10.0);
        assertFalse(r.isActiva());
    }
}
