package view.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel MVC para gestión de servicios a la habitación.
 * No contiene lógica de negocio: solo componentes y getters.
 */
public class PanelServicios extends JPanel {
    private final JTable tablaServicios;
    private final JComboBox<String> comboHabitacionesOcupadas; // listado de habitaciones ocupadas
    private final JButton btnAgregarServicio;
    private final JButton btnRefrescar;
    private final JButton btnVolver;

    public PanelServicios(){
        setLayout(new BorderLayout(10,10));
        setBackground(new Color(245,246,250));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        top.setOpaque(false);
        top.add(new JLabel("Habitación:"));
        comboHabitacionesOcupadas = new JComboBox<>();
        comboHabitacionesOcupadas.setPreferredSize(new Dimension(140,25));
        top.add(comboHabitacionesOcupadas);
        btnAgregarServicio = new JButton("Agregar Servicio");
        btnRefrescar = new JButton("Refrescar");
        btnVolver = new JButton("Volver");
        top.add(btnAgregarServicio);
        top.add(btnRefrescar);
        top.add(btnVolver);
        add(top, BorderLayout.NORTH);

        String[] cols = {"ID","Fecha","Tipo","Descripción","Costo"};
        tablaServicios = new JTable(new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        });
        tablaServicios.setRowHeight(26);
        JScrollPane scroll = new JScrollPane(tablaServicios);
        add(scroll, BorderLayout.CENTER);
    }

    public JTable getTablaServicios(){return tablaServicios;}
    public JComboBox<String> getComboHabitacionesOcupadas(){return comboHabitacionesOcupadas;}
    public JButton getBtnAgregarServicio(){return btnAgregarServicio;}
    public JButton getBtnRefrescar(){return btnRefrescar;}
    public JButton getBtnVolver(){return btnVolver;}
}
