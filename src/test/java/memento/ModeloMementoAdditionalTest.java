package memento;

import org.junit.jupiter.api.Test;
import model.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ModeloMementoAdditionalTest {

    @Test
    void mementoEmptyIsEmptyTrue(){
        ModeloMemento m = new ModeloMemento(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(m.isEmpty());
        assertTrue(m.getDescription().contains("0 clientes"));
    }

    @Test
    void deepCopyIndependiente(){
        Cliente c = new Cliente("C1","N","A","CED","TEL");
        Habitacion h = new Habitacion("H1","101","Simple", false, 50);
        Reserva r = new Reserva("R1","C1","H1", new Date(), null, 10.0);
        ModeloMemento m = new ModeloMemento(List.of(c), List.of(h), List.of(r));
        // Obtener copias y mutarlas
        m.getClientes().get(0).setNombre("CAMBIADO");
        m.getHabitaciones().get(0).setOcupada(true);
        m.getReservas().get(0).finalizar();
        // Volver a pedir para comprobar que original no se alteró
        assertEquals("N", m.getClientes().get(0).getNombre());
        assertFalse(m.getHabitaciones().get(0).isOcupada());
        assertNull(m.getReservas().get(0).getFechaSalida());
    }
}
