package model;

import org.junit.jupiter.api.Test;
import org.bson.Document;
import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {
    @Test
    void toStringFormatoBasico(){
        Cliente c = new Cliente("CLI-0001","Ana","Lopez","1234567","999111222");
        String s = c.toString();
        assertTrue(s.contains("Ana"));
        assertTrue(s.contains("Lopez"));
        assertTrue(s.contains("1234567"));
    }

    @Test
    void nombreCompleto(){
        Cliente c = new Cliente("CLI-0002","Juan","Perez","7654321","111222333");
        assertEquals("Juan Perez", c.getNombreCompleto());
    }

    @Test
    void toDocumentAndFromDocumentRoundtrip(){
        Cliente c = new Cliente("CLI-99","Juan","Perez","123","555");
        Document d = c.toDocument();
        assertEquals("CLI-99", d.getString("_id"));
        Cliente c2 = Cliente.fromDocument(d);
        assertEquals(c.getNombre(), c2.getNombre());
        assertEquals(c.getApellido(), c2.getApellido());
        assertEquals(c.getCedula(), c2.getCedula());
        assertEquals(c.getTelefono(), c2.getTelefono());
    }

    @Test
    void nombreCompletoYToStringConNulos(){
        Cliente c = new Cliente(null, null, "Lopez", "777", null);
        String nc = c.getNombreCompleto();
        assertTrue(nc.endsWith("Lopez"));
        String ts = c.toString();
        assertTrue(ts.contains("Lopez"));
    }
}
