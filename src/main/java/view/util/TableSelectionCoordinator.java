package view.util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Coordina la selección exclusiva entre varias JTable.
 * Cuando una tabla gana selección, se limpia en las demás.
 * Permite callback para actualizar botones u otros componentes.
 */
public class TableSelectionCoordinator {
    private final List<JTable> tablas;
    private final Consumer<JTable> onSelectionChanged;
    private boolean internalChange = false;

    public TableSelectionCoordinator(Consumer<JTable> onSelectionChanged, JTable... tablas) {
        this.tablas = Arrays.asList(tablas);
        this.onSelectionChanged = onSelectionChanged != null ? onSelectionChanged : t -> {};
        instalar();
    }

    private void instalar() {
        ListSelectionListener listener = new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() || internalChange) return;
                JTable source = null;
                for (JTable t: tablas) {
                    if (t.getSelectionModel() == e.getSource()) { source = t; break; }
                }
                if (source == null) return;
                if (source.getSelectedRow() != -1) {
                    internalChange = true;
                    for (JTable t: tablas) {
                        if (t != source) t.clearSelection();
                    }
                    internalChange = false;
                }
                onSelectionChanged.accept(source);
            }
        };

        for (JTable t: tablas) {
            t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            t.getSelectionModel().addListSelectionListener(listener);
        }
    }

    /**
     * Obtiene la tabla actualmente con selección (o null).
     */
    public JTable getTablaActiva() {
        for (JTable t: tablas) if (t.getSelectedRow() != -1) return t;
        return null;
    }
}
