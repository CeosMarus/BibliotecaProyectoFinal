package app.view;

// Importaciones DAO
import app.dao.CompraLibroDAO;
import app.dao.SolicitudCompraDAO;
import app.dao.UsuarioDAO;
import app.dao.LibroDAO;
//Importaciones de modelos
import app.model.CompraLibro;
import app.model.SolicitudCompra;
import app.model.SolicitudCompraVista;
// Librerías Swing y utilitarias
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CompraLibroForm {
    // ----- Componentes -----
    public JPanel panelPrincipal;
    private JComboBox<SolicitudCompraVista> cboSolicitud;
    private JTextField txtProveedor;
    private JTextField txtCostoTotal;
    private JTextField txtFechaRecepcion;
    private JComboBox<String> cboEstado;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnCargar;
    private JButton btnLimpiar;
    private JButton btnSalir;
    private JTable tbCompras;

    // DAOs

    private final CompraLibroDAO compraDAO = new CompraLibroDAO();
    private final SolicitudCompraDAO solicitudDAO = new SolicitudCompraDAO();
    //  Modelo de tabla

    private final DefaultTableModel model = new DefaultTableModel(

            new Object[]{"ID", "Solicitud", "Proveedor", "Costo Total", "Fecha Recepción", "Estado"}, 0

    );

    // Variables de control

    private Integer selectedId = null;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    //Constructor

    public CompraLibroForm() {

        panelPrincipal.setPreferredSize(new Dimension(950, 450));
        tbCompras.setModel(model);
        tbCompras.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cboEstado.addItem("1 - Activo");
        cboEstado.addItem("0 - Inactivo");
        cargarSolicitudesEnCombo();
        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnCargar.addActionListener(e -> cargarTabla());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnSalir.addActionListener(e -> onSalir());
        tbCompras.getSelectionModel().addListSelectionListener(this::onTableSelection);
        cargarTabla();

    }



    // Cargar solicitudes (legibles en el ComboBox)
    private void cargarSolicitudesEnCombo() {

        try {
            cboSolicitud.removeAllItems();
            List<SolicitudCompra> solicitudes = solicitudDAO.listar(); // solo activas
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            LibroDAO libroDAO = new LibroDAO();
            var usuarios = usuarioDAO.listar();
            var libros = libroDAO.listarTodos();
            for (SolicitudCompra s : solicitudes) {
                if (s.getEstado() == 1) { // solo solicitudes activas
                    String nombreUsuario = usuarios.stream()
                            .filter(u -> u.getId() == s.getIdUsuario())
                            .map(u -> u.getNombre())
                            .findFirst().orElse("Usuario #" + s.getIdUsuario());
                    String tituloLibro = libros.stream()
                            .filter(l -> l.getId() == s.getIdLibro())
                            .map(l -> l.getTitulo())
                            .findFirst().orElse("Libro #" + s.getIdLibro());
                    String descripcion = String.format(
                            "#%d | %s | %s | \"%s\" | Cant: %d | Q%.2f",
                            s.getId(),
                            new java.text.SimpleDateFormat("yyyy-MM-dd").format(s.getFecha()),
                            nombreUsuario,
                            tituloLibro,
                            s.getCantidad(),
                            s.getCostoUnitario()
                    );
                    cboSolicitud.addItem(new SolicitudCompraVista(s.getId(), descripcion));
                }
            }



        } catch (Exception e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(panelPrincipal,

                    "Error al cargar solicitudes: " + e.getMessage(),

                    "Error",

                    JOptionPane.ERROR_MESSAGE);

        }

    }



    // Guardar nueva compra

    private void onGuardar() {

        try {

            SolicitudCompraVista solicitudVista = (SolicitudCompraVista) cboSolicitud.getSelectedItem();

            String proveedor = txtProveedor.getText().trim();

            double costo = Double.parseDouble(txtCostoTotal.getText().trim());

            Date fecha = parseFecha(txtFechaRecepcion.getText().trim());

            int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;

            if (solicitudVista == null) {

                mostrarError("Debe seleccionar una solicitud de compra.");

                return;

            }

            if (proveedor.isEmpty() || costo <= 0) {

                mostrarError("Debe ingresar proveedor y un costo válido.");

                return;

            }

            CompraLibro nueva = new CompraLibro(0, solicitudVista.getId(), proveedor, costo, fecha, estado);
            boolean ok = compraDAO.insertar(nueva);

            if (ok) {

                mostrarInfo("Compra registrada correctamente.");

                limpiarFormulario();

                cargarTabla();

            } else {

                mostrarError("No se pudo registrar la compra.");

            }

        } catch (NumberFormatException ex) {

            mostrarError("El costo debe ser un número válido.");

        } catch (ParseException ex) {

            mostrarError("Formato de fecha inválido (use yyyy-MM-dd).");

        }

    }

    // Actualizar compra existente

    private void onActualizar() {

        if (selectedId == null) {

            mostrarError("Seleccione una compra para actualizar.");

            return;

        }

        try {

            SolicitudCompraVista solicitudVista = (SolicitudCompraVista) cboSolicitud.getSelectedItem();
            String proveedor = txtProveedor.getText().trim();
            double costo = Double.parseDouble(txtCostoTotal.getText().trim());
            Date fecha = parseFecha(txtFechaRecepcion.getText().trim());
            int estado = cboEstado.getSelectedIndex() == 0 ? 1 : 0;
            if (solicitudVista == null) {
                mostrarError("Debe seleccionar una solicitud de compra.");
                return;
            }

            CompraLibro compra = new CompraLibro(selectedId, solicitudVista.getId(), proveedor, costo, fecha, estado);
            boolean ok = compraDAO.actualizar(compra);

            if (ok) {

                mostrarInfo("Compra actualizada correctamente.");
                cargarTabla();
                seleccionarFilaPorId(selectedId);
            } else {
                mostrarError("No se pudo actualizar la compra.");
            }

        } catch (NumberFormatException ex) {
            mostrarError("El costo debe ser numérico.");
        } catch (ParseException ex) {
            mostrarError("Formato de fecha inválido (use yyyy-MM-dd).");
        }
    }

    // Eliminación lógica
    private void onEliminar() {
        if (selectedId == null) {
            mostrarError("Seleccione una compra para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(panelPrincipal,
                "¿Está seguro de desactivar esta compra?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = compraDAO.eliminarLogico(selectedId);
            if (ok) {
                mostrarInfo("Compra desactivada correctamente.");
                limpiarFormulario();
                cargarTabla();
            } else {
                mostrarError("No se pudo desactivar la compra.");
            }
        }
    }
    //  Selección en tabla

    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tbCompras.getSelectedRow();
        if (row == -1) {
            selectedId = null;
            return;
        }

        selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
        seleccionarSolicitudPorTexto(model.getValueAt(row, 1).toString());
        txtProveedor.setText(model.getValueAt(row, 2).toString());
        txtCostoTotal.setText(model.getValueAt(row, 3).toString());
        txtFechaRecepcion.setText(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
        cboEstado.setSelectedIndex(model.getValueAt(row, 5).toString().startsWith("1") ? 0 : 1);
    }



    // Cargar tabla
    private void cargarTabla() {
        List<CompraLibro> lista = compraDAO.listarActivos();
        model.setRowCount(0);
        for (CompraLibro c : lista) {
            String solicitudTxt = "Solicitud #" + c.getIdSolicitud();
            model.addRow(new Object[]{
                    c.getId(),
                    solicitudTxt,
                    c.getProveedor(),
                    c.getCostoTotal(),
                    c.getFechaRecepcion() != null ? sdf.format(c.getFechaRecepcion()) : "",
                    c.getEstado() == 1 ? "1 - Activo" : "0 - Inactivo"
            });
        }
    }



    // Seleccionar solicitud en ComboBox

    private void seleccionarSolicitudPorTexto(String texto) {
        for (int i = 0; i < cboSolicitud.getItemCount(); i++) {
            SolicitudCompraVista s = cboSolicitud.getItemAt(i);
            if (texto.contains(String.valueOf(s.getId()))) {
                cboSolicitud.setSelectedIndex(i);
                return;
            }
        }
    }

    //Limpiar formulario
    private void limpiarFormulario() {
        txtProveedor.setText("");
        txtCostoTotal.setText("");
        txtFechaRecepcion.setText("");
        if (cboSolicitud.getItemCount() > 0) cboSolicitud.setSelectedIndex(0);
        cboEstado.setSelectedIndex(0);
        tbCompras.clearSelection();
        selectedId = null;
    }

    //  Utilidades

    private Date parseFecha(String texto) throws ParseException {
        if (texto == null || texto.isEmpty()) return null;
        return sdf.parse(texto);
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(panelPrincipal, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(panelPrincipal, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void seleccionarFilaPorId(Integer id) {
        if (id == null) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 0);
            if (val != null && Integer.parseInt(val.toString()) == id) {
                tbCompras.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void onSalir() {
        Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (window != null) window.dispose();
    }

    //  Main de prueba
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Gestión de Compras de Libros - Cliente");
            f.setContentPane(new CompraLibroForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}