package view.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel de gestión de habitaciones.
 * Muestra habitaciones disponibles y ocupadas, y permite acciones rápidas.
 */
public class PanelHabitaciones extends JPanel {

	private final JTable tablaDisponibles;
	private final JTable tablaOcupadas;
	private final JComboBox<String> comboSeleccionHabitacion;
	private final JButton btnRefrescar;
	private final JButton btnCheckinRapido;
	private final JButton btnLiberar;
	private final JButton btnVolver;

	// Flag para evitar bucles recursivos al limpiar selecciones
	private boolean cambiandoSeleccion = false;

	public PanelHabitaciones() {
		setLayout(new BorderLayout(15, 15));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(new Color(240, 242, 247));

		JLabel titulo = new JLabel("Gestión de Habitaciones");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
		add(titulo, BorderLayout.NORTH);

		// Panel central con tablas
		JPanel centro = new JPanel(new GridLayout(1, 2, 15, 15));
		centro.setOpaque(false);

		// Tabla habitaciones disponibles
		String[] colsDisp = {"Número", "Tipo", "Precio"};
		tablaDisponibles = new JTable(new DefaultTableModel(colsDisp, 0) {
			public boolean isCellEditable(int r, int c) {return false;}
		});
		estilizarTabla(tablaDisponibles);
		JScrollPane spDisp = new JScrollPane(tablaDisponibles);
		spDisp.setBorder(BorderFactory.createTitledBorder("Disponibles"));
		centro.add(spDisp);

		// Tabla habitaciones ocupadas
		String[] colsOcup = {"Número", "Tipo", "Precio", "Cliente"};
		tablaOcupadas = new JTable(new DefaultTableModel(colsOcup, 0) {
			public boolean isCellEditable(int r, int c) {return false;}
		});
		estilizarTabla(tablaOcupadas);
		JScrollPane spOcup = new JScrollPane(tablaOcupadas);
		spOcup.setBorder(BorderFactory.createTitledBorder("Ocupadas"));
		centro.add(spOcup);

		add(centro, BorderLayout.CENTER);

		// Panel inferior de acciones
		JPanel acciones = new JPanel();
		acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
		acciones.setOpaque(false);

		JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fila1.setOpaque(false);
		fila1.add(new JLabel("Hab. rápida:"));
		comboSeleccionHabitacion = new JComboBox<>();
		comboSeleccionHabitacion.setPreferredSize(new Dimension(220, 28));
		fila1.add(comboSeleccionHabitacion);

		btnCheckinRapido = new JButton("Check-in Rápido");
		btnRefrescar = new JButton("Refrescar");
		btnLiberar = new JButton("Liberar Habitación");
		btnVolver = new JButton("Volver Dashboard");

		fila1.add(btnCheckinRapido);
		fila1.add(btnLiberar);
		fila1.add(btnRefrescar);
		fila1.add(btnVolver);

		acciones.add(fila1);
		add(acciones, BorderLayout.SOUTH);

		// Listeners de selección mutuamente excluyentes
		instalarListenersSeleccion();
		actualizarEstadoBotones();
	}

	private void estilizarTabla(JTable tabla) {
		tabla.setRowHeight(26);
		tabla.getTableHeader().setReorderingAllowed(false);
		tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * Configura listeners para que solo una tabla pueda tener selección a la vez
	 * y actualiza el estado de los botones de acción.
	 */
	private void instalarListenersSeleccion() {
		ListSelectionListener listenerDisponibles = new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() || cambiandoSeleccion) return;
				if (tablaDisponibles.getSelectedRow() != -1) {
					cambiandoSeleccion = true;
					tablaOcupadas.clearSelection();
					cambiandoSeleccion = false;
				}
				actualizarEstadoBotones();
			}
		};

		ListSelectionListener listenerOcupadas = new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() || cambiandoSeleccion) return;
				if (tablaOcupadas.getSelectedRow() != -1) {
					cambiandoSeleccion = true;
					tablaDisponibles.clearSelection();
					cambiandoSeleccion = false;
				}
				actualizarEstadoBotones();
			}
		};

		tablaDisponibles.getSelectionModel().addListSelectionListener(listenerDisponibles);
		tablaOcupadas.getSelectionModel().addListSelectionListener(listenerOcupadas);
	}

	/**
	 * Habilita / deshabilita botones según la selección actual.
	 */
	private void actualizarEstadoBotones() {
		boolean disponibleSeleccionada = tablaDisponibles.getSelectedRow() != -1;
		boolean ocupadaSeleccionada = tablaOcupadas.getSelectedRow() != -1;
		btnCheckinRapido.setEnabled(disponibleSeleccionada);
		btnLiberar.setEnabled(ocupadaSeleccionada);
	}

	/**
	 * Devuelve el número (String) de la habitación seleccionada en cualquiera de las tablas.
	 * @return número de habitación o null si nada seleccionado
	 */
	public String getHabitacionSeleccionada() {
		int idx = tablaDisponibles.getSelectedRow();
		if (idx != -1) {
			return tablaDisponibles.getValueAt(idx, 0).toString();
		}
		idx = tablaOcupadas.getSelectedRow();
		if (idx != -1) {
			return tablaOcupadas.getValueAt(idx, 0).toString();
		}
		return null;
	}

	// Getters para el controlador
	public JTable getTablaDisponibles() {return tablaDisponibles;}
	public JTable getTablaOcupadas() {return tablaOcupadas;}
	public JComboBox<String> getComboSeleccionHabitacion() {return comboSeleccionHabitacion;}
	public JButton getBtnRefrescar() {return btnRefrescar;}
	public JButton getBtnCheckinRapido() {return btnCheckinRapido;}
	public JButton getBtnLiberar() {return btnLiberar;}
	public JButton getBtnVolver() {return btnVolver;}
}

