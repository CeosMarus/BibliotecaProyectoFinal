package app.view;

import app.dao.AuditoriaDAO;
import app.dao.UsuarioDAO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.List;


public class AuditoriaForm extends JFrame {
    private JComboBox<String> cboFiltro;
    private JLabel infoNombre;
    private JTextField txtNombre;
    private JTable tblAuditoria;
    private JButton btnCargar;
    public JPanel panelPrincipal;
    private JComboBox cboOpcion;
    private JButton btnLimpiar;
    private JButton btnSalir;

    //Definimos la estructura de nuestra tabla
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Fecha y Hora 📅 ", "Usuario 👤", "Rol", "Modulo", "Accion", "Detalle"}, 0
    ) { //Evitamos que las celdas sean editables
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // ninguna celda editable
        }
    };

    public AuditoriaForm() {

        //Dimensiones de la ventana
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        //establecemos el modelo de la tabla
        tblAuditoria.setModel(model);
        //mostrar contenido en ventana emergente
        tblAuditoria.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblAuditoria.rowAtPoint(e.getPoint());
                    int col = tblAuditoria.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        Object value = tblAuditoria.getValueAt(row, col);
                        JOptionPane.showMessageDialog(null,
                                value == null ? "" : value.toString(),
                                "Contenido de celda",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        //Cargar opciones en el combo Filtro
        cboFiltro.addItem("Modulo");
        cboFiltro.addItem("Usuario");
        cboFiltro.addItem("Todos");
        cboOpcion.setEnabled(false);

        //Listener al escoger filtro
        cboFiltro.addActionListener(e -> {
            String filtro = (String) cboFiltro.getSelectedItem();
            if (filtro.equals("Modulo")) {
                cboOpcion.setEnabled(true);
                infoNombre.setVisible(true);
                cboOpcion.setVisible(true);
                cargarCBOModulo();
            } else if (filtro.equals("Usuario")) {
                cboOpcion.setEnabled(true);
                infoNombre.setVisible(true);
                cboOpcion.setVisible(true);
                cargarUsernamesEnCombo(cboOpcion);
            } else if (filtro.equals("Todos")) {
                infoNombre.setVisible(false);
                cboOpcion.setVisible(false);
            }
        });

        // Acción del botón Cargar
        btnCargar.addActionListener(e -> cargarAuditorias());

        //Accion del boton limpiar
        btnLimpiar.addActionListener(e -> limpiarTabla());

        //Acción para Salir
        btnSalir.addActionListener(e -> onSalir());
    }

    private void cargarAuditorias() {
        String filtro = (String) cboFiltro.getSelectedItem();
        String opcion = (String) cboOpcion.getSelectedItem();

        // Validaciones
        if (!"Todos".equalsIgnoreCase(filtro)) {
            if ("Seleccione un modulo...".equalsIgnoreCase(opcion)) {
                JOptionPane.showMessageDialog(null,
                        "Debe seleccionar un modulo valido ");
                return;
            } else if ("Seleccione un usuario...".equalsIgnoreCase(opcion)) {
                JOptionPane.showMessageDialog(null,
                        "Debe seleccionar un usuario valido ");
                return;
            }
        }

        limpiarTabla();

        try {
            AuditoriaDAO dao = new AuditoriaDAO();
            List<Map<String, Object>> lista;

            switch (filtro) {
                case "Modulo" -> {
                    if (!dao.existeModulo(opcion)) {
                        JOptionPane.showMessageDialog(null,
                                "No exsiten registros para el modulo: " + opcion,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    lista = dao.listarPorModulo(opcion);
                }
                case "Usuario" -> {
                    Integer idUsuario = dao.obtenerIdUsuarioPorUsername(opcion);
                    if (idUsuario == null) {
                        JOptionPane.showMessageDialog(null,
                                "No se encontró ningún usuario:  '" + opcion + "'.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    lista = dao.listarPorUsuario(idUsuario);
                }
                default -> lista = dao.listarConUsuario();
            }

            // Cargar los resultados en la tabla
            for (Map<String, Object> fila : lista) {
                model.addRow(new Object[]{
                        fila.get("id"),
                        fila.get("fechaHora"),
                        fila.get("usuario"),
                        fila.get("rol"),
                        fila.get("modulo"),
                        fila.get("accion"),
                        fila.get("detalle")
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar auditorías: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void limpiarTabla() {
        // Limpiar la tabla antes de cargar nuevos datos
        DefaultTableModel model = (DefaultTableModel) tblAuditoria.getModel();
        model.setRowCount(0);
    }

    public void cargarCBOModulo() {
        cboOpcion.removeAllItems();
        cboOpcion.addItem("Seleccione un modulo..."); // opción inicial
        //Cargar opciones en el combo Opcion
        cboOpcion.addItem("Auditoria");
        cboOpcion.addItem("Catalogo");
        cboOpcion.addItem("Cliente");
        cboOpcion.addItem("Compras");
        cboOpcion.addItem("Financiero");
        cboOpcion.addItem("Inventarios");
        cboOpcion.addItem("Prestamos");
        cboOpcion.addItem("Reservas");
        cboOpcion.addItem("Seguridad");
        cboOpcion.addItem("Reportes");
        cboOpcion.addItem("SolicitudesCompra");
        cboOpcion.addItem("Usuarios");

    }

    //Cargamos el cbOpcion
    private void cargarUsernamesEnCombo(JComboBox<String> cboOpcion) {
        try {
            UsuarioDAO dao = new UsuarioDAO();
            List<String> usernames = dao.listarUsernamesActivos();

            cboOpcion.removeAllItems();
            cboOpcion.addItem("Seleccione un usuario..."); // opción inicial

            for (String username : usernames) {
                cboOpcion.addItem(username);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar los usernames: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    //Launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Auditoria");
            f.setContentPane(new AuditoriaForm().panelPrincipal);
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
        panelPrincipal.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 4, new Insets(0, 0, 0, 0), -1, -1));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panelPrincipal.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("JetBrains Mono", -1, 18, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Auditoria \uD83D\uDC6E");
        panelPrincipal.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Filtrar por: ");
        panelPrincipal.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        infoNombre = new JLabel();
        infoNombre.setText("Seleccione:");
        panelPrincipal.add(infoNombre, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelPrincipal.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tblAuditoria = new JTable();
        scrollPane1.setViewportView(tblAuditoria);
        cboOpcion = new JComboBox();
        panelPrincipal.add(cboOpcion, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cboFiltro = new JComboBox();
        panelPrincipal.add(cboFiltro, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar \uD83D\uDD0C");
        panelPrincipal.add(btnLimpiar, new com.intellij.uiDesigner.core.GridConstraints(4, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCargar = new JButton();
        btnCargar.setText("Cargar \uD83D\uDCC0");
        panelPrincipal.add(btnCargar, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSalir = new JButton();
        btnSalir.setText("Salir");
        panelPrincipal.add(btnSalir, new com.intellij.uiDesigner.core.GridConstraints(4, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
