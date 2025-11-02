package app.view;

import app.dao.EjemplarDAO;
import app.dao.LibroDAO;
import app.model.Ejemplar;
import app.model.LibroConAutor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.sql.Date;
import java.util.List;

public class EjemplarForm extends JFrame {
    private JPanel mainPanel;
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
        setContentPane(mainPanel);
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        inicializarFormulario();

        btnGuardar.addActionListener(this::guardarEjemplar);
        btnActualizar.addActionListener(this::actualizarEjemplar);
        btnEliminar.addActionListener(this::eliminarEjemplar);
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnSalir.addActionListener(e -> salirFormulario());

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

    private void salirFormulario() {
        if (JOptionPane.showConfirmDialog(this, "¿Deseas cerrar el formulario?",
                "Confirmar salida", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            this.dispose();
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

        try { Integer.parseInt(txtEstante.getText().trim()); }
        catch (Exception e) {
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
            try { Date.valueOf(txtFechaBaja.getText().trim()); }
            catch (Exception e) {
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
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            EjemplarForm form = new EjemplarForm();
            form.setVisible(true);
        });
    }
}