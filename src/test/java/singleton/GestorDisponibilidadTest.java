package singleton;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import model.*;
import java.util.*;

public class GestorDisponibilidadTest {

    @BeforeEach
    void limpiarEstado() {
        // Asegura estado limpio del singleton entre tests
        GestorDisponibilidad.getInstance().reset();
    }

    @Test
    void inicializarContadoresYSingletonIdentity() {
        GestorDisponibilidad gestorA = GestorDisponibilidad.getInstance();
        GestorDisponibilidad gestorB = GestorDisponibilidad.getInstance();
        assertSame(gestorA, gestorB, "Debe devolver siempre la misma instancia");

        List<Habitacion> habs = List.of(
                new Habitacion("HAB-010", "010", "Suite", false, 120),
                new Habitacion("HAB-011", "011", "Simple", false, 50)
        );
        gestorA.inicializar(habs);

        assertEquals(2, gestorA.getNumeroTotalHabitaciones());
        assertEquals(2, gestorA.getNumeroHabitacionesDisponibles());
        assertEquals(2, gestorA.getHabitacionesDisponibles().size());
    }

    @Test
    void reservarYLiberarPropagaEstado() {
        GestorDisponibilidad gestor = GestorDisponibilidad.getInstance();
        gestor.inicializar(List.of(
                new Habitacion("HAB-020", "020", "Doble", false, 80)
        ));

        assertTrue(gestor.isHabitacionDisponible("HAB-020"));
        assertTrue(gestor.reservarHabitacion("HAB-020"), "Debe poder reservar si estaba disponible");
        assertFalse(gestor.isHabitacionDisponible("HAB-020"));
        Habitacion h = gestor.getHabitacion("HAB-020");
        assertNotNull(h);
        assertTrue(h.isOcupada(), "El estado de la entidad debe reflejar la reserva");

        gestor.liberarHabitacion("HAB-020");
        assertTrue(gestor.isHabitacionDisponible("HAB-020"));
        assertFalse(h.isOcupada(), "Al liberar debe reflejarse en la entidad");
        assertEquals(1, gestor.getNumeroHabitacionesDisponibles());
    }

    @Test
    void reservarHabitacionNoDisponibleDevuelveFalse() {
        GestorDisponibilidad gestor = GestorDisponibilidad.getInstance();
        gestor.inicializar(List.of(
                new Habitacion("HAB-030", "030", "Simple", true, 40) // ocupada => no disponible
        ));

        assertFalse(gestor.isHabitacionDisponible("HAB-030"));
        assertFalse(gestor.reservarHabitacion("HAB-030"), "No debe permitir reservar si ya está ocupada");
        assertEquals(0, gestor.getNumeroHabitacionesDisponibles());
        assertEquals(1, gestor.getNumeroTotalHabitaciones());
        assertEquals(0, gestor.getHabitacionesDisponibles().size());
        assertEquals(1, gestor.getHabitacionesOcupadas().size());
    }
}
