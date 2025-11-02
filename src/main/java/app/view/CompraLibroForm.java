package app.view;

import app.dao.CompraLibroDAO;
import app.dao.SolicitudCompraDAO;
import app.model.CompraLibro;
import app.model.SolicitudCompra;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CompraLibroForm extends JFrame {

    private JPanel panel;
    private JComboBox<SolicitudCompra> cboSolicitud;
    private JTextField txtProveedor, txtCostoTotal, txtFechaRecepcion;
    private JButton btnGuardar, btnEliminar, btnSalir, btnLimpiar;
    private JTable tabla;
    private DefaultTableModel modelo;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final CompraLibroDAO compraDAO = new CompraLibroDAO();
    private final SolicitudCompraDAO solicitudDAO = new SolicitudCompraDAO();

    public CompraLibroForm() {
        setTitle("Registro de Compras de Libros");
        setSize(800, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        panel = new JPanel(new BorderLayout());
        setContentPane(panel);

        JPanel top = new JPanel(new GridLayout(4,2,5,5));
        panel.add(top, BorderLayout.NORTH);

        top.add(new JLabel("Solicitud Aprobada:"));
        cboSolicitud = new JComboBox<>();
        cargarSolicitudes();
        top.add(cboSolicitud);

        top.add(new JLabel("Proveedor:"));
        txtProveedor = new JTextField();
        top.add(txtProveedor);

        top.add(new JLabel("Costo Total:"));
        txtCostoTotal = new JTextField();
        top.add(txtCostoTotal);

        top.add(new JLabel("Fecha Recepción (YYYY-MM-DD):"));
        txtFechaRecepcion = new JTextField(sdf.format(new Date()));
        top.add(txtFechaRecepcion);

        JPanel buttons = new JPanel();
        btnGuardar = new JButton("Registrar Compra");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar"); // ✅ botón agregado
        btnSalir = new JButton("Cerrar");

        buttons.add(btnGuardar);
        buttons.add(btnEliminar);
        buttons.add(btnLimpiar); // ✅ agregado al panel
        buttons.add(btnSalir);
        panel.add(buttons, BorderLayout.SOUTH);

        modelo = new DefaultTableModel(new String[]{"ID", "Solicitud", "Proveedor", "Costo", "Fecha"}, 0);
        tabla = new JTable(modelo);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        cargarTabla();

        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnSalir.addActionListener(e -> dispose());
        btnLimpiar.addActionListener(e -> limpiarFormulario()); // ✅ evento asignado
    }

    private void cargarSolicitudes() {
        cboSolicitud.removeAllItems();
        List<SolicitudCompra> lista = solicitudDAO.listarAprobadas();
        for (SolicitudCompra s : lista) cboSolicitud.addItem(s);
    }

    private void limpiarFormulario() {
        txtProveedor.setText("");
        txtCostoTotal.setText("");
        txtFechaRecepcion.setText(sdf.format(new Date()));

        if (cboSolicitud.getItemCount() > 0) {
            cboSolicitud.setSelectedIndex(0);
        }

        tabla.clearSelection();
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (CompraLibro c : compraDAO.listarActivos()) {
            modelo.addRow(new Object[]{
                    c.getId(),
                    c.getIdSolicitud(),
                    c.getProveedor(),
                    c.getCostoTotal(),
                    sdf.format(c.getFechaRecepcion())
            });
        }
    }

    private void guardar() {
        try {
            SolicitudCompra sc = (SolicitudCompra) cboSolicitud.getSelectedItem();
            String proveedor = txtProveedor.getText();
            double costo = Double.parseDouble(txtCostoTotal.getText());
            Date fecha = sdf.parse(txtFechaRecepcion.getText());

            CompraLibro compra = new CompraLibro(null, sc.getId(), proveedor, costo, fecha, 1);

            if (compraDAO.insertar(compra)) {
                solicitudDAO.actualizarEstado(sc.getId(), 4); // ✅ marcar como comprada
                JOptionPane.showMessageDialog(this, "✅ Compra registrada");
                cargarTabla();
                cargarSolicitudes();
                limpiarFormulario();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: Selecciona un registro" + e.getMessage());
        }
    }

    private void eliminar() {
        int row = tabla.getSelectedRow();
        if (row < 0) return;

        int id = (int) tabla.getValueAt(row, 0);
        compraDAO.eliminarLogico(id);
        cargarTabla();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompraLibroForm().setVisible(true));
    }
}
