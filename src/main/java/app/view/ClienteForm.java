package app.view;

import app.dao.ClienteDAO;
import app.model.Cliente;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ClienteForm {
    // Panel Principal
    public JPanel panelPrincipal;

    // Cuadros de texto
    private JTextField txtNombre;
    private JTextField txtNit;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtBuscar;

    // Combo Box
    private JComboBox<String> cboEstado;

    // Botones
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnCargar;
    private JButton btnLimpiar;
    private JButton btnSalir;

    // Tabla
    private JTable tbClientes;

    // DAO y Modelo
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "NIT", "Teléfono", "Correo", "Estado"}, 0
    );

    // Selección de tabla
    private Integer selectedId = null;

    // Usuario actual para control de roles
    private final Usuario usuarioActual;

    public ClienteForm(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;

        panelPrincipal.setPreferredSize(new Dimension(900, 600));
        tbClientes.setModel(model);
        tbClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurar combo de estado
        cboEstado.addItem("1 - Activo");
        cboEstado.addItem("0 - Desactivado");

        // Listeners de botones
        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnCargar.addActionListener(e -> cargarTabla());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnSalir.addActionListener(e -> onSalir());

        // Listener selección tabla
        tbClientes.getSelectionModel().addListSelectionListener(this::onTableSelection);

        // Búsqueda en tiempo real
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { buscar(); }
            public void removeUpdate(DocumentEvent e) { buscar(); }
            public void changedUpdate(DocumentEvent e) { buscar(); }

            private void buscar() {
                String texto = txtBuscar.getText().trim();
                List<Cliente> lista = texto.isEmpty()
                        ? clienteDAO.listar()
                        : clienteDAO.buscarPorNombre(texto);
                actualizarTabla(lista);
            }
        });

        // Cargar tabla inicial
        cargarTabla();

        // Configurar permisos según el rol
        configurarPermisosPorRol();
    }

    /** Configura permisos según el rol del usuario */
    private void configurarPermisosPorRol() {
        if (usuarioActual == null) return;
        String rol = usuarioActual.getRol().toUpperCase();

        switch (rol) {
            case "ADMIN":
                // Acceso total
                break;
            case "BIBLIOTECARIO":
                // Puede registrar, actualizar y buscar, pero no eliminar
                btnEliminar.setEnabled(false);
                break;
            case "FINANCIERO":
            case "CLIENTE":
                // Solo lectura
                btnGuardar.setEnabled(false);
                btnActualizar.setEnabled(false);
                btnEliminar.setEnabled(false);
                break;
        }
    }

    /** Listener de selección en tabla */
    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tbClientes.getSelectedRow();
        if (row == -1) {
            selectedId = null;
            return;
        }

        selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
        txtNombre.setText(model.getValueAt(row, 1).toString());
        txtNit.setText(model.getValueAt(row, 2).toString());
        txtTelefono.setText(model.getValueAt(row, 3).toString());
        txtCorreo.setText(model.getValueAt(row, 4).toString());
        String estadoTxt = model.getValueAt(row, 5).toString();
        cboEstado.setSelectedIndex("1".equalsIgnoreCase(estadoTxt) || estadoTxt.contains("Activo") ? 0 : 1);
    }

    /** Guardar nuevo cliente */
    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        String nit = txtNit.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        int estado = (cboEstado.getSelectedIndex() == 0) ? 1 : 0;

        if (nombre.isEmpty() || nit.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        try {
            if (clienteDAO.existeNit(nit, 0)) {
                mostrarError("El NIT ya está registrado.");
                return;
            }

            Cliente nuevoCliente = new Cliente(nombre, nit, telefono, correo, estado);
            int idGenerado = clienteDAO.insertar(nuevoCliente);

            if (idGenerado > 0) {
                limpiarFormulario();
                cargarTabla();
                mostrarInfo("Cliente guardado exitosamente con ID: " + idGenerado);
            } else {
                mostrarError("No se pudo guardar el cliente.");
            }

        } catch (SQLException ex) {
            mostrarError("Error al guardar el cliente: " + ex.getMessage());
        }
    }

    /** Actualizar cliente existente */
    private void onActualizar() {
        if (selectedId == null) {
            mostrarError("Seleccione un cliente para actualizar.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String nit = txtNit.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        int estado = (cboEstado.getSelectedIndex() == 0) ? 1 : 0;

        if (nombre.isEmpty() || nit.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            mostrarError("Todos los campos son obligatorios.");
            return;
        }

        try {
            if (clienteDAO.existeNit(nit, selectedId)) {
                mostrarError("El NIT ya está registrado en otro cliente.");
                return;
            }

            Cliente cliente = new Cliente(selectedId, nombre, nit, telefono, correo, estado);
            boolean ok = clienteDAO.actualizar(cliente);
            if (ok) {
                cargarTabla();
                seleccionarFilaPorId(selectedId);
                mostrarInfo("Cliente actualizado exitosamente.");
            } else {
                mostrarError("No se pudo actualizar el cliente.");
            }

        } catch (SQLException ex) {
            mostrarError("Error al actualizar el cliente: " + ex.getMessage());
        }
    }

    /** Eliminar (lógicamente) cliente */
    private void onEliminar() {
        if (selectedId == null) {
            mostrarError("Seleccione un cliente para eliminar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(panelPrincipal,
                "¿Está seguro que desea eliminar (desactivar) este cliente?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 🔹 Eliminación lógica
                boolean ok = clienteDAO.cambiarEstado(selectedId, 0);
                if (ok) {
                    cargarTabla();
                    limpiarFormulario();
                    mostrarInfo("Cliente desactivado exitosamente.");
                } else {
                    mostrarError("No se pudo desactivar el cliente.");
                }
            } catch (Exception ex) {
                mostrarError("Error al eliminar cliente: " + ex.getMessage());
            }
        }
    }

    /** Cargar tabla */
    private void cargarTabla() {
        List<Cliente> lista = clienteDAO.listar();
        actualizarTabla(lista);
    }

    /** Actualiza tabla visual */
    private void actualizarTabla(List<Cliente> lista) {
        model.setRowCount(0);
        for (Cliente c : lista) {
            if (c.getEstado() == 1) { // solo activos
                model.addRow(new Object[]{
                        c.getId(),
                        c.getNombre(),
                        c.getNit(),
                        c.getTelefono(),
                        c.getCorreo(),
                        "1 - Activo"
                });
            }
        }
    }

    /** Limpia formulario */
    private void limpiarFormulario() {
        txtNombre.setText("");
        txtNit.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        cboEstado.setSelectedIndex(0);
        txtBuscar.setText("");
        tbClientes.clearSelection();
        selectedId = null;
    }

    /** Seleccionar fila por ID */
    private void seleccionarFilaPorId(Integer id) {
        if (id == null) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 0);
            if (val != null && Integer.parseInt(val.toString()) == id) {
                tbClientes.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    /** Botón Salir */
    private void onSalir() {
        if (JOptionPane.showConfirmDialog(panelPrincipal, "¿Deseas cerrar el formulario?",
                "Confirmar salida", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (window != null) {
                window.dispose(); // Cierra solo esta ventana
            }
        }
    }


    /** Utilidades */
    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(panelPrincipal, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(panelPrincipal, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Main de prueba */
    public static void main(String[] args) {
        Usuario u = new Usuario(1, "admin", "Administrador", "ADMIN", 1);
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Gestión de Clientes");
            f.setContentPane(new ClienteForm(u).panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    private void createUIComponents() {
        // personalización de componentes
    }
}