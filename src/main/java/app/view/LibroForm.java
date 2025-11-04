package app.view;

import app.dao.CategoriaDAO;
import app.dao.LibroDAO;
import app.dao.AutorDAO;
import app.model.Categoria;
import app.model.Libro;
import app.model.LibroConAutor;
import app.model.Autor;

import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibroForm extends JFrame {
    public JPanel panelPrincipal;
    private JTextField txtNombre;
    private JTextField txtAnio;
    private JTextField txtIsbn; // <-- agregado
    private JComboBox<Autor> cboAutor;
    private JComboBox<Categoria> cboCategoria;
    private JComboBox<String> cboEstado;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnCargar;
    private JButton btnSalir;
    private JTextField txtBuscar;
    private JTable tblLibros;

    private final LibroDAO libroDAO = new LibroDAO();
    private final AutorDAO autorDAO = new AutorDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Año", "ISBN", "Autor", "Categoría", "Estado"}, 0
    );

    private Integer selectedId = null;

    public LibroForm() {
        setTitle("Gestión de Libros");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(panelPrincipal);

        panelPrincipal.setPreferredSize(new Dimension(1000, 600));
        tblLibros.setModel(model);
        tblLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cboEstado.addItem("Activo");
        cboEstado.addItem("Inactivo");

        cargarAutoresYCategorias();

        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnCargar.addActionListener(e -> cargarTabla());
        btnSalir.addActionListener(e -> onSalir());

        tblLibros.getSelectionModel().addListSelectionListener(this::onTableSelection);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { buscar(); }
            public void removeUpdate(DocumentEvent e) { buscar(); }
            public void changedUpdate(DocumentEvent e) { buscar(); }

            private void buscar() {
                String texto = txtBuscar.getText().trim();
                try {
                    List<LibroConAutor> lista = texto.isEmpty() ? libroDAO.listarTodos() : libroDAO.listarTodos().stream()
                            .filter(l -> l.getTitulo().toLowerCase().contains(texto.toLowerCase()))
                            .toList();

                    model.setRowCount(0);
                    for (LibroConAutor l : lista) {
                        model.addRow(new Object[]{
                                l.getId(),
                                l.getTitulo(),
                                l.getAnio(),
                                l.getIsbn(),
                                l.getAutorNombre(),
                                l.getCategoriaNombre(),
                                l.getEstado() == 1 ? "Activo" : "Inactivo"
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        cargarTabla();
    }

    private void cargarAutoresYCategorias() {
        cboAutor.removeAllItems();
        cboCategoria.removeAllItems();

        List<Autor> autores = autorDAO.listarAutoresActivos();
        for (Autor a : autores) cboAutor.addItem(a);

        List<Categoria> categorias = categoriaDAO.listarActivas();
        for (Categoria c : categorias) cboCategoria.addItem(c);
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

    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tblLibros.getSelectedRow();
        if (row == -1) {
            selectedId = null;
            return;
        }

        selectedId = (Integer) model.getValueAt(row, 0);
        txtNombre.setText((String) model.getValueAt(row, 1));
        txtAnio.setText(String.valueOf(model.getValueAt(row, 2)));
        txtIsbn.setText((String) model.getValueAt(row, 3)); // <-- asignar isbn

        String autorNombre = (String) model.getValueAt(row, 4);
        for (int i = 0; i < cboAutor.getItemCount(); i++) {
            if (cboAutor.getItemAt(i).getNombre().equals(autorNombre)) {
                cboAutor.setSelectedIndex(i);
                break;
            }
        }

        String categoriaNombre = (String) model.getValueAt(row, 5);
        for (int i = 0; i < cboCategoria.getItemCount(); i++) {
            if (cboCategoria.getItemAt(i).getNombre().equals(categoriaNombre)) {
                cboCategoria.setSelectedIndex(i);
                break;
            }
        }

        cboEstado.setSelectedIndex(model.getValueAt(row, 6).equals("Activo") ? 0 : 1);
    }

    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        String anioStr = txtAnio.getText().trim();
        String isbn = txtIsbn.getText().trim();
        if (nombre.isEmpty() || anioStr.isEmpty() || isbn.isEmpty() || cboAutor.getSelectedItem() == null || cboCategoria.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(panelPrincipal, "Completa todos los campos", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(panelPrincipal, "El año debe ser un número válido", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;
        Libro libro = new Libro(
                nombre,
                anio,
                ((Autor) cboAutor.getSelectedItem()).getId(),
                ((Categoria) cboCategoria.getSelectedItem()).getId(),
                estado,
                isbn
        );

        if (libroDAO.agregarLibro(libro)) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    private void onActualizar() {
        if (selectedId == null) return;

        String nombre = txtNombre.getText().trim();
        String anioStr = txtAnio.getText().trim();
        String isbn = txtIsbn.getText().trim();
        if (nombre.isEmpty() || anioStr.isEmpty() || isbn.isEmpty() || cboAutor.getSelectedItem() == null || cboCategoria.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(panelPrincipal, "Completa todos los campos", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(panelPrincipal, "El año debe ser un número válido", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;
        Libro libro = new Libro(
                selectedId,
                nombre,
                anio,
                ((Autor) cboAutor.getSelectedItem()).getId(),
                ((Categoria) cboCategoria.getSelectedItem()).getId(),
                estado,
                isbn
        );

        if (libroDAO.actualizarLibro(libro)) {
            cargarTabla();
        }
    }

    private void onEliminar() {
        if (selectedId == null) return;
        if (libroDAO.desactivarLibro(selectedId)) {
            limpiarFormulario();
            cargarTabla();
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtAnio.setText("");
        txtIsbn.setText(""); // <-- limpiar isbn
        cboAutor.setSelectedIndex(-1);
        cboCategoria.setSelectedIndex(-1);
        cboEstado.setSelectedIndex(0);
        tblLibros.clearSelection();
        selectedId = null;
    }

    private void cargarTabla() {
        List<LibroConAutor> lista = libroDAO.listarTodos();
        model.setRowCount(0);
        for (LibroConAutor l : lista) {
            model.addRow(new Object[]{
                    l.getId(),
                    l.getTitulo(),
                    l.getAnio(),
                    l.getIsbn(),
                    l.getAutorNombre(),
                    l.getCategoriaNombre(),
                    l.getEstado() == 1 ? "Activo" : "Inactivo"
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibroForm form = new LibroForm();
            form.setVisible(true);
        });
    }
}
