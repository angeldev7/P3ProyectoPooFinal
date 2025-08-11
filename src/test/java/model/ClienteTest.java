package model;

import org.junit.jupiter.api.Test;
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
}
