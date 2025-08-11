/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package view.panels;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author asdw
 */
public class PanelClientes extends javax.swing.JPanel {

    // Componentes de búsqueda
    private JTextField txtBuscarCedula;
    private JButton btnBuscarCedula;
    private JButton btnLimpiarBusqueda;

    /**
     * Creates new form PanelClientes
     */
    public PanelClientes() {
        initComponents();
        estilizar();
    }

    private void estilizar() {
        setBackground(new Color(240,242,247));
        jLabel1.setForeground(new Color(52,58,76));
        setBorder(new EmptyBorder(15,15,15,15));
        tablaClientes.setRowHeight(26);
        tablaClientes.getTableHeader().setReorderingAllowed(false);
        tablaClientes.getTableHeader().setBackground(new Color(52,58,76));
        tablaClientes.getTableHeader().setForeground(Color.WHITE);
        tablaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    tablaClientes.setToolTipText("ID legible (CLI-####), clic para seleccionar filas. Use la barra de búsqueda para filtrar por cédula.");
        jScrollPane1.setBorder(BorderFactory.createTitledBorder("Listado de Clientes"));
        JButton[] botones = {btnNuevoCliente, btnEditarCliente, btnEliminarCliente};
        for (JButton b: botones) {

        // Barra de búsqueda (sólo cédula exacta)
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
        barra.setOpaque(false);
        barra.add(new JLabel("Buscar cédula:"));
        txtBuscarCedula = new JTextField(12);
        barra.add(txtBuscarCedula);
        btnBuscarCedula = new JButton("Buscar");
        btnLimpiarBusqueda = new JButton("Limpiar");
        JButton[] buscadores = {btnBuscarCedula, btnLimpiarBusqueda};
        for (JButton bt: buscadores){
            bt.setFocusPainted(false);
            bt.setBackground(new Color(92,146,196));
            bt.setForeground(Color.WHITE);
            bt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        barra.add(btnBuscarCedula);
        barra.add(btnLimpiarBusqueda);
        // Insertar barra debajo del título
        setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        jLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        barra.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(jLabel1);
        top.add(Box.createVerticalStrut(8));
        top.add(barra);
        add(top, BorderLayout.NORTH);
        // Reubicar scroll y botones
        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(jScrollPane1, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.setOpaque(false);
        acciones.add(btnNuevoCliente);
        acciones.add(btnEditarCliente);
        acciones.add(btnEliminarCliente);
        centro.add(acciones, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
            b.setFocusPainted(false);
            b.setBackground(new Color(72,126,176));
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        // Deshabilitar acciones que requieren selección al inicio
        btnEditarCliente.setEnabled(false);
        btnEliminarCliente.setEnabled(false);

        instalarListenerSeleccion();
    }

    private void instalarListenerSeleccion(){
        ListSelectionListener l = new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                boolean sel = tablaClientes.getSelectedRow() != -1;
                btnEditarCliente.setEnabled(sel);
                btnEliminarCliente.setEnabled(sel);
            }
        };
        tablaClientes.getSelectionModel().addListSelectionListener(l);
    }

    public javax.swing.JTable getTablaClientes() {
        return tablaClientes;
    }

    public JTextField getTxtBuscarCedula(){return txtBuscarCedula;}
    public JButton getBtnBuscarCedula(){return btnBuscarCedula;}
    public JButton getBtnLimpiarBusqueda(){return btnLimpiarBusqueda;}

    public javax.swing.JButton getBtnNuevoCliente() {
        return btnNuevoCliente;
    }
    
    public javax.swing.JButton getBtnEditarCliente() {
        return btnEditarCliente;
    }

    public javax.swing.JButton getBtnEliminarCliente() {
        return btnEliminarCliente;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tablaClientes = new javax.swing.JTable();
        btnNuevoCliente = new javax.swing.JButton();
        btnEditarCliente = new javax.swing.JButton();
        btnEliminarCliente = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        tablaClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nombre", "Apellido", "Cédula", "Teléfono"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tablaClientes);

        btnNuevoCliente.setText("Nuevo Cliente");

        btnEditarCliente.setText("Editar Cliente");

        btnEliminarCliente.setText("Eliminar Cliente");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Gestión de Clientes");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnNuevoCliente)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditarCliente)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEliminarCliente)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNuevoCliente)
                    .addComponent(btnEditarCliente)
                    .addComponent(btnEliminarCliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 313, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton btnEditarCliente;
    public javax.swing.JButton btnEliminarCliente;
    public javax.swing.JButton btnNuevoCliente;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JTable tablaClientes;
    // End of variables declaration//GEN-END:variables
}
