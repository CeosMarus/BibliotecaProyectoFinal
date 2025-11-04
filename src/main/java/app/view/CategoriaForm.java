package app.view;

import app.dao.CategoriaDAO;
import app.model.Categoria;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoriaForm extends JFrame {
    // Componentes del formulario
    private JTextField txtNombre;
    private JComboBox<String> cboEstado;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JTextField txtBuscar;
    private JTable tblCategorias;
    private JButton btnSalir;
    private JButton btnCargar;
    public JPanel panelPrincipal;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Estado"}, 0
    );
    private Integer selectedId = null;

    public CategoriaForm() {
        // Configuración del JFrame
        setTitle("Gestión de Categorías");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(panelPrincipal);
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        // Tabla
        tblCategorias.setModel(model);
        tblCategorias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Combo Estado
        cboEstado.addItem("Activo");
        cboEstado.addItem("Inactivo");

        // Listeners botones
        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnCargar.addActionListener(e -> cargarTabla());
        btnSalir.addActionListener(e -> onSalir());

        // Listener selección de tabla
        tblCategorias.getSelectionModel().addListSelectionListener(this::onTableSelection);

        // Búsqueda en tiempo real
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { buscar(); }
            public void removeUpdate(DocumentEvent e) { buscar(); }
            public void changedUpdate(DocumentEvent e) { buscar(); }

            private void buscar() {
                String texto = txtBuscar.getText().trim();
                List<Categoria> lista = texto.isEmpty() ? categoriaDAO.listarTodas() : categoriaDAO.buscarPorNombre(texto);
                model.setRowCount(0);
                for (Categoria c : lista) {
                    model.addRow(new Object[]{
                            c.getId(),
                            c.getNombre(),
                            c.getEstado() == 1 ? "Activo" : "Inactivo"
                    });
                }
            }
        });

        // Cargar datos al iniciar
        cargarTabla();
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

    // Selección de tabla
    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tblCategorias.getSelectedRow();
        if (row == -1) {
            selectedId = null;
            return;
        }

        selectedId = (Integer) model.getValueAt(row, 0);
        txtNombre.setText((String) model.getValueAt(row, 1));
        cboEstado.setSelectedIndex(model.getValueAt(row, 2).equals("Activo") ? 0 : 1);
    }

    // Guardar
    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "El campo Nombre es obligatorio",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setEstado(estado);

        if (categoriaDAO.agregarCategoria(c)) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    // Actualizar
    private void onActualizar() {
        if (selectedId == null) return;

        String nombre = txtNombre.getText().trim();
        int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "El campo Nombre es obligatorio",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        Categoria c = new Categoria(selectedId, nombre, estado);
        if (categoriaDAO.actualizarCategoria(c)) {
            cargarTabla();
        }
    }

    // Eliminar (lógica)
    private void onEliminar() {
        if (selectedId == null) return;

        int r = JOptionPane.showConfirmDialog(panelPrincipal,
                "¿Está seguro que desea desactivar esta categoría?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        if (categoriaDAO.eliminarCategoria(selectedId)) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    // Limpiar formulario
    private void limpiarFormulario() {
        txtNombre.setText("");
        cboEstado.setSelectedIndex(0);
        tblCategorias.clearSelection();
        selectedId = null;
    }

    // Cargar tabla
    private void cargarTabla() {
        List<Categoria> lista = categoriaDAO.listarTodas();
        model.setRowCount(0);
        for (Categoria c : lista) {
            model.addRow(new Object[]{
                    c.getId(),
                    c.getNombre(),
                    c.getEstado() == 1 ? "Activo" : "Inactivo"
            });
        }
    }

    public JPanel getPanel() {
        return panelPrincipal;
    }
    public static void main(String[] args) {
        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            CategoriaForm form = new CategoriaForm();
            form.setVisible(true);
        });
    }
}
