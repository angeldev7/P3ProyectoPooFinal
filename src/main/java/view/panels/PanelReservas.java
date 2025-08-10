package view.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel de administración de reservas.
 * Permite crear, finalizar y listar reservas.
 */
public class PanelReservas extends JPanel {

	private final JTable tablaReservas;
	private final JButton btnNuevaReserva;
	private final JButton btnFinalizarReserva;
	private final JButton btnRefrescar;
	private final JButton btnVolver;

	public PanelReservas() {
		setLayout(new BorderLayout(15, 15));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(new Color(240, 242, 247));

		JLabel titulo = new JLabel("Gestión de Reservas");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
		add(titulo, BorderLayout.NORTH);

		// Tabla de reservas
		String[] cols = {"ID", "Cliente", "Hab.", "Ingreso", "Salida", "Estado", "Total"};
		tablaReservas = new JTable(new DefaultTableModel(cols, 0) {
			public boolean isCellEditable(int r, int c) {return false;}
		});
		tablaReservas.setRowHeight(26);
		tablaReservas.getTableHeader().setReorderingAllowed(false);
		JScrollPane sp = new JScrollPane(tablaReservas);
		add(sp, BorderLayout.CENTER);

		// Panel acciones
		JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
		acciones.setOpaque(false);
		btnNuevaReserva = new JButton("Nueva Reserva");
		btnFinalizarReserva = new JButton("Finalizar / Checkout");
		btnRefrescar = new JButton("Refrescar");
		btnVolver = new JButton("Volver Dashboard");
		acciones.add(btnNuevaReserva);
		acciones.add(btnFinalizarReserva);
		acciones.add(btnRefrescar);
		acciones.add(btnVolver);
		add(acciones, BorderLayout.SOUTH);
	}

	public JTable getTablaReservas() {return tablaReservas;}
	public JButton getBtnNuevaReserva() {return btnNuevaReserva;}
	public JButton getBtnFinalizarReserva() {return btnFinalizarReserva;}
	public JButton getBtnRefrescar() {return btnRefrescar;}
	public JButton getBtnVolver() {return btnVolver;}
}

