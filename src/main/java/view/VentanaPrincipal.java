/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import model.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;

/**
 *
 * @author asdw
 */
public class VentanaPrincipal extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName());

    /**
     * Creates new form VentanaPrinciapl
     */
    public VentanaPrincipal() {
        initComponents();
        setLocationRelativeTo(null);
    // Aumentar altura total en +50 para evitar que botones queden ocultos
    try { setSize(getWidth(), getHeight() + 50); } catch (Exception ignored) {}
        initCustomComponents();
    }

    /**
     * Vuelve a la ventana de selección principal y cierra esta ventana.
     */
    private void volverASelector(){
        try {
            if (modeloService!=null && commandInvoker!=null && gestorDisponibilidad!=null){
                VentanaSelector selector = new VentanaSelector(modeloService, commandInvoker, gestorDisponibilidad);
                selector.setVisible(true);
        dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Dependencias no configuradas para volver al selector", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex){
            logger.warning("No se pudo abrir VentanaSelector: "+ex.getMessage());
    }
    }
    
    /**
     * Inicializa componentes personalizados y atajos de teclado.
     */
    private IModeloService modeloService; // inyectado externamente
    private command.ICommandInvoker commandInvoker; // para volver al selector
    private singleton.GestorDisponibilidad gestorDisponibilidad; // para volver al selector
    private String idReservaActivaUsuario; // reserva activa asociada al usuario (según cédula)
    private String metodoPagoSeleccionado; // "TRANSFERENCIA" o "EFECTIVO"
    private Map<String,String> datosPago = new HashMap<>();
    private boolean modoAdmin = false; // controla visibilidad de columnas sensibles
    // Campo procesandoCheckin eliminado (lógica centralizada en controlador)

    // Factores de validación reutilizados
    private static final int LONGITUD_CEDULA_TELEFONO = 10;

    private void initCustomComponents() {
        configurarAtajosTeclado();
        aplicarFiltrosEntrada();
        configurarEventosUsuario();
        if (BtnVolverSelector != null) {
            BtnVolverSelector.addActionListener(e -> volverASelector());
        }
    // inicializar spinners si existen
    try { if (SpinnerDiasHastaLlegada!=null) SpinnerDiasHastaLlegada.setValue(0); } catch(Exception ignored){}
    try { if (SpinnerNoches!=null) SpinnerNoches.setValue(1); } catch(Exception ignored){}
    // listeners servicios
    if (BtnSolicitarServicio != null) BtnSolicitarServicio.addActionListener(e -> solicitarServicio());
    if (BtnRefrescarServicios != null) BtnRefrescarServicios.addActionListener(e -> cargarServiciosReservaActiva());
    if (BtnValidarServicio != null) BtnValidarServicio.addActionListener(e -> validarCedulaHabitacionServicio());
    if (BtnVolverServicios != null) BtnVolverServicios.addActionListener(e -> volverASelector());
    if (jTabbedPane2 != null) jTabbedPane2.addChangeListener(e -> { 
        if (jTabbedPane2.getSelectedComponent() == Servicios) {
            cargarHabitacionesOcupadasEnServicios();
            cargarServiciosReservaActiva();
        }
    });
    if (TxtCedulaServicio != null) setSoloDigitos(TxtCedulaServicio, LONGITUD_CEDULA_TELEFONO);
    configurarColumnasServicios();
    }

    /** Permite configurar si la ventana opera en modo administrador. */
    public void setModoAdmin(boolean admin){
        this.modoAdmin = admin;
        configurarColumnasServicios();
    }

    /** Columnas sensibles de la tabla de servicios se ocultan para usuarios normales. */
    private void configurarColumnasServicios(){
        if (TablaServicios == null) return;
        // Actualmente la primera columna es ID del servicio (sensible); se oculta si no es admin
        javax.swing.table.TableColumnModel cm = TablaServicios.getColumnModel();
        if (cm.getColumnCount()==0) return;
        try {
            javax.swing.table.TableColumn colId = cm.getColumn(0);
            if (!modoAdmin){
                colId.setMinWidth(0); colId.setMaxWidth(0); colId.setPreferredWidth(0);
            } else {
                colId.setMinWidth(50); colId.setMaxWidth(150); colId.setPreferredWidth(80);
            }
        } catch (Exception ignored) {}
    }

    public void setModeloService(IModeloService modeloService){
        this.modeloService = modeloService;
        // Inicializar habitaciones disponibles al cargar servicio
        cargarHabitacionesDisponibles();
    }

    public void setCommandInvoker(command.ICommandInvoker inv){
        this.commandInvoker = inv;
    }

    public void setGestorDisponibilidad(singleton.GestorDisponibilidad gd){
        this.gestorDisponibilidad = gd;
    }

    /**
     * Inyección agrupada opcional
     */
    public void configurarDependencias(IModeloService ms, command.ICommandInvoker inv, singleton.GestorDisponibilidad gd){
        setModeloService(ms);
        setCommandInvoker(inv);
        setGestorDisponibilidad(gd);
    }
    
    /**
     * Configura los atajos de teclado Ctrl+Z y Ctrl+Y.
     */
    private void configurarAtajosTeclado() {
        // Ctrl+Z para Undo
        javax.swing.KeyStroke undoKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Z, 
            java.awt.event.InputEvent.CTRL_DOWN_MASK
        );
        this.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(undoKeyStroke, "undo");
        this.getRootPane().getActionMap().put("undo", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (BtnUndo.isEnabled()) {
                    BtnUndo.doClick();
                }
            }
        });
        
        // Ctrl+Y para Redo
        javax.swing.KeyStroke redoKeyStroke = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_Y, 
            java.awt.event.InputEvent.CTRL_DOWN_MASK
        );
        this.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(redoKeyStroke, "redo");
        this.getRootPane().getActionMap().put("redo", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (BtnRedo.isEnabled()) {
                    BtnRedo.doClick();
                }
            }
        });
    }

    private void aplicarFiltrosEntrada(){
        setSoloLetras(TxtNombre);
        setSoloLetras(TxtApellido);
        setSoloDigitos(TxtCedula, LONGITUD_CEDULA_TELEFONO);
        setSoloDigitos(TxtTelefono, LONGITUD_CEDULA_TELEFONO);
    }

    private void configurarEventosUsuario(){
    // Listener interno de check-in desactivado para evitar doble ejecución;
    // ahora el controlador (ControladorVentanaPrincipal) gestiona el botón.
        BtnLimpiarCampos.addActionListener(e -> limpiarCampos());
        RBtnMetodoTransferencia.addActionListener(e -> onSeleccionMetodoPago("TRANSFERENCIA"));
        RBtnMetodoEfectivo.addActionListener(e -> onSeleccionMetodoPago("EFECTIVO"));
    }

    private void onSeleccionMetodoPago(String metodo){
        if (metodoPagoSeleccionado!=null && !metodoPagoSeleccionado.equals(metodo) && !datosPago.isEmpty()){
            int r = JOptionPane.showConfirmDialog(this, "Cambiar método eliminará los datos capturados del método anterior. ¿Continuar?", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r!=JOptionPane.YES_OPTION){
                // revertir selección
                if ("TRANSFERENCIA".equals(metodoPagoSeleccionado)) RBtnMetodoTransferencia.setSelected(true); else RBtnMetodoEfectivo.setSelected(true);
                return;
            }
            datosPago.clear();
        }
        metodoPagoSeleccionado = metodo;
        // NUEVO: Eliminamos ventanas emergentes para evitar montos absurdos.
        // Se registra automáticamente un pago válido y controlado.
        registrarPagoSeguro(metodo);
    }

    /**
     * Registra los datos de pago sin mostrar diálogos. Evita que el usuario ingrese valores irreales.
     * EFECTIVO: se asume pago exacto (= precio habitación) y cambio 0.
     * TRANSFERENCIA: se colocan valores placeholder mínimos para que la lógica continúe; si deseas forzar captura real
     * podrías reemplazar esto por un panel embebido en la UI más adelante.
     */
    private void registrarPagoSeguro(String metodo){
        try {
            if ("EFECTIVO".equals(metodo)){
                double precio = obtenerPrecioHabitacionSeleccionada();
                if (precio <= 0){
                    JOptionPane.showMessageDialog(this, "Seleccione primero una habitación válida antes de elegir el método de pago.", "Validación", JOptionPane.WARNING_MESSAGE);
                    limpiarPagoSeleccion();
                    return;
                }
                datosPago.put("entregado", String.format("%.2f", precio));
                datosPago.put("cambio", "0.00");
                JOptionPane.showMessageDialog(this, "Pago en efectivo registrado automáticamente por el monto exacto ($"+String.format("%.2f", precio)+").", "Pago", JOptionPane.INFORMATION_MESSAGE);
            } else if ("TRANSFERENCIA".equals(metodo)) {
                // Ahora exigimos datos reales mediante diálogo validado (sin permitir placeholders).
                capturarTransferencia();
            }
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this, "Error registrando método de pago: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            limpiarPagoSeleccion();
        }
    }

    /** Captura interactiva de datos de transferencia con validaciones estrictas. */
    private void capturarTransferencia(){
        JTextField txtBanco = new JTextField(datosPago.getOrDefault("banco", ""));
        JTextField txtCuenta = new JTextField(datosPago.getOrDefault("cuenta", ""));
        JTextField txtReferencia = new JTextField(datosPago.getOrDefault("referencia", ""));
        aplicarFiltroTexto(txtBanco,30,"[\\p{L} ]", s->s);
        aplicarFiltroTexto(txtCuenta,24,"[0-9]", s->s);
        aplicarFiltroTexto(txtReferencia,30,"[A-Za-z0-9]", String::toUpperCase);
        JPanel panel = new JPanel(new GridLayout(0,1,4,4));
        panel.add(new JLabel("Banco:")); panel.add(txtBanco);
        panel.add(new JLabel("N° Cuenta / CCI:")); panel.add(txtCuenta);
        panel.add(new JLabel("Código / Referencia transacción:")); panel.add(txtReferencia);
        int op = JOptionPane.showConfirmDialog(this, panel, "Datos Transferencia", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op!=JOptionPane.OK_OPTION){
            // Si cancela y aún no había datos previos, revertir selección y limpiar
            if (!datosPago.containsKey("banco")) { limpiarPagoSeleccion(); }
            return;
        }
        String banco = txtBanco.getText().trim();
        String cuenta = txtCuenta.getText().trim();
        String referencia = txtReferencia.getText().trim();
        if (banco.isEmpty()||cuenta.isEmpty()||referencia.isEmpty()){
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Validación", JOptionPane.WARNING_MESSAGE);
            capturarTransferencia();
            return;
        }
        if (cuenta.length() < 6){
            JOptionPane.showMessageDialog(this, "La cuenta debe tener al menos 6 dígitos", "Validación", JOptionPane.WARNING_MESSAGE);
            capturarTransferencia();
            return;
        }
        if (referencia.length() < 6){
            JOptionPane.showMessageDialog(this, "La referencia debe tener mínimo 6 caracteres", "Validación", JOptionPane.WARNING_MESSAGE);
            capturarTransferencia();
            return;
        }
        datosPago.put("banco", banco);
        datosPago.put("cuenta", cuenta);
        datosPago.put("referencia", referencia.toUpperCase());
        JOptionPane.showMessageDialog(this, "Pago por transferencia registrado", "Pago", JOptionPane.INFORMATION_MESSAGE);
    }

    private double obtenerPrecioHabitacionSeleccionada(){
        try {
            String sel = (String) CmboxHabitaciones.getSelectedItem();
            if (sel==null) return 0.0;
            // Formato: #001 | Tipo | $120,00  (puede variar separador decimal)
            int idx = sel.lastIndexOf('$');
            if (idx==-1) return 0.0;
            String num = sel.substring(idx+1).replaceAll("[^0-9.,]", "").replace(",", ".");
            return Double.parseDouble(num);
        } catch (Exception ex){ return 0.0; }
    }

    private void cargarHabitacionesDisponibles(){
        if (modeloService == null) return;
        try {
            List<Habitacion> libres = modeloService.obtenerHabitacionesDisponibles();
            CmboxHabitaciones.removeAllItems();
            for (Habitacion h: libres){
                CmboxHabitaciones.addItem(formatoHabitacion(h));
            }
            actualizarTablasHabitaciones();
        } catch (Exception ex){
            logger.warning("No se pudieron cargar habitaciones: "+ex.getMessage());
        }
    }

    private void actualizarTablasHabitaciones(){
        if (modeloService == null) return;
        javax.swing.table.DefaultTableModel modeloDisp = (javax.swing.table.DefaultTableModel) TablaHabitacionesDisponibles.getModel();
        javax.swing.table.DefaultTableModel modeloOcup = (javax.swing.table.DefaultTableModel) TablaHabitacionesOcupadas.getModel();
        modeloDisp.setRowCount(0); modeloOcup.setRowCount(0);
        for (Habitacion h : modeloService.obtenerHabitacionesDisponibles()){
            modeloDisp.addRow(new Object[]{"#"+h.getNumero(), h.getTipo(), String.format("$%.2f", h.getPrecio())});
        }
        for (Habitacion h : modeloService.obtenerHabitacionesOcupadas()){
            modeloOcup.addRow(new Object[]{"#"+h.getNumero(), h.getTipo(), String.format("$%.2f", h.getPrecio())});
        }
    }

    private String formatoHabitacion(Habitacion h){
        return "#"+h.getNumero()+" | "+h.getTipo()+" | "+String.format("$%.2f", h.getPrecio());
    }

    // (Lógica de check-in y validación movida al controlador para evitar duplicación)

    private void limpiarCampos(){
    TxtNombre.setText("");
    TxtApellido.setText("");
    TxtCedula.setText("");
    TxtTelefono.setText("");
    try { if (SpinnerDiasHastaLlegada!=null) SpinnerDiasHastaLlegada.setValue(0); } catch(Exception ignored){}
    try { if (SpinnerNoches!=null) SpinnerNoches.setValue(1); } catch(Exception ignored){}
    // Limpiar selección de método de pago y datos asociados
    if (GruposBtnMetodoPagos != null) GruposBtnMetodoPagos.clearSelection();
    metodoPagoSeleccionado = null;
    datosPago.clear();
    }

    // ==== Accesores para controlador (validación unificada) ====
    public String getMetodoPagoSeleccionado(){ return metodoPagoSeleccionado; }
    public boolean pagoTransferenciaCompleto(){
        return "TRANSFERENCIA".equals(metodoPagoSeleccionado) &&
               datosPago.containsKey("banco") && datosPago.containsKey("cuenta") && datosPago.containsKey("referencia");
    }
    public boolean pagoEfectivoCompleto(){
        return "EFECTIVO".equals(metodoPagoSeleccionado) &&
               datosPago.containsKey("entregado") && datosPago.containsKey("cambio");
    }
    public void limpiarPagoSeleccion(){
        if (GruposBtnMetodoPagos != null) GruposBtnMetodoPagos.clearSelection();
        metodoPagoSeleccionado = null;
        datosPago.clear();
    }

    // ==== Servicios a la habitación (usuario) ====
    // Solicita un servicio (solo si ya hizo check-in y tiene reserva activa)
    public void solicitarServicio(){
        if (modeloService==null){
            JOptionPane.showMessageDialog(this, "Servicio no disponible (modelo nulo)", "Error", JOptionPane.ERROR_MESSAGE); return;
        }
        // Validar que usuario haya validado su cédula y habitación
        if (idReservaActivaUsuario==null){
            JOptionPane.showMessageDialog(this, "Valide primero su cédula y habitación ocupada", "Validación", JOptionPane.WARNING_MESSAGE); return;
        }
        // Confirmar coincidencia actual antes de registrar
        if (!coincideHabitacionSeleccionadaConReserva()) {
            JOptionPane.showMessageDialog(this, "La habitación seleccionada ya no coincide con la reserva validada. Revalide.", "Validación", JOptionPane.WARNING_MESSAGE);
            idReservaActivaUsuario = null;
            return;
        }
        String[] tipos = {ServicioHabitacion.TIPO_COMIDA, ServicioHabitacion.TIPO_BEBIDA, ServicioHabitacion.TIPO_LIMPIEZA};
        JComboBox<String> comboTipo = new JComboBox<>(tipos);
        JTextField descripcion = new JTextField();
        JTextField especiasField = new JTextField();
        JLabel especiasLabel = new JLabel("Especias (solo comida):");
        JPanel panel = new JPanel(new java.awt.GridLayout(0,1,4,4));
        panel.add(new JLabel("Tipo:")); panel.add(comboTipo);
        panel.add(new JLabel("Descripción (opcional):")); panel.add(descripcion);
        panel.add(especiasLabel); panel.add(especiasField);
        Runnable toggle = ()->{boolean vis = ServicioHabitacion.TIPO_COMIDA.equals(comboTipo.getSelectedItem()); especiasLabel.setVisible(vis); especiasField.setVisible(vis); if(!vis) especiasField.setText("");};
        comboTipo.addActionListener(e -> toggle.run()); toggle.run();
        int op = JOptionPane.showConfirmDialog(this, panel, "Nuevo Servicio", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;
        try {
            String tipo = (String) comboTipo.getSelectedItem();
            // Encontrar habitación ocupada asociada a la reserva activa
            Reserva activa = modeloService.obtenerTodasReservas().stream().filter(r -> r.getId().equals(idReservaActivaUsuario)).findFirst().orElse(null);
            if (activa == null){JOptionPane.showMessageDialog(this, "Reserva no encontrada", "Error", JOptionPane.ERROR_MESSAGE); return;}
            Habitacion hab = modeloService.obtenerTodasHabitaciones().stream().filter(h -> h.getId().equals(activa.getIdHabitacion())).findFirst().orElse(null);
            if (hab == null){JOptionPane.showMessageDialog(this, "Habitación asociada no existe", "Error", JOptionPane.ERROR_MESSAGE); return;}
            ServicioHabitacion servicio;
            if (ServicioHabitacion.TIPO_COMIDA.equals(tipo)){
                java.util.List<String> especias = new java.util.ArrayList<>();
                for (String s: especiasField.getText().split(",")){String t=s.trim(); if(!t.isEmpty()) especias.add(t);} 
                servicio = ServicioHabitacion.crearComida(activa.getId(), hab.getId(), descripcion.getText(), especias);
            } else if (ServicioHabitacion.TIPO_BEBIDA.equals(tipo)){
                servicio = ServicioHabitacion.crearBebida(activa.getId(), hab.getId(), descripcion.getText());
            } else {
                servicio = ServicioHabitacion.crearLimpieza(activa.getId(), hab.getId());
            }
            boolean ok = modeloService.registrarServicioHabitacion(servicio);
            if (ok) JOptionPane.showMessageDialog(this, "Servicio registrado ($"+String.format("%.2f", servicio.getCosto())+")", "Info", JOptionPane.INFORMATION_MESSAGE);
            else JOptionPane.showMessageDialog(this, "No se pudo registrar el servicio", "Error", JOptionPane.ERROR_MESSAGE);
            if (ok) cargarServiciosReservaActiva();
        } catch (Exception ex){
            JOptionPane.showMessageDialog(this, "Error servicio: "+ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarServiciosReservaActiva(){
        if (modeloService==null || idReservaActivaUsuario==null || TablaServicios==null) return;
        List<ServicioHabitacion> servicios = modeloService.obtenerServiciosPorReserva(idReservaActivaUsuario);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) TablaServicios.getModel();
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
        for (ServicioHabitacion s: servicios){
            model.addRow(new Object[]{
                s.getId()!=null? s.getId():"(pendiente)",
                sdf.format(s.getFecha()),
                s.getTipo(),
                s.getDescripcion()!=null? s.getDescripcion():"",
                String.format("$%.2f", s.getCosto()),
                (s.getEspecias()!=null && !s.getEspecias().isEmpty())? String.join(", ", s.getEspecias()): "-"
            });
        }
    configurarColumnasServicios();
    }

    // Carga habitaciones ocupadas en combo de servicios
    private void cargarHabitacionesOcupadasEnServicios(){
        if (modeloService==null || CmboxHabitacionReservada==null) return;
        CmboxHabitacionReservada.removeAllItems();
        for (Habitacion h: modeloService.obtenerHabitacionesOcupadas()){
            CmboxHabitacionReservada.addItem("#"+h.getNumero());
        }
    }

    // Valida que la cédula ingresada tenga una reserva activa y que la habitación seleccionada coincida
    private void validarCedulaHabitacionServicio(){
        if (modeloService==null) return;
        String ced = TxtCedulaServicio!=null? TxtCedulaServicio.getText().trim():"";
        if (ced.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese cédula", "Validación", JOptionPane.WARNING_MESSAGE); return; }
        Reserva r = modeloService.buscarReservaActivaPorCedula(ced);
        if (r==null){
            idReservaActivaUsuario = null;
            JOptionPane.showMessageDialog(this, "No existe reserva activa para esa cédula", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Verificar habitación seleccionada
        if (!coincideHabitacionSeleccionadaConReserva(r)){
            idReservaActivaUsuario = null;
            JOptionPane.showMessageDialog(this, "La habitación seleccionada no corresponde a la reserva de la cédula.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        idReservaActivaUsuario = r.getId();
        JOptionPane.showMessageDialog(this, "Validación exitosa. Puede solicitar servicios.", "Validación", JOptionPane.INFORMATION_MESSAGE);
        cargarServiciosReservaActiva();
    }

    private boolean coincideHabitacionSeleccionadaConReserva(){
        if (modeloService==null || idReservaActivaUsuario==null) return false;
        Reserva r = modeloService.obtenerTodasReservas().stream().filter(x -> x.getId().equals(idReservaActivaUsuario)).findFirst().orElse(null);
        if (r==null) return false;
        return coincideHabitacionSeleccionadaConReserva(r);
    }

    private boolean coincideHabitacionSeleccionadaConReserva(Reserva r){
        if (r==null) return false;
        String sel = (String) (CmboxHabitacionReservada!=null? CmboxHabitacionReservada.getSelectedItem():null);
        if (sel==null) return false;
        String numero = sel.replace("#", "").trim();
        Habitacion habReserva = modeloService.obtenerTodasHabitaciones().stream().filter(h -> h.getId().equals(r.getIdHabitacion())).findFirst().orElse(null);
        if (habReserva==null) return false;
        return numero.equalsIgnoreCase(habReserva.getNumero());
    }

    // ===== Utilidades de validación/capitalización =====
    private void setSoloLetras(JTextField tf){
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter(){
            @Override public void replace(FilterBypass fb,int offset,int length,String text,AttributeSet attrs) throws BadLocationException {
                if (text!=null && text.matches(".*[0-9].*")) {Toolkit.getDefaultToolkit().beep(); return;} fb.replace(offset,length,text,attrs);
            }
            @Override public void insertString(FilterBypass fb,int offset,String string,AttributeSet attr) throws BadLocationException {replace(fb,offset,0,string,attr);} });
    }
    private void setSoloDigitos(JTextField tf,int max){
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter(){
            @Override public void replace(FilterBypass fb,int offset,int length,String text,AttributeSet attrs) throws BadLocationException {
                if (text==null) {fb.replace(offset,length,null,attrs); return;} String actual=fb.getDocument().getText(0,fb.getDocument().getLength()); String nuevo=actual.substring(0,offset)+text+actual.substring(offset+length);
                if(!nuevo.matches("\\d{0,"+max+"}")){Toolkit.getDefaultToolkit().beep(); return;} fb.replace(offset,length,text.replaceAll("[^0-9]",""),attrs);
            }
            @Override public void insertString(FilterBypass fb,int offset,String string,AttributeSet attr) throws BadLocationException {replace(fb,offset,0,string,attr);} });
    }
    // Utilidades de capitalización/validación ahora en el controlador (métodos retirados)

    // ===== Filtros de entrada especializados para métodos de pago =====
    private void aplicarFiltroTexto(JTextField field,int maxLen,String allowedRegexChars, java.util.function.Function<String,String> transform){
        ((AbstractDocument)field.getDocument()).setDocumentFilter(new DocumentFilter(){
            private boolean valido(String texto){return texto.length()<=maxLen && texto.matches("["+allowedRegexChars+"]*");}
            @Override public void replace(FilterBypass fb,int offset,int length,String text,AttributeSet attrs) throws BadLocationException {mod(fb,offset,length,text,attrs);}            
            @Override public void insertString(FilterBypass fb,int offset,String string,AttributeSet attr) throws BadLocationException {mod(fb,offset,0,string,attr);}            
            private void mod(FilterBypass fb,int offset,int length,String text,AttributeSet attrs) throws BadLocationException { if(text==null){fb.replace(offset,length,null,attrs); return;} String actual=fb.getDocument().getText(0,fb.getDocument().getLength()); String nuevo=actual.substring(0,offset)+text+actual.substring(offset+length); if(!valido(nuevo)){Toolkit.getDefaultToolkit().beep(); return;} fb.replace(offset,length, transform.apply(text),attrs);} });
    }
    // aplicarFiltroTextoDecimal removido (no usado tras refactor de pagos)
    
    // Getters para que el controlador pueda acceder a los botones
    public javax.swing.JButton getBtnUndo() {
        return BtnUndo;
    }
    
    public javax.swing.JButton getBtnRedo() {
        return BtnRedo;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        GruposBtnMetodoPagos = new javax.swing.ButtonGroup();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        PanelHabitaciones = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TablaHabitacionesOcupadas = new javax.swing.JTable();
        LblHabitacionDisponible = new javax.swing.JLabel();
        LblHabitacionOcupada = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TablaHabitacionesDisponibles = new javax.swing.JTable();
        PanelAnularReserva = new javax.swing.JPanel();
        LblCheckCedulaAnular = new javax.swing.JLabel();
        TxtBuscarCedulaAnular = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        LblCheckNombre4 = new javax.swing.JLabel();
        PanelCheckin = new javax.swing.JPanel();
        LblCheckNombre = new javax.swing.JLabel();
        LblCheckApellido = new javax.swing.JLabel();
        LblCheckCedula = new javax.swing.JLabel();
        LblCheckTelefono = new javax.swing.JLabel();
        TxtNombre = new javax.swing.JTextField();
        TxtApellido = new javax.swing.JTextField();
        TxtCedula = new javax.swing.JTextField();
        TxtTelefono = new javax.swing.JTextField();
        BtnCheckin = new javax.swing.JButton();
        LblCheckHabitacion = new javax.swing.JLabel();
        CmboxHabitaciones = new javax.swing.JComboBox<>();
        BtnLimpiarCampos = new javax.swing.JButton();
        RBtnMetodoTransferencia = new javax.swing.JRadioButton();
        RBtnMetodoEfectivo = new javax.swing.JRadioButton();
        LblMetodoPago = new javax.swing.JLabel();
        Servicios = new javax.swing.JPanel();
    JLblHabitacionReservada = new javax.swing.JLabel();
    CmboxHabitacionReservada = new javax.swing.JComboBox<>();
    LblCedulaServicio = new javax.swing.JLabel();
    TxtCedulaServicio = new javax.swing.JTextField();
    BtnValidarServicio = new javax.swing.JButton();
    BtnSolicitarServicio = new javax.swing.JButton();
    BtnRefrescarServicios = new javax.swing.JButton();
    BtnVolverServicios = new javax.swing.JButton();
    jScrollPaneServicios = new javax.swing.JScrollPane();
    TablaServicios = new javax.swing.JTable();
        BtnUndo = new javax.swing.JButton();
        BtnRedo = new javax.swing.JButton();
    BtnVolverSelector = new javax.swing.JButton();
        LblContacto = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane2.setPreferredSize(new java.awt.Dimension(600, 500));

        TablaHabitacionesOcupadas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Habitacion", "Tipo", "Precio"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(TablaHabitacionesOcupadas);

        LblHabitacionDisponible.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblHabitacionDisponible.setText("Habitaciones disponibles");

        LblHabitacionOcupada.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblHabitacionOcupada.setText("Habitaciones ocupadas");

        TablaHabitacionesDisponibles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Habitaciones", "Tipo", "Precio"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(TablaHabitacionesDisponibles);

        javax.swing.GroupLayout PanelHabitacionesLayout = new javax.swing.GroupLayout(PanelHabitaciones);
        PanelHabitaciones.setLayout(PanelHabitacionesLayout);
        PanelHabitacionesLayout.setHorizontalGroup(
            PanelHabitacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelHabitacionesLayout.createSequentialGroup()
                .addGroup(PanelHabitacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(PanelHabitacionesLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addComponent(LblHabitacionDisponible))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
                .addGroup(PanelHabitacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelHabitacionesLayout.createSequentialGroup()
                        .addComponent(LblHabitacionOcupada)
                        .addGap(29, 29, 29))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelHabitacionesLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        PanelHabitacionesLayout.setVerticalGroup(
            PanelHabitacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanelHabitacionesLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(PanelHabitacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LblHabitacionDisponible)
                    .addComponent(LblHabitacionOcupada))
                .addGap(18, 18, 18)
                .addGroup(PanelHabitacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Habitaciones", PanelHabitaciones);

        LblCheckCedulaAnular.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblCheckCedulaAnular.setText("Cedula");

        TxtBuscarCedulaAnular.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        jButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jButton2.setText("Anular reserva");

        LblCheckNombre4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblCheckNombre4.setText("Escriba su cedula para anular su reserva");

        javax.swing.GroupLayout PanelAnularReservaLayout = new javax.swing.GroupLayout(PanelAnularReserva);
        PanelAnularReserva.setLayout(PanelAnularReservaLayout);
        PanelAnularReservaLayout.setHorizontalGroup(
            PanelAnularReservaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelAnularReservaLayout.createSequentialGroup()
                .addGroup(PanelAnularReservaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelAnularReservaLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(PanelAnularReservaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LblCheckNombre4, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PanelAnularReservaLayout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(LblCheckCedulaAnular, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(TxtBuscarCedulaAnular, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(PanelAnularReservaLayout.createSequentialGroup()
                        .addGap(98, 98, 98)
                        .addComponent(jButton2)))
                .addContainerGap(217, Short.MAX_VALUE))
        );
        PanelAnularReservaLayout.setVerticalGroup(
            PanelAnularReservaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelAnularReservaLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(LblCheckNombre4)
                .addGap(52, 52, 52)
                .addGroup(PanelAnularReservaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LblCheckCedulaAnular)
                    .addComponent(TxtBuscarCedulaAnular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(64, 64, 64)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(222, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Anular reserva", PanelAnularReserva);

        LblCheckNombre.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblCheckNombre.setText("Nombre");

        LblCheckApellido.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblCheckApellido.setText("Apellido");

        LblCheckCedula.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblCheckCedula.setText("Cedula");

        LblCheckTelefono.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblCheckTelefono.setText("Telefono");

        TxtNombre.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        TxtApellido.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        TxtCedula.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        TxtTelefono.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        BtnCheckin.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        BtnCheckin.setText("Check-in");

        LblCheckHabitacion.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        LblCheckHabitacion.setText("Habitacion");

        CmboxHabitaciones.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        BtnLimpiarCampos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        BtnLimpiarCampos.setText("Limpiar campos");

        GruposBtnMetodoPagos.add(RBtnMetodoTransferencia);
        RBtnMetodoTransferencia.setText("Transferencia");

        GruposBtnMetodoPagos.add(RBtnMetodoEfectivo);
        RBtnMetodoEfectivo.setText("Efectivo");

        LblMetodoPago.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        LblMetodoPago.setText("Metodo de pagos");

        javax.swing.GroupLayout PanelCheckinLayout = new javax.swing.GroupLayout(PanelCheckin);
        PanelCheckin.setLayout(PanelCheckinLayout);
        PanelCheckinLayout.setHorizontalGroup(
            PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelCheckinLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelCheckinLayout.createSequentialGroup()
                        .addComponent(BtnLimpiarCampos)
                        .addGap(18, 18, 18)
                        .addComponent(BtnCheckin, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelCheckinLayout.createSequentialGroup()
                        .addComponent(RBtnMetodoTransferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(81, 81, 81)
                        .addComponent(RBtnMetodoEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(PanelCheckinLayout.createSequentialGroup()
                            .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(LblCheckNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(LblCheckApellido, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(LblCheckCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(LblCheckTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(28, 28, 28)
                            .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(TxtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(TxtApellido, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(TxtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(TxtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PanelCheckinLayout.createSequentialGroup()
                            .addComponent(LblCheckHabitacion)
                            .addGap(18, 18, 18)
                            .addComponent(CmboxHabitaciones, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(LblMetodoPago, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(228, Short.MAX_VALUE))
        );

        PanelCheckinLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {LblCheckApellido, LblCheckCedula, LblCheckNombre, LblCheckTelefono});

        PanelCheckinLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {TxtApellido, TxtCedula, TxtNombre, TxtTelefono});

        PanelCheckinLayout.setVerticalGroup(
            PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelCheckinLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LblCheckNombre)
                    .addComponent(TxtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LblCheckApellido)
                    .addComponent(TxtApellido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LblCheckCedula)
                    .addComponent(TxtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LblCheckTelefono)
                    .addComponent(TxtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LblCheckHabitacion)
                    .addComponent(CmboxHabitaciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LblMetodoPago)
                .addGap(16, 16, 16)
                .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RBtnMetodoTransferencia)
                    .addComponent(RBtnMetodoEfectivo))
                .addGap(48, 48, 48)
                .addGroup(PanelCheckinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnLimpiarCampos, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtnCheckin, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        PanelCheckinLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {LblCheckApellido, LblCheckCedula, LblCheckNombre, LblCheckTelefono});

        PanelCheckinLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {TxtApellido, TxtCedula, TxtNombre, TxtTelefono});

        jTabbedPane2.addTab("Check-in", PanelCheckin);

        JLblHabitacionReservada.setText("Habitación ocupada:");

        CmboxHabitacionReservada.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));

        LblCedulaServicio.setText("Cédula:");

        BtnValidarServicio.setText("Validar");

        BtnSolicitarServicio.setText("Solicitar servicio");

        BtnRefrescarServicios.setText("Refrescar");

        BtnVolverServicios.setText("Volver");

        TablaServicios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"ID", "Fecha", "Tipo", "Descripción", "Costo", "Especias"}
        ) {
            boolean[] canEdit = new boolean [] { false,false,false,false,false,false };
            public boolean isCellEditable(int rowIndex, int columnIndex) { return canEdit[columnIndex]; }
        });
        jScrollPaneServicios.setViewportView(TablaServicios);

        javax.swing.GroupLayout ServiciosLayout = new javax.swing.GroupLayout(Servicios);
        Servicios.setLayout(ServiciosLayout);
        ServiciosLayout.setHorizontalGroup(
            ServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ServiciosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneServicios, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addGroup(ServiciosLayout.createSequentialGroup()
                        .addGroup(ServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(ServiciosLayout.createSequentialGroup()
                                .addComponent(LblCedulaServicio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TxtCedulaServicio, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(JLblHabitacionReservada)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CmboxHabitacionReservada, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(BtnValidarServicio))
                            .addGroup(ServiciosLayout.createSequentialGroup()
                                .addComponent(BtnSolicitarServicio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(BtnRefrescarServicios)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(BtnVolverServicios)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        ServiciosLayout.setVerticalGroup(
            ServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ServiciosLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(ServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LblCedulaServicio)
                    .addComponent(TxtCedulaServicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(JLblHabitacionReservada)
                    .addComponent(CmboxHabitacionReservada, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtnValidarServicio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPaneServicios, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(ServiciosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnSolicitarServicio)
                    .addComponent(BtnRefrescarServicios)
                    .addComponent(BtnVolverServicios))
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Servicios", Servicios);

        BtnUndo.setText("Deshacer check-in");

        BtnRedo.setText("Rehacer check-in");

    BtnVolverSelector.setText("Volver selector");

        LblContacto.setText("N° administrativo: +59399458741");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(BtnUndo)
                .addGap(42, 42, 42)
                .addComponent(BtnRedo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(BtnVolverSelector)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LblContacto)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnUndo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtnRedo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BtnVolverSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LblContacto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton BtnCheckin;
    public javax.swing.JButton BtnLimpiarCampos;
    public javax.swing.JButton BtnRedo;
    public javax.swing.JButton BtnUndo;
    public javax.swing.JComboBox<String> CmboxHabitacionReservada;
    public javax.swing.JComboBox<String> CmboxHabitaciones;
    public javax.swing.ButtonGroup GruposBtnMetodoPagos;
    private javax.swing.JLabel JLblHabitacionReservada;
    public javax.swing.JLabel LblCheckApellido;
    public javax.swing.JLabel LblCheckCedula;
    public javax.swing.JLabel LblCheckCedulaAnular;
    public javax.swing.JLabel LblCheckHabitacion;
    public javax.swing.JLabel LblCheckNombre;
    public javax.swing.JLabel LblCheckNombre4;
    public javax.swing.JLabel LblCheckTelefono;
    public javax.swing.JLabel LblContacto;
    public javax.swing.JLabel LblHabitacionDisponible;
    public javax.swing.JLabel LblHabitacionOcupada;
    public javax.swing.JLabel LblMetodoPago;
    public javax.swing.JPanel PanelAnularReserva;
    public javax.swing.JPanel PanelCheckin;
    public javax.swing.JPanel PanelHabitaciones;
    public javax.swing.JRadioButton RBtnMetodoEfectivo;
    public javax.swing.JRadioButton RBtnMetodoTransferencia;
    public javax.swing.JPanel Servicios;
    public javax.swing.JTable TablaHabitacionesDisponibles;
    public javax.swing.JTable TablaHabitacionesOcupadas;
    public javax.swing.JTextField TxtApellido;
    public javax.swing.JTextField TxtBuscarCedulaAnular;
    public javax.swing.JTextField TxtCedula;
    public javax.swing.JTextField TxtNombre;
    public javax.swing.JTextField TxtTelefono;
    public javax.swing.JButton jButton2;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JTabbedPane jTabbedPane2;
    // === Componentes agregados para pestaña Servicios ===
    public javax.swing.JLabel LblCedulaServicio;
    public javax.swing.JTextField TxtCedulaServicio;
    public javax.swing.JButton BtnValidarServicio;
    public javax.swing.JButton BtnSolicitarServicio;
    public javax.swing.JButton BtnRefrescarServicios;
    public javax.swing.JButton BtnVolverServicios;
    public javax.swing.JTable TablaServicios;
    public javax.swing.JScrollPane jScrollPaneServicios;
    // Placeholders de spinners opcionales (evitar errores si aún no existen en el formulario)
    public javax.swing.JSpinner SpinnerDiasHastaLlegada;
    public javax.swing.JSpinner SpinnerNoches;
    public javax.swing.JButton BtnVolverSelector;
    // End of variables declaration//GEN-END:variables
}
