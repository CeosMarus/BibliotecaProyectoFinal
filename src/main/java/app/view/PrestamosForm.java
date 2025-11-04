package app.view;

import app.dao.ClienteDAO;
import app.dao.EjemplarDAO;
import app.dao.PrestamoDAO;
import app.model.Cliente;
import app.model.Ejemplar;
import app.model.Prestamo;
import app.model.Libro;
import app.dao.LibroDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PrestamosForm extends JFrame {

    public JPanel panelPrincipal;
    private JComboBox<Cliente> cbCliente;
    private JComboBox<Ejemplar> cbEjemplar;
    private JButton btnGuardar;
    private JButton btnEliminar;
    private JButton btnActualizar;
    private JButton btnLimpiar;
    private JTable tblPrestamos;
    private JButton btnRegresar;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final EjemplarDAO ejemplarDAO = new EjemplarDAO();

    private DefaultTableModel modelo;

    public PrestamosForm() {
        setTitle("Gestión de Préstamos");
        setContentPane(panelPrincipal);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        configurarTabla();
        cargarClientes();
        cargarEjemplares();
        listarPrestamos();

        // Botón Guardar
        btnGuardar.addActionListener(e -> guardarPrestamo());

        // Botón Actualizar
        btnActualizar.addActionListener(e -> actualizarPrestamo());

        // Botón Eliminar (Lógica)
        btnEliminar.addActionListener(e -> eliminarPrestamo());

        // Botón Limpiar
        btnLimpiar.addActionListener(e -> limpiarCampos());

        // Botón Regresar
        btnRegresar.addActionListener(e -> onSalir());
    }

    //Configura tabla
    private void configurarTabla() {

        modelo = new DefaultTableModel(
                new Object[]{
                        "ID",
                        "ID Cliente",
                        "ID Ejemplar",
                        "Fecha Préstamo",
                        "Fecha Vencimiento",
                        "Fecha Devolución",
                        "Estado"
                }, 0
        );
        tblPrestamos.setModel(modelo);
        tblPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // Cargar clientes activos y mostrar "Nombre - NIT"
    private void cargarClientes() {
        try {
            cbCliente.removeAllItems();
            List<Cliente> clientes = clienteDAO.listar();

            for (Cliente c : clientes) {
                if (c.getEstado() == 1) cbCliente.addItem(c);
            }

            cbCliente.setRenderer(new DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {

                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                    if (value instanceof Cliente cliente) {
                        setText(cliente.getNombre() + " - NIT: " + cliente.getNit());
                    } else if (value == null) {
                        setText("");
                    }
                    return this;
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cargar ejemplares activos
    private void cargarEjemplares() {
        try {
            cbEjemplar.removeAllItems();
            List<Ejemplar> ejemplares = ejemplarDAO.listarConLibro();
            for (Ejemplar e : ejemplares) {
                if (e.getEstado() == 1) cbEjemplar.addItem(e);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar ejemplares: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Listar préstamos
    private void listarPrestamos() {
        try {
            modelo.setRowCount(0);
            List<Prestamo> lista = prestamoDAO.listar();

            for (Prestamo p : lista) {
                // Cliente
                Cliente cli = clienteDAO.buscarPorId(p.getIdCliente());
                String clienteNombre = (cli != null) ? cli.getNombre() : ("Cliente #" + p.getIdCliente());

                // Ejemplar + Libro
                Ejemplar ej = ejemplarDAO.buscarPorId(p.getIdEjemplar());
                String libroTitulo = "Ejemplar #" + p.getIdEjemplar();
                if (ej != null && ej.getIdLibro() > 0) {
                    Libro libro = new LibroDAO().obtenerPorId(ej.getIdLibro());
                    if (libro != null) libroTitulo = libro.getTitulo();
                }

                modelo.addRow(new Object[]{
                        p.getId(),
                        clienteNombre,
                        libroTitulo,
                        p.getFechaPrestamo(),
                        p.getFechaVencimiento(),
                        p.getFechaDevolucion() != null ? p.getFechaDevolucion() : "",
                        p.getEstadoDescripcion()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al listar préstamos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Guardar préstamo (corregido para usar constructor válido)
    private void guardarPrestamo() {
        try {
            Cliente cliente = (Cliente) cbCliente.getSelectedItem();
            Ejemplar ejemplar = (Ejemplar) cbEjemplar.getSelectedItem();

            if (cliente == null || ejemplar == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un cliente y un ejemplar.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date fechaPrestamo = new Date();
            Date fechaVencimiento = new Date(fechaPrestamo.getTime() + (7L * 24 * 60 * 60 * 1000)); // +7 días
            Date fechaDevolucion = new Date(fechaPrestamo.getTime() + (7L * 24 * 60 * 60 * 1000)); // +7 días (prevista)

            // ✅ Usa el constructor existente de tu modelo:
            Prestamo p = new Prestamo(
                    cliente.getId(),
                    ejemplar.getId(),
                    fechaPrestamo,
                    fechaVencimiento,
                    1 // estado
            );

            // ✅ y setea la fechaDevolucion prevista:
            p.setFechaDevolucion(fechaDevolucion);

            prestamoDAO.insertar(p);
            JOptionPane.showMessageDialog(this, "Préstamo guardado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            listarPrestamos();
            limpiarCampos();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar préstamo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Actualizar préstamo
    private void actualizarPrestamo() {
        int fila = tblPrestamos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo de la tabla.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = (int) tblPrestamos.getValueAt(fila, 0);
            Prestamo prestamo = prestamoDAO.buscarPorId(id);
            if (prestamo == null) {
                JOptionPane.showMessageDialog(this, "Préstamo no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea marcar este préstamo como devuelto?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmar == JOptionPane.YES_OPTION) {
                boolean ok = prestamoDAO.devolver(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Préstamo devuelto correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    listarPrestamos();
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar préstamo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Eliminar (desactivar)
    private void eliminarPrestamo() {
        int fila = tblPrestamos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo para eliminar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tblPrestamos.getValueAt(fila, 0);
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea desactivar este préstamo?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            try {
                boolean ok = prestamoDAO.eliminar(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Préstamo desactivado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    listarPrestamos();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar préstamo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Limpiar campos
    private void limpiarCampos() {
        cbCliente.setSelectedIndex(-1);
        cbEjemplar.setSelectedIndex(-1);
        tblPrestamos.clearSelection();
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

    // MAIN (para pruebas independientes)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrestamosForm().setVisible(true));
    }
}