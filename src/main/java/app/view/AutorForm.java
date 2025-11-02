package app.view;

import app.dao.AutorDAO;
import app.model.Autor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class AutorForm extends JFrame {

    // Componentes del formulario (respetando tu UI Designer)
    public JPanel panelPrincipal;
    private JTextField txtNombre;
    private JTextArea txtBiografia;
    private JComboBox<String> cboEstado;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JTextField txtBuscar;
    private JTable tblAutores;
    private JButton btnSalir;
    private JButton btnCargar;

    private final AutorDAO autorDAO = new AutorDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Biografía", "Estado"}, 0
    );

    private Integer selectedId = null;

    public AutorForm() {
        setTitle("Gestión de Autores");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // **Respetando el panel de UI Designer**
        setContentPane(panelPrincipal);

        tblAutores.setModel(model);
        tblAutores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Combo Estado
        cboEstado.addItem("Activo");
        cboEstado.addItem("Desactivado");

        // Botones
        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnCargar.addActionListener(e -> cargarTabla());
        btnSalir.addActionListener(e -> dispose());

        // Selección de tabla
        tblAutores.getSelectionModel().addListSelectionListener(this::onTableSelection);

        // Búsqueda en tiempo real
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { buscar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { buscar(); }
            @Override
            public void changedUpdate(DocumentEvent e) { buscar(); }

            private void buscar() {
                String texto = txtBuscar.getText().trim();
                List<Autor> lista = texto.isEmpty() ? autorDAO.listarAutoresActivos() : autorDAO.listarTodosLosAutores();
                model.setRowCount(0);
                for (Autor a : lista) {
                    if (a.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                        model.addRow(new Object[]{
                                a.getId(),
                                a.getNombre(),
                                a.getBiografia(),
                                a.getEstado() == 1 ? "Activo" : "Desactivado"
                        });
                    }
                }
            }
        });

        // Cargar datos al iniciar
        cargarTabla();
    }

    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        int row = tblAutores.getSelectedRow();
        if (row == -1) {
            selectedId = null;
            return;
        }

        selectedId = (Integer) model.getValueAt(row, 0);
        txtNombre.setText((String) model.getValueAt(row, 1));
        txtBiografia.setText((String) model.getValueAt(row, 2));
        cboEstado.setSelectedIndex(model.getValueAt(row, 3).equals("Activo") ? 0 : 1);
    }

    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        String biografia = txtBiografia.getText().trim();
        int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "El campo Nombre es obligatorio", "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        Autor autor = new Autor();
        autor.setNombre(nombre);
        autor.setBiografia(biografia);
        autor.setEstado(estado);

        if (autorDAO.agregarAutor(autor)) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    private void onActualizar() {
        if (selectedId == null) return;

        String nombre = txtNombre.getText().trim();
        String biografia = txtBiografia.getText().trim();
        int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "El campo Nombre es obligatorio", "Validación", JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }

        Autor autor = new Autor();
        autor.setId(selectedId);
        autor.setNombre(nombre);
        autor.setBiografia(biografia);
        autor.setEstado(estado);

        if (autorDAO.actualizarAutor(autor)) {
            cargarTabla();
        }
    }

    private void onEliminar() {
        if (selectedId == null) return;

        if (autorDAO.eliminarAutor(selectedId)) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtBiografia.setText("");
        cboEstado.setSelectedIndex(0);
        tblAutores.clearSelection();
        selectedId = null;
    }

    private void cargarTabla() {
        List<Autor> lista = autorDAO.listarAutoresActivos();
        model.setRowCount(0);
        for (Autor a : lista) {
            model.addRow(new Object[]{
                    a.getId(),
                    a.getNombre(),
                    a.getBiografia(),
                    a.getEstado() == 1 ? "Activo" : "Desactivado"
            });
        }
    }

    public JPanel getPanel() {
        return panelPrincipal;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AutorForm form = new AutorForm();
            form.setVisible(true);
        });
    }
}
