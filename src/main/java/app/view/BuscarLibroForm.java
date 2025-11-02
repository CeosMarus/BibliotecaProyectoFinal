package app.view;

import app.dao.LibroDAO;
import app.model.LibroConAutor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BuscarLibroForm extends JFrame {

    private JPanel panelPrincipal;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JTable tblLibros;
    private JButton btnSalir;

    private final LibroDAO libroDAO = new LibroDAO();
    private DefaultTableModel modelo;

    public BuscarLibroForm() {
        setTitle("Buscar Libros");
        setContentPane(panelPrincipal);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        configurarTabla();

        btnBuscar.addActionListener(e -> buscarLibros());
        btnSalir.addActionListener(e -> dispose());
    }

    private void configurarTabla() {
        modelo = new DefaultTableModel();
        modelo.setColumnIdentifiers(new String[]{
                "ID", "Título", "Autor", "Categoría", "ISBN", "Año"
        });
        tblLibros.setModel(modelo);
        tblLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void buscarLibros() {
        String texto = txtBuscar.getText().trim();

        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ingrese texto para buscar.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<LibroConAutor> lista = libroDAO.buscarPorTitulo(texto);
            modelo.setRowCount(0);

            for (LibroConAutor l : lista) {
                modelo.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getAutorNombre(),       // ✅ corregido
                        l.getCategoriaNombre(),   // ✅ corregido
                        l.getIsbn(),
                        l.getAnio()
                });
            }

            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron libros.",
                        "Sin resultados",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar libros: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            BuscarLibroForm form = new BuscarLibroForm();
            form.setVisible(true);
        });
    }
}
