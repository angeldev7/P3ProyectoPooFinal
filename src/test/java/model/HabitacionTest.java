package model;

import org.junit.jupiter.api.Test;
import org.bson.Document;
import static org.junit.jupiter.api.Assertions.*;

public class HabitacionTest {

    @Test
    void toDocumentFromDocumentRoundtrip(){
        Habitacion h = new Habitacion("HAB-XYZ","007","Suite",false,150.0);
        Document d = h.toDocument();
        assertEquals("HAB-XYZ", d.getString("_id"));
        Habitacion h2 = Habitacion.fromDocument(d);
        assertEquals(h.getNumero(), h2.getNumero());
        assertTrue(h2.isDisponible());
    }

    @Test
    void isDisponibleReflejaOcupada(){
        Habitacion h = new Habitacion("HAB-1","001","Simple",true,50.0);
        assertFalse(h.isDisponible());
        h.setOcupada(false);
        assertTrue(h.isDisponible());
    }

    @Test
    void toStringIncluyeNumeroYTipo(){
        Habitacion h = new Habitacion("HAB-2","002","Doble",false,80.0);
        String s = h.toString();
        assertTrue(s.contains("002"));
        assertTrue(s.contains("Doble"));
    }
}
