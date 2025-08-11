package model;

import org.junit.jupiter.api.Test;
import org.bson.Document;
import static org.junit.jupiter.api.Assertions.*;

public class ReservaFromDocumentDefaultsTest {

    @Test
    void fromDocumentCamposFaltantesUsaDefaults(){
        Document d = new Document()
                .append("_id","RID")
                .append("idCliente","CID")
                .append("idHabitacion","HID")
                // sin total, observaciones ni confirmada
                .append("fechaIngreso", new java.util.Date());
        Reserva r = Reserva.fromDocument(d);
        assertEquals(0.0, r.getTotal());
        assertEquals("", r.getObservaciones());
        assertTrue(r.isConfirmada());
    }
}
