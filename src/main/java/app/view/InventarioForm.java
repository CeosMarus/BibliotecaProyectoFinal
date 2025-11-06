package app.view;

import app.dao.BitacoraInventarioDAO;
import app.dao.EjemplarDAO;
import app.dao.InventarioFisicoDAO;
import app.dao.UsuarioDAO;
import app.model.BitacoraInventario;
import app.model.Ejemplar;
import app.model.InventarioFisico;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class InventarioForm extends JFrame {

    private JPanel mainPanel;

    private JTextField txtFecha;
    private JComboBox<Usuario> cbResponsable;
    private JTextArea txtObservaciones;

    private JComboBox<String> comboEjemplar;
    private JTextArea txtDiferencia;
    private JTextArea txtAccion;
    private JButton btnRegistrarDiferencia;

    private JTable tablaInventarios;
    private JTable tablaBitacora;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnAnular;
    private JButton btnLimpiar;
    private JButton btnBuscar;
    private JButton btnVerTodos;
    private JButton btnSalir;

    private int idInventarioSeleccionado = -1;
    private InventarioFisicoDAO inventarioDAO;
    private BitacoraInventarioDAO bitacoraDAO;
    private EjemplarDAO ejemplarDAO;

    public InventarioForm() {
        setTitle("Gestión de Inventario Físico");
        setContentPane(mainPanel);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        inventarioDAO = new InventarioFisicoDAO();
        bitacoraDAO = new BitacoraInventarioDAO();
        ejemplarDAO = new EjemplarDAO();

        inicializarFormulario();

        btnGuardar.addActionListener(this::guardarInventario);
        btnActualizar.addActionListener(this::actualizarInventario);
        btnAnular.addActionListener(this::anularInventario);
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRegistrarDiferencia.addActionListener(this::registrarDiferencia);
        btnBuscar.addActionListener(this::buscarPorFecha);
        btnVerTodos.addActionListener(e -> cargarInventarios());
        btnSalir.addActionListener(e -> onSalir());

        tablaInventarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccionInventario();
        });
    }

    private void inicializarFormulario() {
        configurarTablas();
        cargarResponsables();  // ✅ cargar administradores y bibliotecarios
        cargarEjemplares();
        cargarInventarios();
        establecerFechaActual();
    }

    private void cargarResponsables() {
        try {
            UsuarioDAO udao = new UsuarioDAO();
            List<Usuario> lista = udao.listarEncargadosInventario();

            cbResponsable.removeAllItems();
            for (Usuario u : lista) cbResponsable.addItem(u);

            cbResponsable.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {

                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Usuario u) {
                        setText(u.getNombre() + " (" + u.getRol() + ")");
                    }
                    return this;
                }
            });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando responsables: " + ex.getMessage());
        }
    }

    private void configurarTablas() {
        tablaInventarios.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Fecha", "Responsable", "Usuario", "Estado", "Observaciones"}
        ));

        tablaBitacora.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Ejemplar", "Libro", "Diferencia", "Acción", "Fecha"}
        ));
    }

    private void cargarEjemplares() {
        comboEjemplar.removeAllItems();

        try {
            List<Ejemplar> ejemplares = ejemplarDAO.listarConLibro();
            for (Ejemplar e : ejemplares) {
                comboEjemplar.addItem(e.getId() + " - " + e.getCodigoInventario() + " (" + e.getLibroNombre() + ")");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar ejemplares: " + ex.getMessage());
        }
    }

    private void cargarInventarios() {
        DefaultTableModel modelo = (DefaultTableModel) tablaInventarios.getModel();
        modelo.setRowCount(0);

        List<InventarioFisico> inventarios = inventarioDAO.listar();

        for (InventarioFisico inv : inventarios) {
            modelo.addRow(new Object[]{
                    inv.getId(),
                    inv.getFecha(),
                    inv.getResponsable(),
                    inv.getNombreUsuario(),
                    inv.getEstadoDescripcion(),
                    inv.getObservaciones()
            });
        }
    }

    private void establecerFechaActual() {
        txtFecha.setText(LocalDate.now().toString());
    }

    private void guardarInventario(ActionEvent evt) {
        try {
            if (cbResponsable.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un responsable");
                return;
            }

            Usuario responsableSel = (Usuario) cbResponsable.getSelectedItem();
            String responsable = responsableSel.getNombre();
            Integer idUsuario = responsableSel.getId();

            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            String observaciones = txtObservaciones.getText().trim();

            InventarioFisico inventario = new InventarioFisico(fecha, responsable, idUsuario, observaciones);
            int idGenerado = inventarioDAO.insertar(inventario);

            if (idGenerado > 0) {
                JOptionPane.showMessageDialog(this, "✅ Inventario guardado correctamente.\nID: " + idGenerado);
                limpiarCampos();
                cargarInventarios();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void actualizarInventario(ActionEvent evt) {
        if (idInventarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un inventario");
            return;
        }

        try {
            Usuario responsableSel = (Usuario) cbResponsable.getSelectedItem();
            String responsable = responsableSel.getNombre();
            Integer idUsuario = responsableSel.getId();

            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            String observaciones = txtObservaciones.getText().trim();

            InventarioFisico inventario = new InventarioFisico(
                    idInventarioSeleccionado, fecha, responsable, idUsuario, observaciones, 1
            );

            if (inventarioDAO.actualizar(inventario)) {
                JOptionPane.showMessageDialog(this, "✅ Inventario actualizado");
                limpiarCampos();
                cargarInventarios();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void anularInventario(ActionEvent evt) {
        if (idInventarioSeleccionado == -1) return;

        if (JOptionPane.showConfirmDialog(this, "¿Anular inventario?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (inventarioDAO.anular(idInventarioSeleccionado)) {
                JOptionPane.showMessageDialog(this, "✅ Anulado correctamente");
                limpiarCampos();
                cargarInventarios();
            }
        }
    }

    private void registrarDiferencia(ActionEvent evt) {
        if (idInventarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un inventario primero");
            return;
        }

        if (comboEjemplar.getSelectedItem() == null) return;

        int idEjemplar = Integer.parseInt(comboEjemplar.getSelectedItem().toString().split(" - ")[0]);

        BitacoraInventario bit = new BitacoraInventario(
                idInventarioSeleccionado,
                idEjemplar,
                txtDiferencia.getText().trim(),
                txtAccion.getText().trim()
        );

        bitacoraDAO.insertar(bit);
        cargarBitacora(idInventarioSeleccionado);
        limpiarCamposDiferencia();
    }

    private void cargarBitacora(int idInventario) {
        DefaultTableModel modelo = (DefaultTableModel) tablaBitacora.getModel();
        modelo.setRowCount(0);

        List<BitacoraInventario> bitacoras = bitacoraDAO.listarPorInventario(idInventario);
        for (BitacoraInventario b : bitacoras) {
            modelo.addRow(new Object[]{
                    b.getId(), b.getCodigoInventario(), b.getTituloLibro(),
                    b.getDiferencia(), b.getAccionCorrectiva(), b.getFechaRegistro()
            });
        }
    }

    private void limpiarCampos() {
        idInventarioSeleccionado = -1;
        establecerFechaActual();
        cbResponsable.setSelectedIndex(0);
        txtObservaciones.setText("");
        limpiarCamposDiferencia();
        tablaInventarios.clearSelection();
        ((DefaultTableModel) tablaBitacora.getModel()).setRowCount(0);
    }

    private void limpiarCamposDiferencia() {
        comboEjemplar.setSelectedIndex(0);
        txtDiferencia.setText("");
        txtAccion.setText("");
    }

    private void cargarSeleccionInventario() {
        int fila = tablaInventarios.getSelectedRow();
        if (fila == -1) return;

        idInventarioSeleccionado = (int) tablaInventarios.getValueAt(fila, 0);
        txtFecha.setText(tablaInventarios.getValueAt(fila, 1).toString());

        String responsable = tablaInventarios.getValueAt(fila, 2).toString();
        for (int i = 0; i < cbResponsable.getItemCount(); i++) {
            Usuario u = cbResponsable.getItemAt(i);
            if (u.getNombre().equals(responsable)) {
                cbResponsable.setSelectedIndex(i);
                break;
            }
        }

        txtObservaciones.setText(
                tablaInventarios.getValueAt(fila, 5) != null ?
                        tablaInventarios.getValueAt(fila, 5).toString() : ""
        );

        cargarBitacora(idInventarioSeleccionado);
    }

    private void buscarPorFecha(ActionEvent evt) {
        JTextField txtInicio = new JTextField(LocalDate.now().minusMonths(1).toString());
        JTextField txtFin = new JTextField(LocalDate.now().toString());

        JPanel p = new JPanel();
        p.add(new JLabel("Inicio:"));
        p.add(txtInicio);
        p.add(new JLabel("Fin:"));
        p.add(txtFin);

        if (JOptionPane.showConfirmDialog(this, p, "Buscar por fecha", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                LocalDate i = LocalDate.parse(txtInicio.getText());
                LocalDate f = LocalDate.parse(txtFin.getText());

                List<InventarioFisico> inventarios = inventarioDAO.buscarPorRangoFechas(i, f);
                DefaultTableModel modelo = (DefaultTableModel) tablaInventarios.getModel();
                modelo.setRowCount(0);

                for (InventarioFisico inv : inventarios) {
                    modelo.addRow(new Object[]{
                            inv.getId(), inv.getFecha(), inv.getResponsable(),
                            inv.getNombreUsuario(), inv.getEstadoDescripcion(), inv.getObservaciones()
                    });
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Fecha inválida");
            }
        }
    }

    private void onSalir() {
        if (JOptionPane.showConfirmDialog(this, "¿Deseas cerrar el formulario?",
                "Confirmar salida", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) {
                window.dispose(); // Cierra solo esta ventana
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new InventarioForm().setVisible(true);
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(19, 6, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Chalkboard SE", Font.BOLD, 26, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Gestion de Inventario Fisico");
        mainPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(12, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$("Chalkboard SE", Font.BOLD, 16, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Datos del Inventario:");
        mainPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Inserta la Fecha (AAAA-MM-DD)");
        mainPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFecha = new JTextField();
        mainPanel.add(txtFecha, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Responsable");
        mainPanel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Observaciones");
        mainPanel.add(label5, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtObservaciones = new JTextArea();
        mainPanel.add(txtObservaciones, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JSeparator separator1 = new JSeparator();
        mainPanel.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$("Chalkboard SE", Font.BOLD, 16, label6.getFont());
        if (label6Font != null) label6.setFont(label6Font);
        label6.setText("Registrar Diferencias Encontradas");
        mainPanel.add(label6, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Selecciona un Ejemplar");
        mainPanel.add(label7, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboEjemplar = new JComboBox();
        mainPanel.add(comboEjemplar, new com.intellij.uiDesigner.core.GridConstraints(6, 2, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Ingresasr Diferencias en el producto");
        mainPanel.add(label8, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtDiferencia = new JTextArea();
        mainPanel.add(txtDiferencia, new com.intellij.uiDesigner.core.GridConstraints(7, 2, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Accion Correctiva: ");
        mainPanel.add(label9, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAccion = new JTextArea();
        mainPanel.add(txtAccion, new com.intellij.uiDesigner.core.GridConstraints(8, 2, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        btnRegistrarDiferencia = new JButton();
        btnRegistrarDiferencia.setText("Guardar Diferencia");
        mainPanel.add(btnRegistrarDiferencia, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        Font label10Font = this.$$$getFont$$$("Chalkboard SE", Font.BOLD, 16, label10.getFont());
        if (label10Font != null) label10.setFont(label10Font);
        label10.setText("Lista de Inventarios Realizados");
        mainPanel.add(label10, new com.intellij.uiDesigner.core.GridConstraints(10, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Bitácora de Diferencias (Inventario Seleccionado)");
        mainPanel.add(label11, new com.intellij.uiDesigner.core.GridConstraints(13, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnGuardar = new JButton();
        btnGuardar.setText("Guardar Inventario");
        mainPanel.add(btnGuardar, new com.intellij.uiDesigner.core.GridConstraints(15, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnActualizar = new JButton();
        btnActualizar.setText("Actualizar Dato");
        mainPanel.add(btnActualizar, new com.intellij.uiDesigner.core.GridConstraints(15, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnBuscar = new JButton();
        btnBuscar.setText("Buscar Por Fecha");
        mainPanel.add(btnBuscar, new com.intellij.uiDesigner.core.GridConstraints(16, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAnular = new JButton();
        btnAnular.setText("Anular");
        mainPanel.add(btnAnular, new com.intellij.uiDesigner.core.GridConstraints(15, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar ");
        mainPanel.add(btnLimpiar, new com.intellij.uiDesigner.core.GridConstraints(15, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnVerTodos = new JButton();
        btnVerTodos.setText("Ver Todos los Registros");
        mainPanel.add(btnVerTodos, new com.intellij.uiDesigner.core.GridConstraints(16, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSalir = new JButton();
        btnSalir.setText("Salir");
        mainPanel.add(btnSalir, new com.intellij.uiDesigner.core.GridConstraints(17, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(3, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(18, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cbResponsable = new JComboBox();
        mainPanel.add(cbResponsable, new com.intellij.uiDesigner.core.GridConstraints(2, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(11, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tablaInventarios = new JTable();
        scrollPane1.setViewportView(tablaInventarios);
        final JScrollPane scrollPane2 = new JScrollPane();
        mainPanel.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(14, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tablaBitacora = new JTable();
        scrollPane2.setViewportView(tablaBitacora);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
