package builder;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReservaBuilderEdgeTest {

    @Test
    void setTemporalAfectaConfirmadaYObservaciones(){
        ReservaBuilder b = new ReservaBuilder()
                .setCliente("CLI-X")
                .setHabitacion("HAB-X")
                .setTotal(10)
                .setTemporal();
        model.Reserva r = b.build();
        assertFalse(r.isConfirmada());
        assertTrue(r.getObservaciones().contains("temporal"));
    }

    @Test
    void totalNegativoLanzaExcepcion(){
        ReservaBuilder b = new ReservaBuilder()
                .setCliente("CLI-Y")
                .setHabitacion("HAB-Y")
                .setTotal(-5);
        assertThrows(IllegalStateException.class, b::build);
    }

    @Test
    void chainingCompletoConFechaIngresoAhoraYObservaciones(){
        ReservaBuilder b = new ReservaBuilder()
                .setCliente("CLI-Z")
                .setHabitacion("HAB-Z")
                .setObservaciones("Notas")
                .setTotal(99.9)
                .setConfirmada(true)
                .setFechaIngresoAhora();
        model.Reserva r = b.build();
        assertEquals("CLI-Z", r.getIdCliente());
        assertEquals("HAB-Z", r.getIdHabitacion());
        assertTrue(r.isConfirmada());
        assertEquals(99.9, r.getTotal());
    }
}
