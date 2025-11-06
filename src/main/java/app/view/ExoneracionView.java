package app.view;

import app.dao.MultaDAO;
import app.model.Multa;
import app.model.MultaDetalle;
import app.model.ItemDesplegable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols; // 🆕 Importación para configurar el formato
import java.text.ParseException; // 🆕 Importación obligatoria
import java.util.Date;
import java.util.List;
import java.util.Locale; // 🆕 Importación para configurar el formato
import java.util.logging.Level;
import java.util.logging.Logger;


public class ExoneracionView extends JPanel {

    // ID del usuario logueado.
    private final int ID_USUARIO_ACTUAL = 1;

    private final MultaDAO multaDAO = new MultaDAO();
    // 🆕 Configuración segura del formato: USANDO PUNTO DECIMAL y COMA separador de miles
    private final DecimalFormat currencyFormat;

    // Componentes de la Tabla
    private JTable tablaMultasPendientes;
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID Multa", "Cliente", "Monto Pendiente", "Días Atraso"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private JButton btnRecargar;

    // Componentes de Exoneración
    private JTextField txtMontoExonerar;
    private JCheckBox chkExoneracionTotal;
    private JTextArea txtJustificacion;
    private JButton btnProcesarExoneracion;
    private JLabel lblMultaSeleccionada;

    // Componentes de Búsqueda
    private JTextField txtBuscarNombre;
    private JButton btnBuscar;

