package app.view;

import app.dao.ClienteDAO;
import app.dao.EjemplarDAO;
import app.dao.PrestamoDAO;
import app.model.Cliente;
import app.model.Ejemplar;
import app.model.Prestamo;
import app.model.Libro;
import app.dao.LibroDAO;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrestamosForm extends JFrame {

    public JPanel panelPrincipal;
    private JComboBox<Cliente> cbCliente;
    private JComboBox<Ejemplar> cbEjemplar;
    private JButton btnGuardar;
    private JButton btnEliminar;
    private JButton btnActualizar;
    private JButton btnLimpiar;
    private JTable tblPrestamos;
    private JButton btnRegresar;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final EjemplarDAO ejemplarDAO = new EjemplarDAO();

    private DefaultTableModel modelo;

    public PrestamosForm() {
        setTitle("Gestión de Préstamos");
        setContentPane(panelPrincipal);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        configurarTabla();
        cargarClientes();
        cargarEjemplares();
        listarPrestamos();

        // Botón Guardar
        btnGuardar.addActionListener(e -> guardarPrestamo());

        // Botón Actualizar
        btnActualizar.addActionListener(e -> actualizarPrestamo());

        // Botón Eliminar (Lógica)
        btnEliminar.addActionListener(e -> eliminarPrestamo());

        // Botón Limpiar
        btnLimpiar.addActionListener(e -> limpiarCampos());

        // Botón Regresar
        btnRegresar.addActionListener(e -> onSalir());
    }

    //Configura tabla
    private void configurarTabla() {

        modelo = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "ID Cliente",
                        "ID Ejemplar",
                        "Fecha Préstamo",
                        "Fecha Vencimiento",
                        "Fecha Devolución",
                        "Estado"
                }, 0
        );
        tblPrestamos.setModel(modelo);
        tblPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // Cargar clientes activos y mostrar "Nombre - NIT"
    private void cargarClientes() {
        try {
            cbCliente.removeAllItems();
            List<Cliente> clientes = clienteDAO.listar();

            for (Cliente c : clientes) {
                if (c.getEstado() == 1) cbCliente.addItem(c);
            }

            cbCliente.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {

                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                    if (value instanceof Cliente cliente) {
                        setText(cliente.getNombre() + " - NIT: " + cliente.getNit());
                    } else if (value == null) {
                        setText("");
                    }
                    return this;
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cargar ejemplares activos
    private void cargarEjemplares() {
        try {
            cbEjemplar.removeAllItems();
            List<Ejemplar> ejemplares = ejemplarDAO.listarConLibro();
            for (Ejemplar e : ejemplares) {
                if (e.getEstado() == 1) cbEjemplar.addItem(e);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar ejemplares: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Listar préstamos
    private void listarPrestamos() {
        try {
            modelo.setRowCount(0);
            List<Prestamo> lista = prestamoDAO.listar();

            for (Prestamo p : lista) {
                // Cliente
                Cliente cli = clienteDAO.buscarPorId(p.getIdCliente());
                String clienteNombre = (cli != null) ? cli.getNombre() : ("Cliente #" + p.getIdCliente());

                // Ejemplar + Libro
                Ejemplar ej = ejemplarDAO.buscarPorId(p.getIdEjemplar());
                String libroTitulo = "Ejemplar #" + p.getIdEjemplar();
                if (ej != null && ej.getIdLibro() > 0) {
                    Libro libro = new LibroDAO().obtenerPorId(ej.getIdLibro());
                    if (libro != null) libroTitulo = libro.getTitulo();
                }

                modelo.addRow(new Object[]{
                        p.getId(),
                        clienteNombre,
                        libroTitulo,
                        p.getFechaPrestamo(),
                        p.getFechaVencimiento(),
                        p.getFechaDevolucion() != null ? p.getFechaDevolucion() : "",
                        p.getEstadoDescripcion()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al listar préstamos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Guardar préstamo (corregido para usar constructor válido)
    private void guardarPrestamo() {
        try {
            Cliente cliente = (Cliente) cbCliente.getSelectedItem();
            Ejemplar ejemplar = (Ejemplar) cbEjemplar.getSelectedItem();

            if (cliente == null || ejemplar == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un cliente y un ejemplar.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date fechaPrestamo = new Date();
            Date fechaVencimiento = new Date(fechaPrestamo.getTime() + (7L * 24 * 60 * 60 * 1000)); // +7 días
            Date fechaDevolucion = new Date(fechaPrestamo.getTime() + (7L * 24 * 60 * 60 * 1000)); // +7 días (prevista)

            // ✅ Usa el constructor existente de tu modelo:
            Prestamo p = new Prestamo(
                    cliente.getId(),
                    ejemplar.getId(),
                    fechaPrestamo,
                    fechaVencimiento,
                    1 // estado
            );

            // ✅ y setea la fechaDevolucion prevista:
            p.setFechaDevolucion(fechaDevolucion);

            prestamoDAO.insertar(p);
            JOptionPane.showMessageDialog(this, "Préstamo guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            listarPrestamos();
            limpiarCampos();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar préstamo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Actualizar préstamo
    private void actualizarPrestamo() {
        int fila = tblPrestamos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = (int) tblPrestamos.getValueAt(fila, 0);
            Prestamo prestamo = prestamoDAO.buscarPorId(id);
            if (prestamo == null) {
                JOptionPane.showMessageDialog(this, "Préstamo no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea marcar este préstamo como devuelto?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmar == JOptionPane.YES_OPTION) {
                boolean ok = prestamoDAO.devolver(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Préstamo devuelto correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    listarPrestamos();
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar préstamo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Eliminar (desactivar)
    private void eliminarPrestamo() {
        int fila = tblPrestamos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo para eliminar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tblPrestamos.getValueAt(fila, 0);
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea desactivar este préstamo?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                boolean ok = prestamoDAO.eliminar(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Préstamo desactivado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    listarPrestamos();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar préstamo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Limpiar campos
    private void limpiarCampos() {
        cbCliente.setSelectedIndex(-1);
        cbEjemplar.setSelectedIndex(-1);
        tblPrestamos.clearSelection();
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

    // MAIN (para pruebas independientes)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrestamosForm().setVisible(true));
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
        panelPrincipal.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 5, new Insets(8, 8, 8, 8), -1, -1));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Chalkboard SE", Font.BOLD, 26, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Gestion de Prestamos");
        panelPrincipal.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Selecciona al Cliente");
        panelPrincipal.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbCliente = new JComboBox();
        panelPrincipal.add(cbCliente, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnGuardar = new JButton();
        btnGuardar.setText("Guardar");
        panelPrincipal.add(btnGuardar, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnActualizar = new JButton();
        btnActualizar.setText("Actualizar");
        panelPrincipal.add(btnActualizar, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar");
        panelPrincipal.add(btnLimpiar, new com.intellij.uiDesigner.core.GridConstraints(3, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEliminar = new JButton();
        btnEliminar.setText("Eliminar");
        panelPrincipal.add(btnEliminar, new com.intellij.uiDesigner.core.GridConstraints(3, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Seleccina al Ejemplar");
        panelPrincipal.add(label3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbEjemplar = new JComboBox();
        panelPrincipal.add(cbEjemplar, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelPrincipal.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tblPrestamos = new JTable();
        scrollPane1.setViewportView(tblPrestamos);
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 5, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnRegresar = new JButton();
        btnRegresar.setText("Salir");
        panelPrincipal.add(btnRegresar, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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