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

        panelPrincipal.setPreferredSize(new Dimension(1000, 450));
        tbSolicitudes.setModel(model);
        tbSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Estados válidos
        cboEstado.addItem("1 - Pendiente");
        cboEstado.addItem("2 - Aprobada");
        cboEstado.addItem("3 - Rechazada");

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

    /** ✅ Cargar SOLO usuarios no cliente (Admin/Bibliotecario) */
    private void cargarUsuariosEnCombo() {
        try {
            cboUsuario.removeAllItems();
            List<Usuario> lista = usuarioDAO.listar();

            // Filtramos solo ADMIN y BIBLIOTECARIO
            for (Usuario u : lista) {
                if (u.getRol().equalsIgnoreCase("ADMIN") ||
                        u.getRol().equalsIgnoreCase("BIBLIOTECARIO")) {
                    cboUsuario.addItem(u);
                }
            }

            if (cboUsuario.getItemCount() == 0) {
                JOptionPane.showMessageDialog(null, "No hay usuarios administrativos registrados.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar usuarios: " + e.getMessage());
        }
    }

    /** ✅ Cargar libros activos */
    private void cargarLibrosEnCombo() {
        try {
            cboLibro.removeAllItems();
            List<Libro> libros = libroDAO.listar();
            for (Libro l : libros) {
                cboLibro.addItem(l);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar libros: " + e.getMessage());
        }
    }

    /** ✅ Guardar solicitud */
    private void onGuardar() {
        try {
            Date fecha = parseFecha(txtFecha.getText());
            Usuario usuario = (Usuario) cboUsuario.getSelectedItem();
            Libro libro = (Libro) cboLibro.getSelectedItem();

            if (usuario == null || libro == null) {
                JOptionPane.showMessageDialog(null, "Debe seleccionar un usuario y un libro.");
                return;
            }

            int cantidad = Integer.parseInt(txtCantidad.getText());
            double costoUnitario = Double.parseDouble(txtCostoUnitario.getText());
            int estado = obtenerEstado();

            SolicitudCompra solicitud = new SolicitudCompra(
                    fecha, usuario.getId(), libro.getId(), cantidad, costoUnitario, estado
            );

            if (solicitudDAO.insertar(solicitud)) {
                JOptionPane.showMessageDialog(null, "✅ Solicitud creada correctamente.");
                limpiarFormulario();
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(null, "❌ Error al guardar.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    /** ✅ Actualizar */
    private void onActualizar() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(null, "Seleccione una solicitud.");
            return;
        }

        try {
            Date fecha = parseFecha(txtFecha.getText());
            Usuario usuario = (Usuario) cboUsuario.getSelectedItem();
            Libro libro = (Libro) cboLibro.getSelectedItem();
            int cantidad = Integer.parseInt(txtCantidad.getText());
            double costoUnitario = Double.parseDouble(txtCostoUnitario.getText());
            int estado = obtenerEstado();

            SolicitudCompra sc = new SolicitudCompra(
                    selectedId, fecha, usuario.getId(), libro.getId(), cantidad, costoUnitario, estado
            );

            if (solicitudDAO.actualizar(sc)) {
                JOptionPane.showMessageDialog(null, "✅ Actualizado correctamente.");
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(null, "❌ Error al actualizar.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    /** ✅ Eliminar (marcar estado 0) */
    private void onEliminar() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(null, "Seleccione una solicitud.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Desea eliminar esta solicitud?",
                "Confirmación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (solicitudDAO.cambiarEstado(selectedId, 0)) {
                JOptionPane.showMessageDialog(null, "✅ Eliminada correctamente.");
                limpiarFormulario();
                cargarTabla();
            }
        }
    }

    /** ✅ Llenar tabla */
    private void cargarTabla() {
        model.setRowCount(0);
        List<SolicitudCompra> lista = solicitudDAO.listar();

        for (SolicitudCompra s : lista) {
            model.addRow(new Object[]{
                    s.getId(),
                    sdf.format(s.getFecha()),
                    obtenerNombreUsuario(s.getIdUsuario()),
                    obtenerTituloLibro(s.getIdLibro()),
                    s.getCantidad(),
                    s.getCostoUnitario(),
                    s.getEstadoTexto()
            });
        }
    }

    /** ✅ Selección tabla */
    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        int row = tbSolicitudes.getSelectedRow();
        if (row == -1) return;

        selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
        txtFecha.setText(model.getValueAt(row, 1).toString());
        txtCantidad.setText(model.getValueAt(row, 4).toString());
        txtCostoUnitario.setText(model.getValueAt(row, 5).toString());

        seleccionarComboUsuario(model.getValueAt(row, 2).toString());
        seleccionarComboLibro(model.getValueAt(row, 3).toString());
        seleccionarEstado(model.getValueAt(row, 6).toString());
    }

    /** ✅ Utilidades */
    private String obtenerNombreUsuario(int id) {
        try {
            for (Usuario u : usuarioDAO.listar())
                if (u.getId() == id) return u.getNombre();
        } catch (Exception ignored) {}
        return "---";
    }

    private String obtenerTituloLibro(int id) {
        try {
            for (Libro l : libroDAO.listar())
                if (l.getId() == id) return l.getTitulo();
        } catch (Exception ignored) {}
        return "---";
    }

    private void seleccionarComboUsuario(String nombre) {
        for (int i = 0; i < cboUsuario.getItemCount(); i++)
            if (cboUsuario.getItemAt(i).getNombre().equals(nombre))
                cboUsuario.setSelectedIndex(i);
    }

    private void seleccionarComboLibro(String titulo) {
        for (int i = 0; i < cboLibro.getItemCount(); i++)
            if (cboLibro.getItemAt(i).getTitulo().equals(titulo))
                cboLibro.setSelectedIndex(i);
    }

    private void seleccionarEstado(String estadoTexto) {
        switch (estadoTexto) {
            case "Pendiente" -> cboEstado.setSelectedIndex(0);
            case "Aprobada" -> cboEstado.setSelectedIndex(1);
            case "Rechazada" -> cboEstado.setSelectedIndex(2);
        }
    }

    private int obtenerEstado() {
        return cboEstado.getSelectedIndex() + 1;
    }

    private Date parseFecha(String texto) throws ParseException {
        return sdf.parse(texto);
    }

    private void limpiarFormulario() {
        txtFecha.setText("");
        txtCantidad.setText("");
        txtCostoUnitario.setText("");
        tbSolicitudes.clearSelection();
        selectedId = null;
    }

    private void onSalir() {
        Window w = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (w != null) w.dispose();
    }

    /** ✅ MAIN de pruebas */
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
}