    public ExoneracionView() {

        // 🆕 INICIALIZACIÓN DEL FORMATO: Asegura el uso de punto como decimal
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "GT"));
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        currencyFormat = new DecimalFormat("#,##0.00", symbols);

        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(1000, 600));

        initComponents();
        cargarTablaMultasPendientes();

        btnRecargar.addActionListener(e -> cargarTablaMultasPendientes());
        btnBuscar.addActionListener(e -> buscarMultaPorNombreCliente(txtBuscarNombre.getText().trim()));
        btnProcesarExoneracion.addActionListener(e -> onProcesarExoneracion());

        // Listener para la tabla: actualiza el formulario de exoneración al seleccionar
        tablaMultasPendientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarPanelExoneracion();
            }
        });

        // Listener para la casilla de Exoneración Total: llena el campo de monto con el total
        chkExoneracionTotal.addActionListener(e -> {
            int row = tablaMultasPendientes.getSelectedRow();
            if (row != -1) {
                String montoStr = (String) modeloTabla.getValueAt(row, 2);
                if (chkExoneracionTotal.isSelected()) {
                    txtMontoExonerar.setText(montoStr.replace(",", "")); // Quitar comas al pasar a campo
                    txtMontoExonerar.setEnabled(false);
                } else {
                    txtMontoExonerar.setText("");
                    txtMontoExonerar.setEnabled(true);
                }
            } else {
                // Si no hay fila seleccionada, desmarcar
                chkExoneracionTotal.setSelected(false);
            }
        });

        // Inicializar
        btnProcesarExoneracion.setEnabled(false);
    }

    private void initComponents() {
        // --- Título ---
        JLabel lblTitulo = new JLabel("Gestión de Exoneraciones de Multas", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(lblTitulo, BorderLayout.NORTH);

        // --- Panel Central ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65); // 65% para la tabla

        // A. Panel de la Tabla (Izquierda) - Incluye Búsqueda
        JPanel pnlTabla = createTablaPanel();
        splitPane.setLeftComponent(pnlTabla);

        // B. Panel de Exoneración (Derecha) - Diseño corregido
        JPanel pnlExoneracion = createExoneracionPanel();
        splitPane.setRightComponent(pnlExoneracion);

        add(splitPane, BorderLayout.CENTER);
    }

    // Diseño del panel de la tabla con búsqueda
    private JPanel createTablaPanel() {
        JPanel pnlTabla = new JPanel(new BorderLayout(5, 5));
        pnlTabla.setBorder(BorderFactory.createTitledBorder("Multas Pendientes Activas"));

        tablaMultasPendientes = new JTable(modeloTabla);
        tablaMultasPendientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaMultasPendientes.setAutoCreateRowSorter(true);

        // --- Panel de Búsqueda y Recarga ---
        JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        txtBuscarNombre = new JTextField(15);
        btnBuscar = new JButton("Buscar Cliente");
        btnRecargar = new JButton("Mostrar Todas");

        pnlControl.add(new JLabel("Buscar por Cliente:"));
        pnlControl.add(txtBuscarNombre);
        pnlControl.add(btnBuscar);
        pnlControl.add(btnRecargar);

        pnlTabla.add(pnlControl, BorderLayout.NORTH);
        pnlTabla.add(new JScrollPane(tablaMultasPendientes), BorderLayout.CENTER);

        return pnlTabla;
    }

    // Diseño del panel de exoneración usando BoxLayout (soluciona el bug de diseño)
    private JPanel createExoneracionPanel() {
        JPanel pnlForm = new JPanel();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setBorder(BorderFactory.createTitledBorder("Procesar Exoneración"));

        // Componentes de Exoneración
        txtMontoExonerar = new JTextField(10);
        txtMontoExonerar.setMaximumSize(new Dimension(Integer.MAX_VALUE, txtMontoExonerar.getPreferredSize().height));

        chkExoneracionTotal = new JCheckBox("Exoneración Total (Monto = Monto Pendiente)");
        txtJustificacion = new JTextArea(5, 20);
        txtJustificacion.setLineWrap(true);
        txtJustificacion.setWrapStyleWord(true);
        JScrollPane scrollJustificacion = new JScrollPane(txtJustificacion);
        btnProcesarExoneracion = new JButton("Procesar Exoneración");
        lblMultaSeleccionada = new JLabel("Seleccione una multa de la izquierda.");
        lblMultaSeleccionada.setFont(new Font("Arial", Font.BOLD, 12));

        // Alineación central y padding
        pnlForm.add(Box.createVerticalStrut(10));

        // Sección 1: Multa Seleccionada
        pnlForm.add(new JLabel("Multa Seleccionada:"));
        pnlForm.add(lblMultaSeleccionada);
        pnlForm.add(Box.createVerticalStrut(15));

        // Sección 2: Monto
        pnlForm.add(new JLabel("Monto a Exonerar:"));
        pnlForm.add(txtMontoExonerar);
        pnlForm.add(chkExoneracionTotal);
        pnlForm.add(Box.createVerticalStrut(15));

        // Sección 3: Justificación
        pnlForm.add(new JLabel("Justificación Obligatoria:"));
        pnlForm.add(scrollJustificacion);
        pnlForm.add(Box.createVerticalStrut(15));

        // Sección 4: Botón
        JPanel pnlBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBoton.add(btnProcesarExoneracion);
        pnlForm.add(pnlBoton);

        pnlForm.add(Box.createVerticalGlue());

        return pnlForm;
    }

    private void actualizarPanelExoneracion() {
        int row = tablaMultasPendientes.getSelectedRow();
        if (row == -1) {
            lblMultaSeleccionada.setText("Seleccione una multa de la izquierda.");
            btnProcesarExoneracion.setEnabled(false);
            limpiarFormularioExoneracion();
            return;
        }

        int idMulta = (Integer) modeloTabla.getValueAt(row, 0);
        String monto = (String) modeloTabla.getValueAt(row, 2);

        lblMultaSeleccionada.setText("ID Multa: " + idMulta + " (Monto: " + monto + ")");
        btnProcesarExoneracion.setEnabled(true);
        // Limpiar campos relacionados con la selección
        txtMontoExonerar.setText("");
        chkExoneracionTotal.setSelected(false);
        txtMontoExonerar.setEnabled(true);
        txtJustificacion.setText("");
    }

    /**
     * Carga la tabla con todas las multas pendientes usando MultaDetalle.
     */
    private void cargarTablaMultasPendientes() {
        modeloTabla.setRowCount(0);
        try {
            List<MultaDetalle> multasPendientes = multaDAO.listarPendientesConDetalles();

            for (MultaDetalle m : multasPendientes) {
                modeloTabla.addRow(new Object[]{
                        m.getId(),
                        m.getNombreCliente(),
                        currencyFormat.format(m.getMonto()),
                        m.getDiasAtraso()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar multas pendientes: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ExoneracionView.class.getName()).log(Level.SEVERE, "Error al cargar multas:", ex);
        }
    }

    /**
     * Método para buscar multas pendientes por nombre de cliente (coincidencia parcial).
     */
    private void buscarMultaPorNombreCliente(String nombre) {
        if (nombre.isEmpty()) {
            cargarTablaMultasPendientes();
            return;
        }

        modeloTabla.setRowCount(0);
        try {
            // Se asume que MultaDAO tiene un método buscarPorNombreCliente que devuelve MultaDetalle
            List<MultaDetalle> multasEncontradas = multaDAO.buscarPorNombreCliente(nombre).stream()
                    .filter(m -> m.getEstadoPago() == 0 && m.getEstado() == 1)
                    .toList();

            if (multasEncontradas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron multas pendientes para el cliente: " + nombre, "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
                cargarTablaMultasPendientes();
                return;
            }

            for (MultaDetalle m : multasEncontradas) {
                modeloTabla.addRow(new Object[]{
                        m.getId(),
                        m.getNombreCliente(),
                        currencyFormat.format(m.getMonto()),
                        m.getDiasAtraso()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar multas por nombre: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ExoneracionView.class.getName()).log(Level.SEVERE, "Error al buscar multas:", ex);
        }
    }


    private void onProcesarExoneracion() {
        int fila = tablaMultasPendientes.getSelectedRow();
        if (fila == -1) return;

        int idMulta = (Integer) modeloTabla.getValueAt(fila, 0);

        // 1. OBTENER MONTO PENDIENTE DE LA TABLA (Requiere manejo de ParseException)
        String montoPendienteStr = (String) modeloTabla.getValueAt(fila, 2);
        BigDecimal montoPendiente;
        try {
            // 🆕 Uso de parse() y manejo de ParseException
            montoPendiente = new BigDecimal(currencyFormat.parse(montoPendienteStr).toString());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Error de formato interno. No se pudo leer el monto: " + montoPendienteStr, "Error Interno", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(ExoneracionView.class.getName()).log(Level.SEVERE, "Error al parsear monto pendiente de la tabla: " + montoPendienteStr, e);
            return;
        }

        String justificacion = txtJustificacion.getText().trim();
        // Al obtener el texto de la caja, es mejor limpiarlo de separadores de miles
        String montoExonerarText = txtMontoExonerar.getText().trim().replace(',', '.');

        if (justificacion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La justificación es obligatoria para la exoneración.", "Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (montoExonerarText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el monto a exonerar o marcar Exoneración Total.", "Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 2. OBTENER MONTO A EXONERAR DEL CAMPO DE TEXTO
            BigDecimal montoExonerar = new BigDecimal(montoExonerarText);

            // Validaciones
            if (montoExonerar.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "El monto a exonerar debe ser mayor a cero.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (montoExonerar.compareTo(montoPendiente) > 0) {
                JOptionPane.showMessageDialog(this, "El monto a exonerar no puede ser mayor al monto pendiente (" + currencyFormat.format(montoPendiente) + ").", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirmación final
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Confirma la exoneración de " + currencyFormat.format(montoExonerar) + " a la Multa ID [" + idMulta + "]? La multa se modificará.",
                    "Confirmar Exoneración", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // ... (El resto de la lógica de actualización sigue igual)
                Multa multa = multaDAO.buscarPorId(idMulta);
                if (multa == null) {
                    JOptionPane.showMessageDialog(this, "Error: No se encontró la multa en la base de datos.", "Error BD", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BigDecimal nuevoMonto = montoPendiente.subtract(montoExonerar);

                multa.setMonto(nuevoMonto);

                String observacionAnterior = (multa.getObservaciones() != null) ? multa.getObservaciones() : "Sin observaciones previas.";

                if (nuevoMonto.compareTo(BigDecimal.ZERO) <= 0) {
                    multa.setEstadoPago(1);
                    multa.setFechaPago(new Date());
                    multa.setObservaciones(observacionAnterior + " | [EXONERACIÓN TOTAL] Justificación: " + justificacion + " (Usuario ID: " + ID_USUARIO_ACTUAL + ")");
                } else {
                    multa.setObservaciones(observacionAnterior + " | [EXONERACIÓN PARCIAL " + currencyFormat.format(montoExonerar) + "] Justificación: " + justificacion + " (Usuario ID: " + ID_USUARIO_ACTUAL + ")");
                }

                boolean ok = multaDAO.actualizar(multa);

                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Exoneración procesada. Nuevo monto pendiente: " + currencyFormat.format(nuevoMonto),
                            "Proceso Exitoso", JOptionPane.INFORMATION_MESSAGE);

                    limpiarFormularioExoneracion();
                    cargarTablaMultasPendientes();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar la multa después de la exoneración.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error de formato: Ingrese un Monto a Exonerar válido (solo números y punto decimal).", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            Logger.getLogger(ExoneracionView.class.getName()).log(Level.SEVERE, "Error al procesar exoneración:", ex);
            JOptionPane.showMessageDialog(this, "Error de BD: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormularioExoneracion() {
        lblMultaSeleccionada.setText("Seleccione una multa de la izquierda.");
        txtMontoExonerar.setText("");
        txtJustificacion.setText("");
        chkExoneracionTotal.setSelected(false);
        txtMontoExonerar.setEnabled(true);
        btnProcesarExoneracion.setEnabled(false);
        tablaMultasPendientes.clearSelection();
    }

    // --------------------------------------------------------------------------
    // MÉTODO MAIN PARA PRUEBA INDIVIDUAL
    // --------------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gestión de Exoneraciones");
            frame.setContentPane(new ExoneracionView());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    }
}