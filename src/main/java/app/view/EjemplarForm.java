package app.view;

import app.dao.EjemplarDAO;
import app.dao.LibroDAO;
import app.model.Ejemplar;
import app.model.LibroConAutor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class EjemplarForm extends JFrame {

    private JPanel panelPrincipal;
    private JTextField txtCodigo;
    private JComboBox<LibroConAutor> cboLibro;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnCargar;
    private JButton btnSalir;
    private JTable tblEjemplares;
    private JTextField txtBuscar;

    private final EjemplarDAO ejemplarDAO = new EjemplarDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Código", "Libro", "Estado"}, 0
    );
    private Integer selectedId = null;

    public EjemplarForm() {
        setTitle("Gestión de Ejemplares");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(panelPrincipal);

        tblEjemplares.setModel(model);
        tblEjemplares.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cargarLibros();

        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnCargar.addActionListener(e -> cargarTabla());

        tblEjemplares.getSelectionModel().addListSelectionListener(this::onTableSelection);

        // 🔍 Búsqueda en tiempo real
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { buscar(); }
            public void removeUpdate(DocumentEvent e) { buscar(); }
            public void changedUpdate(DocumentEvent e) { buscar(); }

            private void buscar() {
                String texto = txtBuscar.getText().trim();
                try {
                    List<Ejemplar> lista = texto.isEmpty()
                            ? ejemplarDAO.listarConLibro()
                            : ejemplarDAO.buscarPorCodigoInventario(texto);

                    model.setRowCount(0);
                    for (Ejemplar ej : lista) {
                        model.addRow(new Object[]{
                                ej.getId(),
                                ej.getCodigoInventario(),
                                ej.getLibroNombre(),
                                ej.getEstadoDescripcion()
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        cargarTabla();
    }

    private void cargarLibros() {
        cboLibro.removeAllItems();
        try {
            List<LibroConAutor> lista = libroDAO.listarTodos();
            for (LibroConAutor l : lista) {
                cboLibro.addItem(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tblEjemplares.getSelectedRow();
        if (row == -1) {
            selectedId = null;
            return;
        }

        selectedId = (Integer) model.getValueAt(row, 0);
        txtCodigo.setText((String) model.getValueAt(row, 1));

        String libroNombre = (String) model.getValueAt(row, 2);
        for (int i = 0; i < cboLibro.getItemCount(); i++) {
            if (cboLibro.getItemAt(i).getNombre().equals(libroNombre)) {
                cboLibro.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onGuardar() {
        String codigo = txtCodigo.getText().trim();
        LibroConAutor libro = (LibroConAutor) cboLibro.getSelectedItem();

        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "El campo Código es obligatorio",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtCodigo.requestFocus();
            return;
        }
        if (libro == null) {
            JOptionPane.showMessageDialog(panelPrincipal, "Debe seleccionar un Libro",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Ejemplar e = new Ejemplar();
            e.setCodigoInventario(codigo);
            e.setIdLibro(libro.getId());
            e.setSala("Principal");
            e.setEstante("A1");
            e.setNivel("1");
            e.setEstadoCopia("Disponible");
            e.setFechaAlta(Date.valueOf(LocalDate.now()));
            e.setEstado("Activo");

            int id = ejemplarDAO.insertar(e);
            if (id > 0) {
                JOptionPane.showMessageDialog(panelPrincipal, "Ejemplar agregado correctamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarTabla();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onActualizar() {
        if (selectedId == null) return;

        String codigo = txtCodigo.getText().trim();
        LibroConAutor libro = (LibroConAutor) cboLibro.getSelectedItem();

        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "El campo Código es obligatorio",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtCodigo.requestFocus();
            return;
        }

        try {
            Ejemplar e = new Ejemplar();
            e.setId(selectedId);
            e.setIdLibro(libro.getId());
            e.setCodigoInventario(codigo);
            e.setSala("Principal");
            e.setEstante("A1");
            e.setNivel("1");
            e.setEstadoCopia("Disponible");
            e.setFechaAlta(Date.valueOf(LocalDate.now()));
            e.setEstado("Activo");

            if (ejemplarDAO.actualizar(e)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Ejemplar actualizado correctamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarTabla();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onEliminar() {
        if (selectedId == null) return;

        int r = JOptionPane.showConfirmDialog(panelPrincipal,
                "¿Desea desactivar este ejemplar?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        try {
            if (ejemplarDAO.eliminar(selectedId)) {
                JOptionPane.showMessageDialog(panelPrincipal, "Ejemplar desactivado correctamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
                cargarTabla();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        cboLibro.setSelectedIndex(-1);
        tblEjemplares.clearSelection();
        selectedId = null;
    }

    private void cargarTabla() {
        try {
            List<Ejemplar> lista = ejemplarDAO.listarConLibro();
            model.setRowCount(0);
            for (Ejemplar e : lista) {
                model.addRow(new Object[]{
                        e.getId(),
                        e.getCodigoInventario(),
                        e.getLibroNombre(),
                        e.getEstadoDescripcion()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public JPanel getPanel() {
        return panelPrincipal;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EjemplarForm().setVisible(true));
    }
}
