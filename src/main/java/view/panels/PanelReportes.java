package view.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel simple de reportes (placeholder enriquecido) mostrando KPIs y permitirá filtros.
 */
public class PanelReportes extends JPanel {
    private final JLabel titulo;
    private final JTextArea areaResumen;
    private final JButton btnRefrescar;
    private final JComboBox<String> rangoFechas;

    public PanelReportes() {
        setLayout(new BorderLayout(15,15));
        setBorder(new EmptyBorder(15,15,15,15));
        setBackground(new Color(240,242,247));
        titulo = new JLabel("Reportes & Analítica");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10,10));
        centro.setOpaque(false);
        areaResumen = new JTextArea(8,40);
        areaResumen.setEditable(false);
        areaResumen.setFont(new Font("Consolas", Font.PLAIN, 13));
        areaResumen.setBorder(BorderFactory.createTitledBorder("Resumen"));
        centro.add(new JScrollPane(areaResumen), BorderLayout.CENTER);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Rango:"));
        rangoFechas = new JComboBox<>(new String[]{"Hoy","Últimos 7 días","Este mes"});
        filtros.add(rangoFechas);
        btnRefrescar = new JButton("Refrescar");
        filtros.add(btnRefrescar);
        centro.add(filtros, BorderLayout.NORTH);

        add(centro, BorderLayout.CENTER);
    }

    public void setResumen(String texto) {areaResumen.setText(texto);}    
    public JButton getBtnRefrescar(){return btnRefrescar;}
    public JComboBox<String> getRangoFechas(){return rangoFechas;}
}
