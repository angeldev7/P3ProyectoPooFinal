package singleton;

import org.junit.jupiter.api.Test;
import model.Habitacion;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class GestorDisponibilidadAdditionalTest {

    @Test
    void reservarYLuegoFallaSiYaOcupadaYLiberarFunciona(){
        GestorDisponibilidad gestor = GestorDisponibilidad.getInstance();
        gestor.reset();
        Habitacion h1 = new Habitacion("H1","101","Simple", false, 50);
        Habitacion h2 = new Habitacion("H2","102","Simple", false, 60);
        gestor.inicializar(List.of(h1,h2));
        assertTrue(gestor.isHabitacionDisponible("H1"));
        assertTrue(gestor.reservarHabitacion("H1"));
        assertFalse(gestor.reservarHabitacion("H1")); // ya ocupada -> false
        assertEquals(1, gestor.getNumeroHabitacionesDisponibles());
        gestor.liberarHabitacion("H1");
        assertEquals(2, gestor.getNumeroHabitacionesDisponibles());
        assertEquals(2, gestor.getHabitacionesDisponibles().size());
        assertEquals(0, gestor.getHabitacionesOcupadas().size());
    }
}
