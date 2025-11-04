package app.view;

import app.core.Sesion;
import app.dao.DevolucionDAO;
import app.dao.PrestamoDAO;
import app.dao.ClienteDAO;
import app.dao.EjemplarDAO;
import app.dao.LibroDAO;

import app.model.Devolucion;
import app.model.Prestamo;
import app.model.Usuario;
import app.model.Cliente;
import app.model.Ejemplar;
import app.model.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class DevolucionForm extends JFrame {

    public JPanel panelPrincipal;
    private JComboBox<Prestamo> cboPrestamo;
    private JTextField txtFechaDevolucion;
    private JTable tblDevoluciones;
    //private JScrollPane scrollDevoluciones;
    private JButton btnRegistrar;
    private JButton btnAnular;
    private JButton btnSalir;
    private JComboBox<String> cboEstadoCopia;
    private JTextField txtObservaciones;

    private final DevolucionDAO devolucionDAO = new DevolucionDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final EjemplarDAO ejemplarDAO = new EjemplarDAO();
    private final LibroDAO libroDAO = new LibroDAO();

    public DevolucionForm() {

        setTitle("Registro de Devoluciones");
        setContentPane(panelPrincipal);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        //scrollDevoluciones.setViewportView(tblDevoluciones);

        // Opciones de estado de la copia
        cboEstadoCopia.addItem("Nuevo");
        cboEstadoCopia.addItem("Dañado");
        cboEstadoCopia.addItem("Perdido");

        // Fecha actual bloqueada
        txtFechaDevolucion.setText(new java.sql.Date(new Date().getTime()).toString());
        txtFechaDevolucion.setEditable(false);

        cargarPrestamosPendientes();
        cargarTablaGeneral();

        btnRegistrar.addActionListener(e -> registrarDevolucion());
        btnAnular.addActionListener(e -> anularDevolucion());
        btnSalir.addActionListener(e -> onSalir());
    }

    private void cargarPrestamosPendientes() {
        try {
            cboPrestamo.removeAllItems();
            List<Prestamo> lista = prestamoDAO.listarActivos();

            DefaultComboBoxModel<Prestamo> model = new DefaultComboBoxModel<>();

            for (Prestamo p : lista) {
                Cliente c = clienteDAO.buscarPorId(p.getIdCliente());
                Ejemplar ej = ejemplarDAO.buscarPorId(p.getIdEjemplar());
                Libro l = libroDAO.obtenerPorId(ej.getIdLibro());

                String fechaPrestamo = new java.sql.Date(p.getFechaPrestamo().getTime()).toString();

                String label = String.format(
                        "Préstamo #%d | %s | Libro: %s | Fecha: %s",
                        p.getId(),
                        (c != null ? c.getNombre() : "Cliente#" + p.getIdCliente()),
                        (l != null ? l.getTitulo() : "Ejemplar#" + p.getIdEjemplar()),
                        fechaPrestamo
                );

                Prestamo item = new Prestamo(
                        p.getId(), p.getIdCliente(), p.getIdEjemplar(),
                        p.getFechaPrestamo(), p.getFechaVencimiento(), null, p.getEstado()
                ) {
                    @Override
                    public String toString() {
                        return label;
                    }
                };

                model.addElement(item);
            }

            cboPrestamo.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando préstamos: " + e.getMessage());
        }
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

    private void registrarDevolucion() {
        Prestamo p = (Prestamo) cboPrestamo.getSelectedItem();

        if (p == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo.");
            return;
        }

        try {
            Date fecha = java.sql.Date.valueOf(txtFechaDevolucion.getText());
            String estadoCopia = cboEstadoCopia.getSelectedItem().toString();
            String observaciones = txtObservaciones.getText();

            Devolucion d = new Devolucion(
                    p.getId(),
                    fecha,
                    estadoCopia,
                    observaciones,
                    Sesion.getUsuario().getId()
            );

            int id = devolucionDAO.insertar(d);

            if (id > 0) {
                JOptionPane.showMessageDialog(this, "✅ Devolución registrada");
                cargarPrestamosPendientes();
                cargarTablaGeneral();
                txtObservaciones.setText("");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void anularDevolucion() {
        int row = tblDevoluciones.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una devolución para anular");
            return;
        }

        int id = Integer.parseInt(tblDevoluciones.getValueAt(row, 0).toString());
        int opcion = JOptionPane.showConfirmDialog(this, "¿Anular devolución?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            try {
                if (devolucionDAO.anular(id)) {
                    JOptionPane.showMessageDialog(this, "✅ Devolución anulada");
                    cargarTablaGeneral();
                    cargarPrestamosPendientes();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al anular: " + e.getMessage());
            }
        }
    }

    private void cargarTablaGeneral() {
        try {
            List<Devolucion> lista = devolucionDAO.listar();
            DefaultTableModel model = new DefaultTableModel();
            model.setColumnIdentifiers(new String[]{
                    "ID", "Préstamo", "Cliente", "Libro", "Fecha", "Estado"
            });

            for (Devolucion d : lista) {
                Prestamo p = prestamoDAO.buscarPorId(d.getIdPrestamo());
                Cliente c = clienteDAO.buscarPorId(p.getIdCliente());
                Ejemplar ej = ejemplarDAO.buscarPorId(p.getIdEjemplar());
                Libro l = libroDAO.obtenerPorId(ej.getIdLibro());

                model.addRow(new Object[]{
                        d.getId(),
                        d.getIdPrestamo(),
                        (c != null ? c.getNombre() : "Cliente#" + p.getIdCliente()),
                        (l != null ? l.getTitulo() : "Ejemplar#" + p.getIdEjemplar()),
                        d.getFechaDevolucion(),
                        d.getEstado() == 1 ? "Activo" : "Anulado"
                });
            }

            tblDevoluciones.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando tabla: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new DevolucionForm().setVisible(true);
        });
    }
}