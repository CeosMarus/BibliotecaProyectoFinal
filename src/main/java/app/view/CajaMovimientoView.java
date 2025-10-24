package app.view;

import app.dao.CajaMovimientoDAO;
import app.model.CajaMovimiento;
import app.model.ItemDesplegable; // Clase auxiliar para el JComboBox (asumida)

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat; // Para formatear fechas y horas

/**
 * Vista para registrar movimientos de caja manuales (Ingresos/Egresos) y
 * mostrar el historial de movimientos.
 */
public class CajaMovimientoView extends JPanel {

    // 🛑 ID del usuario logueado. Ajustar según tu sistema de sesión.
    private final int ID_USUARIO_ACTUAL = 1;

    // Formateadores
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private final CajaMovimientoDAO movimientoDAO = new CajaMovimientoDAO();

    // Componentes para Registro
    private JComboBox<ItemDesplegable> cmbTipo;
    private JTextField txtSubtipo;
    private JTextField txtMonto;
    private JTextArea txtDescripcion;
    private JButton btnRegistrar;
    private JButton btnRecargar; // Botón para recargar la tabla

    // Componentes para Historial
    private JTable tablaMovimientos;
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Fecha", "Hora", "Tipo", "Subtipo", "Monto", "Usuario ID", "Descripción"}, 0
    );

    public CajaMovimientoView() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(1000, 600)); // Ajuste de tamaño

        initComponents();
        cargarMovimientos();

        btnRegistrar.addActionListener(e -> registrarMovimiento());
        btnRecargar.addActionListener(e -> cargarMovimientos());

        // Configuración inicial de la tabla
        tablaMovimientos.setModel(modeloTabla);
        tablaMovimientos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Desactivamos la edición directa en la tabla de movimientos
        // (Los movimientos de caja normalmente no se editan después de crearse)
        tablaMovimientos.setEnabled(false);
    }

    private void initComponents() {
        // --- Panel Superior: Registro y Botones ---
        JPanel pnlSuperior = new JPanel(new BorderLayout(10, 10));

        // Panel de Registro (Formulario)
        JPanel pnlRegistro = createRegistroPanel();
        pnlSuperior.add(pnlRegistro, BorderLayout.CENTER);

        // Panel de Botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRecargar = new JButton("Recargar Historial");
        pnlBotones.add(btnRecargar);
        pnlSuperior.add(pnlBotones, BorderLayout.SOUTH);

        add(pnlSuperior, BorderLayout.NORTH);

        // --- Panel de Historial (Centro) ---
        JPanel pnlHistorial = new JPanel(new BorderLayout());
        pnlHistorial.setBorder(BorderFactory.createTitledBorder("Historial de Movimientos de Caja"));

        tablaMovimientos = new JTable(modeloTabla);
        pnlHistorial.add(new JScrollPane(tablaMovimientos), BorderLayout.CENTER);

        add(pnlHistorial, BorderLayout.CENTER);
    }

    private JPanel createRegistroPanel() {
        JPanel pnlRegistro = new JPanel(new GridBagLayout());
        pnlRegistro.setBorder(BorderFactory.createTitledBorder("Registro Manual de Movimiento"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbTipo = new JComboBox<>();
        cmbTipo.addItem(new ItemDesplegable(1, "1 - Ingreso (Entrada de Dinero)"));
        cmbTipo.addItem(new ItemDesplegable(2, "2 - Egreso (Salida de Dinero)"));

        txtSubtipo = new JTextField(20);
        txtMonto = new JTextField(15);
        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setLineWrap(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        btnRegistrar = new JButton("Registrar Movimiento");

        int row = 0;

        // Fila 1: Tipo
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; pnlRegistro.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; pnlRegistro.add(cmbTipo, gbc);

        // Fila 2: Subtipo
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; pnlRegistro.add(new JLabel("Subtipo (Ej. Pago, Proveedor):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; pnlRegistro.add(txtSubtipo, gbc);

        // Fila 3: Monto
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; pnlRegistro.add(new JLabel("Monto:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; pnlRegistro.add(txtMonto, gbc);

        // Fila 4: Descripción
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHWEST; pnlRegistro.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; gbc.weighty = 1.0; pnlRegistro.add(scrollDescripcion, gbc);

        // Fila 5: Botón
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; pnlRegistro.add(btnRegistrar, gbc);

        return pnlRegistro;
    }


    /** * Registra un nuevo movimiento de caja manual (Ingreso/Egreso).
     */
    private void registrarMovimiento() {
        // ... (Se mantiene la lógica de validación y registro) ...
        try {
            ItemDesplegable tipoItem = (ItemDesplegable) cmbTipo.getSelectedItem();
            int tipo = tipoItem.getId();

            String subtipo = txtSubtipo.getText().trim();
            if (subtipo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Subtipo es obligatorio.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String montoText = txtMonto.getText().trim().replace(',', '.');
            if (montoText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Monto es obligatorio.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal monto = new BigDecimal(montoText);
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "El Monto debe ser positivo.", "Error de Lógica", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String descripcion = txtDescripcion.getText().trim();
            Date ahora = new Date();

            CajaMovimiento nuevoMovimiento = new CajaMovimiento(
                    ahora,
                    ahora,
                    tipo,
                    subtipo,
                    monto,
                    ID_USUARIO_ACTUAL,
                    descripcion
            );

            int idGenerado = movimientoDAO.insertar(nuevoMovimiento);

            if (idGenerado > 0) {
                JOptionPane.showMessageDialog(this,
                        "Movimiento registrado con éxito. ID: " + idGenerado,
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);

                limpiarCampos();
                cargarMovimientos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el movimiento.", "Error de Inserción", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error de formato: Ingrese un Monto válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            Logger.getLogger(CajaMovimientoView.class.getName()).log(Level.SEVERE, "Error de BD al registrar movimiento:", ex);
            JOptionPane.showMessageDialog(this, "Error de Base de Datos: " + ex.getMessage(), "Error de BD", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            Logger.getLogger(CajaMovimientoView.class.getName()).log(Level.SEVERE, "Error inesperado:", ex);
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado.", "Error General", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        cmbTipo.setSelectedIndex(0);
        txtSubtipo.setText("");
        txtMonto.setText("");
        txtDescripcion.setText("");
    }

    /**
     * Carga todos los movimientos de caja y actualiza la JTable, similar a cargarTabla en UsuariosForm.
     */
    private void cargarMovimientos() {
        modeloTabla.setRowCount(0); // Limpiar filas

        try {
            // Se lista por fecha descendente, mostrando el historial más reciente primero
            List<CajaMovimiento> movimientos = movimientoDAO.listar();

            for (CajaMovimiento m : movimientos) {
                String tipoStr = (m.getTipo() == 1) ? "INGRESOS (+)" : "EGRESO (-)";

                modeloTabla.addRow(new Object[]{
                        m.getId(),
                        dateFormat.format(m.getFecha()), // Formato de fecha
                        timeFormat.format(m.getHora()),  // Formato de hora
                        tipoStr,
                        m.getSubtipo(),
                        m.getMonto(),
                        m.getIdUsuario(),
                        m.getDescripcion()
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar movimientos: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(CajaMovimientoView.class.getName()).log(Level.SEVERE, "Error al cargar movimientos:", ex);
        }
    }

    // --------------------------------------------------------------------------
    // MÉTODO MAIN PARA PRUEBA INDIVIDUAL
    // --------------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Movimientos de Caja");
            frame.setContentPane(new CajaMovimientoView());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack(); // Se ajusta al preferredSize (1000x600)
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}