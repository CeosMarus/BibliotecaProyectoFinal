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
    private JTextField txtSala;
    private JTextField txtEstante;
    private JTextField txtNivel;
    private JTextField txtEstadoCopia;
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
    }


    // ============================================================
    // INICIALIZACIÓN
    // ============================================================

    private void inicializarFormulario() {
        configurarTabla();
        cargarLibros();
        cargarEjemplares();
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
            comboLibro.addItem("⚠️ No hay libros activos");
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
        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas cerrar el formulario?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmar == JOptionPane.YES_OPTION) {
            this.dispose(); // Cierra solo este JFrame
        }
    }


    // ============================================================
    // CRUD
    // ============================================================

    private void guardarEjemplar(ActionEvent evt) {
        try {
            if (!validarCampos()) return;

            int idLibro = Integer.parseInt(comboLibro.getSelectedItem().toString().split(" - ")[0]);
            Date fechaAlta = Date.valueOf(txtFechaAlta.getText().trim());
            Date fechaBaja = txtFechaBaja.getText().isEmpty() ? null : Date.valueOf(txtFechaBaja.getText().trim());

            Ejemplar e = new Ejemplar();
            e.setIdLibro(idLibro);
            e.setCodigoInventario(txtCodigoInventario.getText().trim());
            e.setSala(txtSala.getText().trim());
            e.setEstante(txtEstante.getText().trim());
            e.setNivel(txtNivel.getText().trim());
            e.setEstadoCopia(txtEstadoCopia.getText().trim());
            e.setFechaAlta(fechaAlta);
            e.setFechaBaja(fechaBaja);
            e.setEstado(1);

            EjemplarDAO dao = new EjemplarDAO();
            dao.insertar(e);

            JOptionPane.showMessageDialog(this, "Ejemplar guardado correctamente.");
            limpiarCampos();
            cargarEjemplares();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Error en el formato de fecha. Usa AAAA-MM-DD.");
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
            e.setSala(txtSala.getText().trim());
            e.setEstante(txtEstante.getText().trim());
            e.setNivel(txtNivel.getText().trim());
            e.setEstadoCopia(txtEstadoCopia.getText().trim());
            e.setFechaAlta(fechaAlta);
            e.setFechaBaja(fechaBaja);
            e.setEstado(1);

            EjemplarDAO dao = new EjemplarDAO();
            dao.actualizar(e);

            JOptionPane.showMessageDialog(this, "Ejemplar actualizado correctamente.");
            limpiarCampos();
            cargarEjemplares();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto. Usa AAAA-MM-DD.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
        }
    }

    private void eliminarEjemplar(ActionEvent evt) {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un ejemplar para eliminar.");
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Deseas desactivar este ejemplar?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                EjemplarDAO dao = new EjemplarDAO();
                dao.eliminar(idSeleccionado);
                JOptionPane.showMessageDialog(this, "Ejemplar desactivado correctamente.");
                limpiarCampos();
                cargarEjemplares();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }


    // ============================================================
    // AUXILIARES
    // ============================================================

    private boolean validarCampos() {
        // Campos obligatorios
        if (txtCodigoInventario.getText().trim().isEmpty() ||
                txtSala.getText().trim().isEmpty() ||
                txtEstante.getText().trim().isEmpty() ||
                txtNivel.getText().trim().isEmpty() ||
                txtEstadoCopia.getText().trim().isEmpty() ||
                txtFechaAlta.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios excepto Fecha Baja.");
            return false;
        }

        /*
        // Validación numérica: Nivel
        try {
            Integer.parseInt(txtNivel.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El campo Nivel solo acepta valores numéricos.");
            txtNivel.requestFocus();
            return false;
        }


        // Validación numérica: Código de Inventario (si aplica)
        try {
            Integer.parseInt(txtCodigoInventario.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El campo Código de Inventario solo acepta valores numéricos.");
            txtCodigoInventario.requestFocus();
            return false;
        }
        */
        // Validación numérica: Nivel
        try {
            Integer.parseInt(txtEstante.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El campo Estante solo acepta valores numéricos.");
            txtEstante.requestFocus();
            return false;
        }

        // Validación de fecha
        try {
            Date.valueOf(txtFechaAlta.getText().trim());
            if (!txtFechaBaja.getText().isEmpty()) {
                Date.valueOf(txtFechaBaja.getText().trim());
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto. Usa AAAA-MM-DD.");
            return false;
        }

        return true;
    }


    private void limpiarCampos() {
        idSeleccionado = -1;
        txtCodigoInventario.setText("");
        txtSala.setText("");
        txtEstante.setText("");
        txtNivel.setText("");
        txtEstadoCopia.setText("");
        txtFechaAlta.setText("");
        txtFechaBaja.setText("");
        if (comboLibro.getItemCount() > 0) comboLibro.setSelectedIndex(0);
        tablaEjemplares.clearSelection();
    }

    private void cargarSeleccionTabla() {
        int fila = tablaEjemplares.getSelectedRow();
        if (fila != -1) {
            idSeleccionado = (int) tablaEjemplares.getValueAt(fila, 0);
            txtCodigoInventario.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 1)));

            // Seleccionar el libro correcto por ID en el combo
            String libroNombreTabla = String.valueOf(tablaEjemplares.getValueAt(fila, 2));
            for (int i = 0; i < comboLibro.getItemCount(); i++) {
                String item = comboLibro.getItemAt(i);
                // Cada item tiene formato "id - nombre"
                if (item.endsWith(" - " + libroNombreTabla)) {
                    comboLibro.setSelectedIndex(i);
                    break;
                }
            }

            txtSala.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 3)));
            txtEstante.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 4)));
            txtNivel.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 5)));
            txtEstadoCopia.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 6)));
            txtFechaAlta.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 7)));
            txtFechaBaja.setText(String.valueOf(tablaEjemplares.getValueAt(fila, 8)));
        }
    }


    // ============================================================
    // MAIN
    // ============================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            EjemplarForm form = new EjemplarForm();
            form.setVisible(true);
        });
    }
}
