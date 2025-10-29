package app.view;

import app.dao.LibroDAO;
import app.dao.SolicitudCompraDAO;
import app.dao.UsuarioDAO;
import app.model.Libro;
import app.model.SolicitudCompra;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SolicitudCompraForm {

    public JPanel panelPrincipal;
    private JTextField txtFecha;
    private JTextField txtCantidad;
    private JTextField txtCostoUnitario;
    private JComboBox<Usuario> cboUsuario;
    private JComboBox<Libro> cboLibro;
    private JComboBox<String> cboEstado;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnCargar;
    private JButton btnLimpiar;
    private JButton btnSalir;

    private JTable tbSolicitudes;

    private final SolicitudCompraDAO solicitudDAO = new SolicitudCompraDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LibroDAO libroDAO = new LibroDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Fecha", "Usuario", "Libro", "Cantidad", "Costo Unitario", "Estado"}, 0
    );

    private Integer selectedId = null;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public SolicitudCompraForm() {
        panelPrincipal.setPreferredSize(new Dimension(1050, 400));
        tbSolicitudes.setModel(model);
        tbSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cboEstado.addItem("1 - Activo");
        cboEstado.addItem("0 - Inactivo");

        cargarUsuariosEnCombo();
        cargarLibrosEnCombo();

        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnCargar.addActionListener(e -> cargarTabla());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnSalir.addActionListener(e -> onSalir());

        tbSolicitudes.getSelectionModel().addListSelectionListener(this::onTableSelection);

        cargarTabla();
    }

    /** 🔹 Cargar SOLO clientes activos en combo */
    private void cargarUsuariosEnCombo() {
        try {
            cboUsuario.removeAllItems();
            List<Usuario> clientes = usuarioDAO.listarClientes(); // Nuevo método en UsuarioDAO
            for (Usuario u : clientes) {
                cboUsuario.addItem(u);
            }

            if (clientes.isEmpty()) {
                JOptionPane.showMessageDialog(panelPrincipal,
                        "No hay clientes registrados o activos.",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Error al cargar los clientes: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 🔹 Cargar libros activos en combo */
    private void cargarLibrosEnCombo() {
        try {
            cboLibro.removeAllItems();
            List<Libro> libros = libroDAO.listar();
            for (Libro l : libros) {
                cboLibro.addItem(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 🔹 Guardar nueva solicitud */
    private void onGuardar() {
        try {
            Date fecha = parseFecha(txtFecha.getText().trim());
            Usuario usuario = (Usuario) cboUsuario.getSelectedItem();
            Libro libro = (Libro) cboLibro.getSelectedItem();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double costo = Double.parseDouble(txtCostoUnitario.getText().trim());
            int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;

            if (usuario == null || libro == null) {
                mostrarError("Debe seleccionar un usuario y un libro.");
                return;
            }

            if (cantidad <= 0 || costo <= 0) {
                mostrarError("La cantidad y el costo deben ser mayores que 0.");
                return;
            }

            SolicitudCompra nueva = new SolicitudCompra(fecha, usuario.getId(), libro.getId(), cantidad, costo, estado);
            boolean ok = solicitudDAO.insertar(nueva);

            if (ok) {
                mostrarInfo("Solicitud registrada correctamente.");
                limpiarFormulario();
                cargarTabla();
            } else {
                mostrarError("No se pudo registrar la solicitud.");
            }

        } catch (NumberFormatException ex) {
            mostrarError("Verifique que los campos numéricos sean válidos.");
        } catch (ParseException ex) {
            mostrarError("Formato de fecha inválido. Use yyyy-MM-dd.");
        }
    }

    /** 🔹 Actualizar solicitud existente */
    private void onActualizar() {
        if (selectedId == null) {
            mostrarError("Seleccione una solicitud para actualizar.");
            return;
        }

        try {
            Date fecha = parseFecha(txtFecha.getText().trim());
            Usuario usuario = (Usuario) cboUsuario.getSelectedItem();
            Libro libro = (Libro) cboLibro.getSelectedItem();
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double costo = Double.parseDouble(txtCostoUnitario.getText().trim());
            int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;

            SolicitudCompra sc = new SolicitudCompra(selectedId, fecha, usuario.getId(), libro.getId(), cantidad, costo, estado);
            boolean ok = solicitudDAO.actualizar(sc);

            if (ok) {
                mostrarInfo("Solicitud actualizada correctamente.");
                cargarTabla();
                seleccionarFilaPorId(selectedId);
            } else {
                mostrarError("No se pudo actualizar la solicitud.");
            }

        } catch (NumberFormatException ex) {
            mostrarError("Verifique los campos numéricos.");
        } catch (ParseException ex) {
            mostrarError("Formato de fecha inválido. Use yyyy-MM-dd.");
        }
    }

    /** 🔹 Eliminación lógica */
    private void onEliminar() {
        if (selectedId == null) {
            mostrarError("Seleccione una solicitud para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(panelPrincipal,
                "¿Está seguro de eliminar (desactivar) esta solicitud?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = solicitudDAO.eliminar(selectedId);
            if (ok) {
                mostrarInfo("Solicitud desactivada correctamente.");
                limpiarFormulario();
                cargarTabla();
            } else {
                mostrarError("No se pudo desactivar la solicitud.");
            }
        }
    }

    /** 🔹 Selección de fila */
    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tbSolicitudes.getSelectedRow();
        if (row == -1) {
            selectedId = null;
            return;
        }

        selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
        txtFecha.setText(model.getValueAt(row, 1).toString());
        txtCantidad.setText(model.getValueAt(row, 4).toString());
        txtCostoUnitario.setText(model.getValueAt(row, 5).toString());

        seleccionarUsuarioPorNombre(model.getValueAt(row, 2).toString());
        seleccionarLibroPorTitulo(model.getValueAt(row, 3).toString());

        String estadoTxt = model.getValueAt(row, 6).toString();
        cboEstado.setSelectedIndex(estadoTxt.startsWith("1") ? 0 : 1);
    }

    /** 🔹 Cargar tabla */
    private void cargarTabla() {
        List<SolicitudCompra> lista = solicitudDAO.listar();
        model.setRowCount(0);

        for (SolicitudCompra s : lista) {
            if (s.getEstado() != 0) {
                String usuarioNombre = obtenerNombreUsuarioPorId(s.getIdUsuario());
                String libroTitulo = obtenerTituloLibroPorId(s.getIdLibro());

                model.addRow(new Object[]{
                        s.getId(),
                        sdf.format(s.getFecha()),
                        usuarioNombre,
                        libroTitulo,
                        s.getCantidad(),
                        s.getCostoUnitario(),
                        s.getEstado() == 1 ? "1 - Activo" : "0 - Inactivo"
                });
            }
        }
    }

    /** 🔹 Buscar nombres por ID — Solo clientes */
    private String obtenerNombreUsuarioPorId(int id) {
        try {
            List<Usuario> clientes = usuarioDAO.listarClientes();
            for (Usuario u : clientes) {
                if (u.getId() == id) return u.getNombre();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Desconocido";
    }

    private String obtenerTituloLibroPorId(int id) {
        try {
            for (Libro l : libroDAO.listar()) {
                if (l.getId() == id) return l.getTitulo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Desconocido";
    }

    /** 🔹 Seleccionar usuario/libro en combos */
    private void seleccionarUsuarioPorNombre(String nombre) {
        for (int i = 0; i < cboUsuario.getItemCount(); i++) {
            Usuario u = cboUsuario.getItemAt(i);
            if (u.getNombre().equalsIgnoreCase(nombre)) {
                cboUsuario.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarLibroPorTitulo(String titulo) {
        for (int i = 0; i < cboLibro.getItemCount(); i++) {
            Libro l = cboLibro.getItemAt(i);
            if (l.getTitulo().equalsIgnoreCase(titulo)) {
                cboLibro.setSelectedIndex(i);
                return;
            }
        }
    }

    /** 🔹 Limpieza y utilidades */
    private void limpiarFormulario() {
        txtFecha.setText("");
        txtCantidad.setText("");
        txtCostoUnitario.setText("");
        if (cboUsuario.getItemCount() > 0) cboUsuario.setSelectedIndex(0);
        if (cboLibro.getItemCount() > 0) cboLibro.setSelectedIndex(0);
        cboEstado.setSelectedIndex(0);
        tbSolicitudes.clearSelection();
        selectedId = null;
    }

    private void seleccionarFilaPorId(Integer id) {
        if (id == null) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 0);
            if (val != null && Integer.parseInt(val.toString()) == id) {
                tbSolicitudes.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void onSalir() {
        Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (window != null) window.dispose();
    }

    private Date parseFecha(String texto) throws ParseException {
        return sdf.parse(texto);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(panelPrincipal, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(panelPrincipal, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /** 🔹 Main de prueba */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Gestión de Solicitudes de Compra");
            f.setContentPane(new SolicitudCompraForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    private void createUIComponents() {
        // Personalización para IntelliJ GUI Designer
    }
}