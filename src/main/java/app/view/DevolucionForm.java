package app.view;

import app.core.Sesion;
import app.dao.DevolucionDAO;
import app.dao.PrestamoDAO;
import app.dao.ClienteDAO;
import app.dao.EjemplarDAO;
import app.dao.LibroDAO;

import app.model.Devolucion;
import app.model.Prestamo;
import app.model.Usuario;
import app.model.Cliente;
import app.model.Ejemplar;
import app.model.Libro;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DevolucionForm extends JFrame {

    public JPanel panelPrincipal;
    private JComboBox<Prestamo> cboPrestamo;
    private JTextField txtFechaDevolucion;
    private JTable tblDevoluciones;
    //private JScrollPane scrollDevoluciones;
    private JButton btnRegistrar;
    private JButton btnAnular;
    private JButton btnSalir;
    private JComboBox<String> cboEstadoCopia;
    private JTextField txtObservaciones;

    private final DevolucionDAO devolucionDAO = new DevolucionDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final EjemplarDAO ejemplarDAO = new EjemplarDAO();
    private final LibroDAO libroDAO = new LibroDAO();

    public DevolucionForm() {

        setTitle("Registro de Devoluciones");
        setContentPane(panelPrincipal);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        //scrollDevoluciones.setViewportView(tblDevoluciones);

        // Opciones de estado de la copia
        cboEstadoCopia.addItem("Nuevo");
        cboEstadoCopia.addItem("Dañado");
        cboEstadoCopia.addItem("Perdido");

        // Fecha actual bloqueada
        txtFechaDevolucion.setText(new java.sql.Date(new Date().getTime()).toString());
        txtFechaDevolucion.setEditable(false);

        cargarPrestamosPendientes();
        cargarTablaGeneral();

        btnRegistrar.addActionListener(e -> registrarDevolucion());
        btnAnular.addActionListener(e -> anularDevolucion());
        btnSalir.addActionListener(e -> onSalir());
    }

    private void cargarPrestamosPendientes() {
        try {
            cboPrestamo.removeAllItems();
            List<Prestamo> lista = prestamoDAO.listarActivos();

            DefaultComboBoxModel<Prestamo> model = new DefaultComboBoxModel<>();

            for (Prestamo p : lista) {
                Cliente c = clienteDAO.buscarPorId(p.getIdCliente());
                Ejemplar ej = ejemplarDAO.buscarPorId(p.getIdEjemplar());
                Libro l = libroDAO.obtenerPorId(ej.getIdLibro());

                String fechaPrestamo = new java.sql.Date(p.getFechaPrestamo().getTime()).toString();

                String label = String.format(
                        "Préstamo #%d | %s | Libro: %s | Fecha: %s",
                        p.getId(),
                        (c != null ? c.getNombre() : "Cliente#" + p.getIdCliente()),
                        (l != null ? l.getTitulo() : "Ejemplar#" + p.getIdEjemplar()),
                        fechaPrestamo
                );

                Prestamo item = new Prestamo(
                        p.getId(), p.getIdCliente(), p.getIdEjemplar(),
                        p.getFechaPrestamo(), p.getFechaVencimiento(), null, p.getEstado()
                ) {
                    @Override
                    public String toString() {
                        return label;
                    }
                };

                model.addElement(item);
            }

            cboPrestamo.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando préstamos: " + e.getMessage());
        }
    }

    private void onSalir() {
        if (JOptionPane.showConfirmDialog(this, "¿Deseas cerrar el formulario?",
                "Confirmar salida", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (window != null) {
                window.dispose(); // Cierra solo esta ventana
            }
        }
    }

    private void registrarDevolucion() {
        Prestamo p = (Prestamo) cboPrestamo.getSelectedItem();

        if (p == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo.");
            return;
        }

        try {
            Date fecha = java.sql.Date.valueOf(txtFechaDevolucion.getText());
            String estadoCopia = cboEstadoCopia.getSelectedItem().toString();
            String observaciones = txtObservaciones.getText();

            Devolucion d = new Devolucion(
                    p.getId(),
                    fecha,
                    estadoCopia,
                    observaciones,
                    Sesion.getUsuario().getId()
            );

            int id = devolucionDAO.insertar(d);

            if (id > 0) {
                JOptionPane.showMessageDialog(this, "✅ Devolución registrada");
                cargarPrestamosPendientes();
                cargarTablaGeneral();
                txtObservaciones.setText("");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void anularDevolucion() {
        int row = tblDevoluciones.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una devolución para anular");
            return;
        }

        int id = Integer.parseInt(tblDevoluciones.getValueAt(row, 0).toString());
        int opcion = JOptionPane.showConfirmDialog(this, "¿Anular devolución?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                if (devolucionDAO.anular(id)) {
                    JOptionPane.showMessageDialog(this, "✅ Devolución anulada");
                    cargarTablaGeneral();
                    cargarPrestamosPendientes();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al anular: " + e.getMessage());
            }
        }
    }

    private void cargarTablaGeneral() {
        try {
            List<Devolucion> lista = devolucionDAO.listar();
            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new String[]{
                    "ID", "Préstamo", "Cliente", "Libro", "Fecha", "Estado"
            });

            for (Devolucion d : lista) {
                Prestamo p = prestamoDAO.buscarPorId(d.getIdPrestamo());
                Cliente c = clienteDAO.buscarPorId(p.getIdCliente());
                Ejemplar ej = ejemplarDAO.buscarPorId(p.getIdEjemplar());
                Libro l = libroDAO.obtenerPorId(ej.getIdLibro());

                model.addRow(new Object[]{
                        d.getId(),
                        d.getIdPrestamo(),
                        (c != null ? c.getNombre() : "Cliente#" + p.getIdCliente()),
                        (l != null ? l.getTitulo() : "Ejemplar#" + p.getIdEjemplar()),
                        d.getFechaDevolucion(),
                        d.getEstado() == 1 ? "Activo" : "Anulado"
                });
            }

            tblDevoluciones.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando tabla: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new DevolucionForm().setVisible(true);
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
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(8, 2, new Insets(8, 8, 8, 8), -1, -1));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Chalkboard SE", Font.BOLD, 26, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Gestion de Devoluciones");
        panelPrincipal.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Selecciona el Prestamo");
        panelPrincipal.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboPrestamo = new JComboBox();
        panelPrincipal.add(cboPrestamo, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("La Fecha de Devolucion es:");
        panelPrincipal.add(label3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFechaDevolucion = new JTextField();
        panelPrincipal.add(txtFechaDevolucion, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Selecciona El estado de la copia");
        panelPrincipal.add(label4, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboEstadoCopia = new JComboBox();
        panelPrincipal.add(cboEstadoCopia, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Agrega una Observacion sobre el Libro:");
        panelPrincipal.add(label5, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtObservaciones = new JTextField();
        panelPrincipal.add(txtObservaciones, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelPrincipal.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tblDevoluciones = new JTable();
        scrollPane1.setViewportView(tblDevoluciones);
        btnSalir = new JButton();
        btnSalir.setText("Salir");
        panelPrincipal.add(btnSalir, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnRegistrar = new JButton();
        btnRegistrar.setText("Registrar Devolucion");
        panelPrincipal.add(btnRegistrar, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAnular = new JButton();
        btnAnular.setText("Anular Devolucion");
        panelPrincipal.add(btnAnular, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        return panelPrincipal;
    }
}