package view.panels;

import javax.swing.*;
import java.awt.*;

/**
 * PanelDashboard actúa como contenedor identificable del dashboard principal.
 * Actualmente la lógica se genera en VentanaAdmin, pero esta clase permite
 * desacoplar y reutilizar en caso de querer extraerlo.
 */
public class PanelDashboard extends JPanel {
	public PanelDashboard() {
		setLayout(new BorderLayout());
		setOpaque(false);
	}
}

