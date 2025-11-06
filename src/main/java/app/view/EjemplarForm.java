package app.view;

import app.dao.EjemplarDAO;
import app.dao.LibroDAO;
import app.model.Ejemplar;
import app.model.LibroConAutor;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Date;
import java.util.List;
import java.util.Locale;

public class EjemplarForm extends JFrame {
    public JPanel mainPanel;
    private JTable tablaEjemplares;
    private JComboBox<String> comboLibro;
    private JTextField txtCodigoInventario;
    private JComboBox<String> cbSala;
    private JTextField txtEstante;
    private JTextField txtNivel;
    private JComboBox<String> cbEstadoCopia;
    private JTextField txtFechaAlta;
    private JTextField txtFechaBaja;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnSalir;

    private int idSeleccionado = -1;

    public EjemplarForm() {
        setTitle("Gestión de Ejemplares");
        //setContentPane(mainPanel);
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainPanel.setPreferredSize(new Dimension(900, 600));

        inicializarFormulario();

        btnGuardar.addActionListener(this::guardarEjemplar);
        btnActualizar.addActionListener(this::actualizarEjemplar);
        btnEliminar.addActionListener(this::eliminarEjemplar);
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnSalir.addActionListener(e -> onSalir());

        tablaEjemplares.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccionTabla();
        });

        // ✅ Auto generar código cuando cambia el libro
        comboLibro.addActionListener(e -> generarCodigoInventarioAutomatico());

        // ✅ Control campo Fecha Baja según estado copia
        cbEstadoCopia.addActionListener(e -> {
            String estado = (String) cbEstadoCopia.getSelectedItem();
            if ("Nuevo".equals(estado)) {
                txtFechaBaja.setText("");
                txtFechaBaja.setEnabled(false);
            } else {
                txtFechaBaja.setEnabled(true);
            }
        });
    }

    private void inicializarFormulario() {
        configurarTabla();
        cargarLibros();
        cargarEjemplares();

        cbSala.addItem("Sala Norte");
        cbSala.addItem("Sala Sur");
        cbSala.addItem("Sala Este");
        cbSala.addItem("Sala Oeste");
        cbSala.addItem("Sala Central");

        cbEstadoCopia.addItem("Nuevo");
        cbEstadoCopia.addItem("Dañado");
        cbEstadoCopia.addItem("Perdido");

        // ✅ Bloquear fecha baja al inicio
        txtFechaBaja.setEnabled(false);
    }

    private void configurarTabla() {
        tablaEjemplares.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Código Inventario", "Libro", "Sala", "Estante",
                        "Nivel", "Estado Copia", "Fecha Alta", "Fecha Baja", "Estado"}
        ));
    }

    private void cargarLibros() {
        comboLibro.removeAllItems();
        LibroDAO libroDAO = new LibroDAO();
        List<LibroConAutor> libros = libroDAO.listarTodos();

        if (libros.isEmpty()) {
            comboLibro.addItem("⚠ No hay libros activos");
            comboLibro.setEnabled(false);
        } else {
            comboLibro.setEnabled(true);
            for (LibroConAutor l : libros) {
                comboLibro.addItem(l.getId() + " - " + l.getTitulo());
            }
        }
    }

    private void cargarEjemplares() {
        DefaultTableModel modelo = (DefaultTableModel) tablaEjemplares.getModel();
        modelo.setRowCount(0);

        EjemplarDAO ejemplarDAO = new EjemplarDAO();
        try {
            List<Ejemplar> lista = ejemplarDAO.listarConLibro();
            for (Ejemplar e : lista) {
                modelo.addRow(new Object[]{
                        e.getId(),
                        e.getCodigoInventario(),
                        e.getLibroNombre(),
                        e.getSala(),
                        e.getEstante(),
                        e.getNivel(),
                        e.getEstadoCopia(),
                        e.getFechaAlta() != null ? e.getFechaAlta().toString() : "",
                        e.getFechaBaja() != null ? e.getFechaBaja().toString() : "",
                        e.getEstadoDescripcion()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar ejemplares: " + ex.getMessage());
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

    private void guardarEjemplar(ActionEvent evt) {
        try {
            if (!validarCampos()) return;

            int idLibro = Integer.parseInt(comboLibro.getSelectedItem().toString().split(" - ")[0]);
            Date fechaAlta = Date.valueOf(txtFechaAlta.getText().trim());
            Date fechaBaja = txtFechaBaja.getText().isEmpty() ? null : Date.valueOf(txtFechaBaja.getText().trim());

            Ejemplar e = new Ejemplar();
            e.setIdLibro(idLibro);
            e.setCodigoInventario(txtCodigoInventario.getText().trim());
            e.setSala(cbSala.getSelectedItem().toString());
            e.setEstante(txtEstante.getText().trim());
            e.setNivel(txtNivel.getText().trim());
            e.setEstadoCopia(cbEstadoCopia.getSelectedItem().toString());
            e.setFechaAlta(fechaAlta);
            e.setFechaBaja(fechaBaja);
            e.setEstado(1);

            new EjemplarDAO().insertar(e);

            JOptionPane.showMessageDialog(this, "Ejemplar guardado correctamente.");
            limpiarCampos();
            cargarEjemplares();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }

    private void actualizarEjemplar(ActionEvent evt) {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un ejemplar primero.");
            return;
        }

        try {
            if (!validarCampos()) return;

            int idLibro = Integer.parseInt(comboLibro.getSelectedItem().toString().split(" - ")[0]);
            Date fechaAlta = Date.valueOf(txtFechaAlta.getText().trim());
            Date fechaBaja = txtFechaBaja.getText().isEmpty() ? null : Date.valueOf(txtFechaBaja.getText().trim());

            Ejemplar e = new Ejemplar();
            e.setId(idSeleccionado);
            e.setIdLibro(idLibro);
            e.setCodigoInventario(txtCodigoInventario.getText().trim());
            e.setSala(cbSala.getSelectedItem().toString());
            e.setEstante(txtEstante.getText().trim());
            e.setNivel(txtNivel.getText().trim());
            e.setEstadoCopia(cbEstadoCopia.getSelectedItem().toString());
            e.setFechaAlta(fechaAlta);
            e.setFechaBaja(fechaBaja);
            e.setEstado(1);

            new EjemplarDAO().actualizar(e);

            JOptionPane.showMessageDialog(this, "Ejemplar actualizado correctamente.");
            limpiarCampos();
            cargarEjemplares();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
        }
    }

    private void eliminarEjemplar(ActionEvent evt) {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un ejemplar para eliminar.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "¿Deseas desactivar este ejemplar?",
                "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            try {
                new EjemplarDAO().eliminar(idSeleccionado);
                JOptionPane.showMessageDialog(this, "Ejemplar desactivado correctamente.");
                limpiarCampos();
                cargarEjemplares();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    private boolean validarCampos() {
        if (txtCodigoInventario.getText().trim().isEmpty() ||
                txtEstante.getText().trim().isEmpty() ||
                txtNivel.getText().trim().isEmpty() ||
                txtFechaAlta.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios excepto Fecha Baja.");
            return false;
        }

        try {
            Integer.parseInt(txtEstante.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "El campo Estante solo acepta valores numéricos.");
            return false;
        }

        try {
            Date.valueOf(txtFechaAlta.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Formato de Fecha Alta incorrecto. Usa AAAA-MM-DD.");
            return false;
        }

        // ✅ Nueva validación para estados Dañado / Perdido
        String estado = (String) cbEstadoCopia.getSelectedItem();
        if (!"Nuevo".equals(estado)) {
            if (txtFechaBaja.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Debe ingresar una Fecha de Baja para ejemplares Dañados o Perdidos.");
                return false;
            }
            try {
                Date.valueOf(txtFechaBaja.getText().trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Formato de Fecha Baja incorrecto. Usa AAAA-MM-DD.");
                return false;
            }
        }

        return true;
    }

    private void limpiarCampos() {
        idSeleccionado = -1;
        txtCodigoInventario.setText("");
        txtEstante.setText("");
        txtNivel.setText("");
        txtFechaAlta.setText("");
        txtFechaBaja.setText("");
        cbSala.setSelectedIndex(0);
        cbEstadoCopia.setSelectedIndex(0);
        txtFechaBaja.setEnabled(false);
        if (comboLibro.getItemCount() > 0) comboLibro.setSelectedIndex(0);
        tablaEjemplares.clearSelection();
    }

    private void cargarSeleccionTabla() {
        int fila = tablaEjemplares.getSelectedRow();
        if (fila == -1) return;

        idSeleccionado = (int) tablaEjemplares.getValueAt(fila, 0);
        txtCodigoInventario.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 1)));

        String libro = String.valueOf(tablaEjemplares.getValueAt(fila, 2));
        for (int i = 0; i < comboLibro.getItemCount(); i++) {
            if (comboLibro.getItemAt(i).endsWith(" - " + libro)) {
                comboLibro.setSelectedIndex(i);
                break;
            }
        }

        cbSala.setSelectedItem(String.valueOf(tablaEjemplares.getValueAt(fila, 3)));
        txtEstante.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 4)));
        txtNivel.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 5)));
        cbEstadoCopia.setSelectedItem(String.valueOf(tablaEjemplares.getValueAt(fila, 6)));
        txtFechaAlta.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 7)));
        txtFechaBaja.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 8)));

        // ✅ Habilitar fecha baja si es Dañado o Perdido
        txtFechaBaja.setEnabled(!"Nuevo".equals(cbEstadoCopia.getSelectedItem()));
    }

    private void generarCodigoInventarioAutomatico() {
        if (comboLibro.getSelectedItem() == null) return;

        String valor = comboLibro.getSelectedItem().toString();
        if (!valor.contains(" - ")) return;

        String titulo = valor.substring(valor.indexOf(" - ") + 3).trim();

        StringBuilder codigo = new StringBuilder();
        for (String palabra : titulo.split(" ")) {
            if (!palabra.isEmpty()) {
                codigo.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }

        txtCodigoInventario.setText(codigo.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            EjemplarForm form = new EjemplarForm();
            form.setVisible(true);
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
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(16, 3, new Insets(8, 8, 8, 8), -1, -1));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Chalkboard SE", Font.BOLD, 26, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Gestion de Ejemplares");
        mainPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Ingresa el Codigo del Libro");
        mainPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtCodigoInventario = new JTextField();
        txtCodigoInventario.setEditable(false);
        mainPanel.add(txtCodigoInventario, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Selecciona el Libro");
        mainPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboLibro = new JComboBox();
        mainPanel.add(comboLibro, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Ingresa la Sala que esta el Libro");
        mainPanel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Ingresa el Estante que esta el Libro");
        mainPanel.add(label5, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtEstante = new JTextField();
        mainPanel.add(txtEstante, new com.intellij.uiDesigner.core.GridConstraints(5, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Ingresa en que nivel esta el Libro");
        mainPanel.add(label6, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtNivel = new JTextField();
        mainPanel.add(txtNivel, new com.intellij.uiDesigner.core.GridConstraints(6, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Ingresa la El estado de la copia");
        mainPanel.add(label7, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Ingresa la fecha que se dio de alta");
        mainPanel.add(label8, new com.intellij.uiDesigner.core.GridConstraints(9, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFechaAlta = new JTextField();
        mainPanel.add(txtFechaAlta, new com.intellij.uiDesigner.core.GridConstraints(9, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Ingresa la fecha de baja");
        mainPanel.add(label9, new com.intellij.uiDesigner.core.GridConstraints(10, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFechaBaja = new JTextField();
        mainPanel.add(txtFechaBaja, new com.intellij.uiDesigner.core.GridConstraints(10, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(11, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        btnGuardar = new JButton();
        btnGuardar.setText("Guardar");
        mainPanel.add(btnGuardar, new com.intellij.uiDesigner.core.GridConstraints(13, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnActualizar = new JButton();
        btnActualizar.setText("Actualizar");
        mainPanel.add(btnActualizar, new com.intellij.uiDesigner.core.GridConstraints(13, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEliminar = new JButton();
        btnEliminar.setText("Eliminar");
        mainPanel.add(btnEliminar, new com.intellij.uiDesigner.core.GridConstraints(14, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar");
        mainPanel.add(btnLimpiar, new com.intellij.uiDesigner.core.GridConstraints(14, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSalir = new JButton();
        btnSalir.setText("Salir");
        mainPanel.add(btnSalir, new com.intellij.uiDesigner.core.GridConstraints(15, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Ojo: escribe el estante en numeros ");
        mainPanel.add(label10, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbSala = new JComboBox();
        mainPanel.add(cbSala, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbEstadoCopia = new JComboBox();
        mainPanel.add(cbEstadoCopia, new com.intellij.uiDesigner.core.GridConstraints(7, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Formatos de la Fecha (YYYY-MM-DD0");
        mainPanel.add(label11, new com.intellij.uiDesigner.core.GridConstraints(8, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(12, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tablaEjemplares = new JTable();
        scrollPane1.setViewportView(tablaEjemplares);
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