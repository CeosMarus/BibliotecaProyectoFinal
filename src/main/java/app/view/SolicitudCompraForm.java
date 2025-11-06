package app.view;

import app.dao.LibroDAO;
import app.dao.SolicitudCompraDAO;
import app.dao.UsuarioDAO;
import app.model.Libro;
import app.model.SolicitudCompra;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SolicitudCompraForm {

    public JPanel panelPrincipal;
    private JTextField txtFecha;
    private JTextField txtCantidad;
    private JTextField txtCostoUnitario;
    private JComboBox<Usuario> cboUsuario;
    private JComboBox<Libro> cboLibro;
    private JComboBox<String> cboEstado;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnCargar;
    private JButton btnLimpiar;
    private JButton btnSalir;

    private JTable tbSolicitudes;

    private final SolicitudCompraDAO solicitudDAO = new SolicitudCompraDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LibroDAO libroDAO = new LibroDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Fecha", "Usuario", "Libro", "Cantidad", "Costo Unitario", "Estado"}, 0
    );

    private Integer selectedId = null;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public SolicitudCompraForm() {

        panelPrincipal.setPreferredSize(new Dimension(1000, 450));
        tbSolicitudes.setModel(model);
        tbSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Estados válidos
        cboEstado.addItem("1 - Pendiente");
        cboEstado.addItem("2 - Aprobada");
        cboEstado.addItem("3 - Rechazada");

        cargarUsuariosEnCombo();
        cargarLibrosEnCombo();

        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnCargar.addActionListener(e -> cargarTabla());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnSalir.addActionListener(e -> onSalir());

        tbSolicitudes.getSelectionModel().addListSelectionListener(this::onTableSelection);

        cargarTabla();
    }

    /**
     * ✅ Cargar SOLO usuarios no cliente (Admin/Bibliotecario)
     */
    private void cargarUsuariosEnCombo() {
        try {
            cboUsuario.removeAllItems();
            List<Usuario> lista = usuarioDAO.listar();

            // Filtramos solo ADMIN y BIBLIOTECARIO
            for (Usuario u : lista) {
                if (u.getRol().equalsIgnoreCase("ADMIN") ||
                        u.getRol().equalsIgnoreCase("BIBLIOTECARIO")) {
                    cboUsuario.addItem(u);
                }
            }

            if (cboUsuario.getItemCount() == 0) {
                JOptionPane.showMessageDialog(null, "No hay usuarios administrativos registrados.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar usuarios: " + e.getMessage());
        }
    }

    /**
     * ✅ Cargar libros activos
     */
    private void cargarLibrosEnCombo() {
        try {
            cboLibro.removeAllItems();
            List<Libro> libros = libroDAO.listar();
            for (Libro l : libros) {
                cboLibro.addItem(l);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar libros: " + e.getMessage());
        }
    }

    /**
     * ✅ Guardar solicitud
     */
    private void onGuardar() {
        try {
            Date fecha = parseFecha(txtFecha.getText());
            Usuario usuario = (Usuario) cboUsuario.getSelectedItem();
            Libro libro = (Libro) cboLibro.getSelectedItem();

            if (usuario == null || libro == null) {
                JOptionPane.showMessageDialog(null, "Debe seleccionar un usuario y un libro.");
                return;
            }

            int cantidad = Integer.parseInt(txtCantidad.getText());
            double costoUnitario = Double.parseDouble(txtCostoUnitario.getText());
            int estado = obtenerEstado();

            SolicitudCompra solicitud = new SolicitudCompra(
                    fecha, usuario.getId(), libro.getId(), cantidad, costoUnitario, estado
            );

            if (solicitudDAO.insertar(solicitud)) {
                JOptionPane.showMessageDialog(null, "✅ Solicitud creada correctamente.");
                limpiarFormulario();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(null, "❌ Error al guardar.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    /**
     * ✅ Actualizar
     */
    private void onActualizar() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(null, "Seleccione una solicitud.");
            return;
        }

        try {
            Date fecha = parseFecha(txtFecha.getText());
            Usuario usuario = (Usuario) cboUsuario.getSelectedItem();
            Libro libro = (Libro) cboLibro.getSelectedItem();
            int cantidad = Integer.parseInt(txtCantidad.getText());
            double costoUnitario = Double.parseDouble(txtCostoUnitario.getText());
            int estado = obtenerEstado();

            SolicitudCompra sc = new SolicitudCompra(
                    selectedId, fecha, usuario.getId(), libro.getId(), cantidad, costoUnitario, estado
            );

            if (solicitudDAO.actualizar(sc)) {
                JOptionPane.showMessageDialog(null, "✅ Actualizado correctamente.");
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(null, "❌ Error al actualizar.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    /**
     * ✅ Eliminar (marcar estado 0)
     */
    private void onEliminar() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(null, "Seleccione una solicitud.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Desea eliminar esta solicitud?",
                "Confirmación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (solicitudDAO.cambiarEstado(selectedId, 0)) {
                JOptionPane.showMessageDialog(null, "✅ Eliminada correctamente.");
                limpiarFormulario();
                cargarTabla();
            }
        }
    }

    /**
     * ✅ Llenar tabla
     */
    private void cargarTabla() {
        model.setRowCount(0);
        List<SolicitudCompra> lista = solicitudDAO.listar();

        for (SolicitudCompra s : lista) {
            model.addRow(new Object[]{
                    s.getId(),
                    sdf.format(s.getFecha()),
                    obtenerNombreUsuario(s.getIdUsuario()),
                    obtenerTituloLibro(s.getIdLibro()),
                    s.getCantidad(),
                    s.getCostoUnitario(),
                    s.getEstadoTexto()
            });
        }
    }

    /**
     * ✅ Selección tabla
     */
    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        int row = tbSolicitudes.getSelectedRow();
        if (row == -1) return;

        selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
        txtFecha.setText(model.getValueAt(row, 1).toString());
        txtCantidad.setText(model.getValueAt(row, 4).toString());
        txtCostoUnitario.setText(model.getValueAt(row, 5).toString());

        seleccionarComboUsuario(model.getValueAt(row, 2).toString());
        seleccionarComboLibro(model.getValueAt(row, 3).toString());
        seleccionarEstado(model.getValueAt(row, 6).toString());
    }

    /**
     * ✅ Utilidades
     */
    private String obtenerNombreUsuario(int id) {
        try {
            for (Usuario u : usuarioDAO.listar())
                if (u.getId() == id) return u.getNombre();
        } catch (Exception ignored) {
        }
        return "---";
    }

    private String obtenerTituloLibro(int id) {
        try {
            for (Libro l : libroDAO.listar())
                if (l.getId() == id) return l.getTitulo();
        } catch (Exception ignored) {
        }
        return "---";
    }

    private void seleccionarComboUsuario(String nombre) {
        for (int i = 0; i < cboUsuario.getItemCount(); i++)
            if (cboUsuario.getItemAt(i).getNombre().equals(nombre))
                cboUsuario.setSelectedIndex(i);
    }

    private void seleccionarComboLibro(String titulo) {
        for (int i = 0; i < cboLibro.getItemCount(); i++)
            if (cboLibro.getItemAt(i).getTitulo().equals(titulo))
                cboLibro.setSelectedIndex(i);
    }

    private void seleccionarEstado(String estadoTexto) {
        switch (estadoTexto) {
            case "Pendiente" -> cboEstado.setSelectedIndex(0);
            case "Aprobada" -> cboEstado.setSelectedIndex(1);
            case "Rechazada" -> cboEstado.setSelectedIndex(2);
        }
    }

    private int obtenerEstado() {
        return cboEstado.getSelectedIndex() + 1;
    }

    private Date parseFecha(String texto) throws ParseException {
        return sdf.parse(texto);
    }

    private void limpiarFormulario() {
        txtFecha.setText("");
        txtCantidad.setText("");
        txtCostoUnitario.setText("");
        tbSolicitudes.clearSelection();
        selectedId = null;
    }

    private void onSalir() {
        if (JOptionPane.showConfirmDialog(panelPrincipal, "¿Deseas cerrar el formulario?",
                "Confirmar salida", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (window != null) {
                window.dispose(); // Cierra solo esta ventana
            }
        }
    }

    /**
     * ✅ MAIN de pruebas
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Gestión de Solicitudes de Compra");
            f.setContentPane(new SolicitudCompraForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
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
        panelPrincipal.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(11, 6, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Chalkboard SE", Font.BOLD, 26, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Gestion de Compra de Libro");
        panelPrincipal.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Ingresa la Fecha de compra   (YYYY-MM-DD)");
        panelPrincipal.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFecha = new JTextField();
        panelPrincipal.add(txtFecha, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Ingresa la cantidad que compro");
        panelPrincipal.add(label3, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtCantidad = new JTextField();
        panelPrincipal.add(txtCantidad, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Ingresa el Costo Unitario del producto");
        panelPrincipal.add(label4, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtCostoUnitario = new JTextField();
        panelPrincipal.add(txtCostoUnitario, new com.intellij.uiDesigner.core.GridConstraints(3, 3, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Selecciona el usuario ");
        panelPrincipal.add(label5, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboUsuario = new JComboBox();
        panelPrincipal.add(cboUsuario, new com.intellij.uiDesigner.core.GridConstraints(4, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Selecciona el libro que Compro");
        panelPrincipal.add(label6, new com.intellij.uiDesigner.core.GridConstraints(4, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboLibro = new JComboBox();
        panelPrincipal.add(cboLibro, new com.intellij.uiDesigner.core.GridConstraints(4, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Selecciona el Estado de la compra");
        panelPrincipal.add(label7, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboEstado = new JComboBox();
        panelPrincipal.add(cboEstado, new com.intellij.uiDesigner.core.GridConstraints(5, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(10, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelPrincipal.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(3, 5, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelPrincipal.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tbSolicitudes = new JTable();
        scrollPane1.setViewportView(tbSolicitudes);
        btnGuardar = new JButton();
        btnGuardar.setText("Guardar");
        panelPrincipal.add(btnGuardar, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnActualizar = new JButton();
        btnActualizar.setText("Actualizar");
        panelPrincipal.add(btnActualizar, new com.intellij.uiDesigner.core.GridConstraints(6, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCargar = new JButton();
        btnCargar.setText("Cargar");
        panelPrincipal.add(btnCargar, new com.intellij.uiDesigner.core.GridConstraints(6, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar");
        panelPrincipal.add(btnLimpiar, new com.intellij.uiDesigner.core.GridConstraints(6, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEliminar = new JButton();
        btnEliminar.setText("Eliminar");
        panelPrincipal.add(btnEliminar, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSalir = new JButton();
        btnSalir.setText("Salir");
        panelPrincipal.add(btnSalir, new com.intellij.uiDesigner.core.GridConstraints(9, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
