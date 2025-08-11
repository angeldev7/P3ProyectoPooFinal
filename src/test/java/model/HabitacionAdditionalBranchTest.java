package model;

import org.junit.jupiter.api.Test;
import org.bson.Document;
import static org.junit.jupiter.api.Assertions.*;

public class HabitacionAdditionalBranchTest {

    @Test
    void fromDocumentNullLanzaNPE(){
        assertThrows(NullPointerException.class, ()-> Habitacion.fromDocument(null));
    }

    @Test
    void getDisplayTextFormato(){
        Habitacion h = new Habitacion("H-10","010","Suite", false, 99.5);
        String disp = h.getDisplayText();
        assertTrue(disp.contains("010"));
        assertTrue(disp.contains("Suite"));
        assertTrue(disp.contains("99.50"));
    }
}
