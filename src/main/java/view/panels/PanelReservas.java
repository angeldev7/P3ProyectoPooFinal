package view.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel de administración de reservas.
 * Permite crear, finalizar y listar reservas.
 */
public class PanelReservas extends JPanel {

	private final JTable tablaReservas;
	private final JButton btnNuevaReserva;
	private final JButton btnFinalizarReserva;
	private final JButton btnAjustarReserva;
	private final JButton btnRefrescar;
	private final JButton btnVolver;
	private final JSpinner spFechaDesde;
	private final JSpinner spFechaHasta;
	private final JComboBox<String> comboEstado;
	private final JButton btnAplicarFiltros;
	private final JButton btnLimpiarFiltros;

	public PanelReservas() {
		setLayout(new BorderLayout(15, 15));
		setBorder(new EmptyBorder(15, 15, 15, 15));
		setBackground(new Color(240, 242, 247));

		JLabel titulo = new JLabel("Gestión de Reservas");
		titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
		add(titulo, BorderLayout.NORTH);

		// Tabla de reservas
		String[] cols = {"ID", "Cliente", "Hab.", "Reserva", "Inicio Plan", "Fin Plan", "Ingreso", "Salida", "Noches", "Estado", "Total"};
		tablaReservas = new JTable(new DefaultTableModel(cols, 0) {
			public boolean isCellEditable(int r, int c) {return false;}
		});
		tablaReservas.setRowHeight(26);
		tablaReservas.getTableHeader().setReorderingAllowed(false);
		JScrollPane sp = new JScrollPane(tablaReservas);
		sp.setBorder(BorderFactory.createTitledBorder("Listado de Reservas"));
		add(sp, BorderLayout.CENTER);

		// Panel acciones
		JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
		acciones.setOpaque(false);
		btnNuevaReserva = new JButton("Nueva Reserva");
		btnFinalizarReserva = new JButton("Finalizar / Checkout");
		btnAjustarReserva = new JButton("Ajustar");
		btnRefrescar = new JButton("Refrescar");
		btnVolver = new JButton("Volver Dashboard");

		// Filtros
		spFechaDesde = new JSpinner(new SpinnerDateModel());
		spFechaHasta = new JSpinner(new SpinnerDateModel());
		JSpinner.DateEditor edDesde = new JSpinner.DateEditor(spFechaDesde, "dd/MM/yyyy");
		JSpinner.DateEditor edHasta = new JSpinner.DateEditor(spFechaHasta, "dd/MM/yyyy");
		spFechaDesde.setEditor(edDesde);
		spFechaHasta.setEditor(edHasta);
		comboEstado = new JComboBox<>(new String[]{"Todos","Activa","Reservada","Check-in","Finalizada"});
		btnAplicarFiltros = new JButton("Filtrar");
		btnLimpiarFiltros = new JButton("Limpiar");

		JButton[] botones = {btnNuevaReserva, btnFinalizarReserva, btnAjustarReserva, btnRefrescar, btnVolver, btnAplicarFiltros, btnLimpiarFiltros};
		for (JButton b: botones){
			b.setFocusPainted(false);
			b.setBackground(new Color(72,126,176));
			b.setForeground(Color.WHITE);
			b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		}

		// Ajuste de columnas básicos
		javax.swing.table.JTableHeader header = tablaReservas.getTableHeader();
		header.setBackground(new Color(52,58,76));
		header.setForeground(Color.WHITE);
		header.setFont(new Font("Segoe UI", Font.BOLD, 12));
		tablaReservas.setSelectionBackground(new Color(72,126,176));
		tablaReservas.setSelectionForeground(Color.WHITE);
		tablaReservas.setAutoCreateRowSorter(true);
		// Anchos sugeridos
		int[] widths = {160,140,45,90,90,90,80,80,60,80,70};
		for (int i=0;i<widths.length && i<tablaReservas.getColumnCount();i++){
			tablaReservas.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		}
		tablaReservas.setToolTipText("Seleccione una reserva para acciones. Puede ordenar haciendo click en los encabezados.");
		acciones.add(new JLabel("Desde:")); acciones.add(spFechaDesde);
		acciones.add(new JLabel("Hasta:")); acciones.add(spFechaHasta);
		acciones.add(new JLabel("Estado:")); acciones.add(comboEstado);
		acciones.add(btnAplicarFiltros);
		acciones.add(btnLimpiarFiltros);
		acciones.add(btnNuevaReserva);
		acciones.add(btnAjustarReserva);
		acciones.add(btnFinalizarReserva);
		acciones.add(btnRefrescar);
		acciones.add(btnVolver);
		add(acciones, BorderLayout.SOUTH);

		// Estado inicial de botones dependientes de selección
		btnFinalizarReserva.setEnabled(false);
		btnAjustarReserva.setEnabled(false);
		instalarListenerSeleccion();
	}

	private void instalarListenerSeleccion(){
		ListSelectionListener l = new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) return;
				boolean sel = tablaReservas.getSelectedRow() != -1;
				btnFinalizarReserva.setEnabled(sel);
				btnAjustarReserva.setEnabled(sel);
			}
		};
		tablaReservas.getSelectionModel().addListSelectionListener(l);
	}

	public JTable getTablaReservas() {return tablaReservas;}
	public JButton getBtnNuevaReserva() {return btnNuevaReserva;}
	public JButton getBtnFinalizarReserva() {return btnFinalizarReserva;}
	public JButton getBtnAjustarReserva() {return btnAjustarReserva;}
	public JButton getBtnRefrescar() {return btnRefrescar;}
	public JButton getBtnVolver() {return btnVolver;}
	public JSpinner getSpFechaDesde() {return spFechaDesde;}
	public JSpinner getSpFechaHasta() {return spFechaHasta;}
	public JComboBox<String> getComboEstado() {return comboEstado;}
	public JButton getBtnAplicarFiltros() {return btnAplicarFiltros;}
	public JButton getBtnLimpiarFiltros() {return btnLimpiarFiltros;}
}

