package app.view;

import app.dao.ClienteDAO;
import app.model.Cliente;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

//Formulario principal para la gestión de Clientes Sigue el estilo de UsuariosForm

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
    //private JButton btnBuscar;

    // Tabla
    private JTable tbClientes;

    // DAO y Modelo
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "NIT", "Teléfono", "Correo", "Estado"}, 0
    );

    //Seleccion de la tabla
    private Integer selectedId = null;

    public ClienteForm() {
        panelPrincipal.setPreferredSize(new Dimension(900, 600));
        tbClientes.setModel(model);
        tbClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurar combo de estado
        cboEstado.addItem("1 - Activo");
        cboEstado.addItem("0 - Bloqueado");

        // Listeners de botones
        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnCargar.addActionListener(e -> cargarTabla());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        // Listener selecction tabla
        tbClientes.getSelectionModel().addListSelectionListener(this::onTableSelection);

        // Búsqueda en tiempo real
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                buscar();
            }
            public void removeUpdate(DocumentEvent e) {
                buscar();
            }
            public void changedUpdate(DocumentEvent e) {
                buscar();
            }

            private void buscar() {
                String texto = txtBuscar.getText().trim();
                List<Cliente> lista = texto.isEmpty() ? clienteDAO.listar() : clienteDAO.buscarPorNombre(texto);
                model.setRowCount(0);
                for (Cliente c : lista) {
                    model.addRow(new Object[]{
                            c.getId(),
                            c.getNombre(),
                            c.getNit(),
                            c.getTelefono(),
                            c.getCorreo(),
                            c.getEstado() == 1 ? "1 - Activo" : "0 - Bloqueado"
                    });
                }
            }
            // Cargar tabla inicial
        });
        cargarTabla();
    }

    // Listener de selección en tabla

    // tbClientes.getSelectionModel().addListSelectionListener(e -> {
    private void onTableSelection(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tbClientes.getSelectedRow();
        if (row == -1) {
            selectedId = null;
            //limpiarCampos();
            return;
        }

        selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
        txtNombre.setText(model.getValueAt(row, 1).toString());
        txtNit.setText(model.getValueAt(row, 2).toString());
        txtTelefono.setText(model.getValueAt(row, 3).toString());
        txtCorreo.setText(model.getValueAt(row, 4).toString());

        String estadoTxt = model.getValueAt(row, 5).toString();
        cboEstado.setSelectedIndex("Activo".equalsIgnoreCase(estadoTxt) ? 0 : 1);


        // Cargar tabla inicial
        // cargarTabla();
    }

    /**
     * Guarda un nuevo cliente
     */
    private void onGuardar() {
        String nombre = txtNombre.getText().trim();
        String nit = txtNit.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        int estado = (cboEstado.getSelectedIndex() == 0) ? 1 : 0;

        // Validaciones
        if (nombre.isEmpty() || nit.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Todos los campos son obligatorios.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (nombre.length() < 3) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "El nombre debe tener al menos 3 caracteres",
                    "Error en Nombre",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!validarNIT(nit)) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "El NIT debe contener solo números y guiones",
                    "Error en NIT",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!validarTelefono(telefono)) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "El teléfono debe tener entre 8 y 15 dígitos",
                    "Error en Teléfono",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!validarCorreo(correo)) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "El formato del correo no es válido",
                    "Error en Correo",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Verificar NIT duplicado
            if (clienteDAO.existeNit(nit, 0)) {
                JOptionPane.showMessageDialog(panelPrincipal,
                        "El NIT ya está registrado",
                        "NIT Duplicado",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Cliente nuevoCliente = new Cliente(nombre, nit, telefono, correo, estado);
            int idGenerado = clienteDAO.insertar(nuevoCliente);

            if (idGenerado > 0) {
                limpiarFormulario();
                cargarTabla();
                JOptionPane.showMessageDialog(panelPrincipal,
                        "Cliente guardado exitosamente con ID: " + idGenerado,
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panelPrincipal,
                        "No se pudo guardar el cliente",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Error al guardar el cliente: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actualiza el cliente seleccionado
     */
    private void onActualizar() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Seleccione un cliente de la tabla para actualizar.",
                    "Error de selección",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nombre = txtNombre.getText().trim();
        String nit = txtNit.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        int estado = (cboEstado.getSelectedIndex() == 0) ? 1 : 0;

        if (nombre.isEmpty() || nit.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Todos los campos son obligatorios.",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Verificar NIT duplicado (excluyendo el actual)
            if (clienteDAO.existeNit(nit, selectedId)) {
                JOptionPane.showMessageDialog(panelPrincipal,
                        "El NIT ya está registrado para otro cliente",
                        "NIT Duplicado",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Cliente cliente = new Cliente(selectedId, nombre, nit, telefono, correo, estado);
            boolean ok = clienteDAO.actualizar(cliente);
            if (ok) {
                cargarTabla();
                seleccionarFilaPorId(selectedId);
                JOptionPane.showMessageDialog(panelPrincipal,
                        "Cliente actualizado exitosamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Error al actualizar el cliente: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina (lógicamente) el cliente seleccionado
     */
    private void onEliminar() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Seleccione un cliente para eliminar.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(panelPrincipal,
                "¿Está seguro que desea eliminar este cliente?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = clienteDAO.eliminar(selectedId);
                if (ok) {
                    cargarTabla();
                    limpiarFormulario();
                    JOptionPane.showMessageDialog(panelPrincipal,
                            "Cliente eliminado exitosamente.",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } /*catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panelPrincipal,
                    "Error al eliminar el cliente: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }*/

            catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panelPrincipal,
                        "Error de validación: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Busca clientes por nombre
     */
    /*
    private void onBuscar() {
        String termino = txtNombre.getText().trim();

        if (termino.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Ingrese un nombre de usuario para buscar.", "Campo Obligatorio", JOptionPane.WARNING_MESSAGE);
            //txtNombre.requestFocus();
            //cargarTabla();
            return;
        }

        try {
            List<Cliente> clientes = clienteDAO.buscarPorNombre(termino);
            model.setRowCount(0);

            if (clientes.isEmpty()) {
                JOptionPane.showMessageDialog(panelPrincipal,
                    "No se encontraron clientes con ese nombre.",
                    "Sin resultados",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Cliente c : clientes) {
                    model.addRow(new Object[]{
                        c.getId(),
                        c.getNombre(),
                        c.getNit(),
                        c.getTelefono(),
                        c.getCorreo(),
                        c.getEstado()
                    });
                }
            }
        } /*catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal,
                "Error al buscar clientes: " + ex.getMessage(),
                "Error de Búsqueda",
                JOptionPane.ERROR_MESSAGE);
        }*/
    /*

        catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Error al buscar usuario: " + ex.getMessage(),
                    "Error de busqueda",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    */

    /**
     * Carga todos los clientes en la tabla
     */
    private void cargarTabla() {
        try {
            List<Cliente> lista = clienteDAO.listar();
            model.setRowCount(0);
            for (Cliente c : lista) {
                model.addRow(new Object[]{
                        c.getId(),
                        c.getNombre(),
                        c.getNit(),
                        c.getTelefono(),
                        c.getCorreo(),
                        c.getEstado()
                });
            }
        } /*catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal,
                "Error al cargar la tabla de clientes.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }*/
        catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Error de validación: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpia el formulario completo
     */
    private void limpiarFormulario() {
        limpiarCampos();
        txtBuscar.setText("");
        tbClientes.clearSelection();
        selectedId = null;
        cargarTabla();
    }

    /**
     * Limpia solo los campos del formulario
     */
    private void limpiarCampos() {
        txtNombre.setText("");
        txtNit.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        cboEstado.setSelectedIndex(0);
    }

    /**
     * Selecciona una fila por ID
     */
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

    // ===== Métodos de validación =====

    private boolean validarNIT(String nit) {
        return nit.matches("^[0-9\\-]+$");
    }

    private boolean validarTelefono(String telefono) {
        String soloNumeros = telefono.replaceAll("[^0-9]", "");
        return soloNumeros.length() >= 8 && soloNumeros.length() <= 15;
    }

    private boolean validarCorreo(String correo) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return correo.matches(regex);
    }

    // Main para pruebas
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Gestión de Clientes");
            f.setContentPane(new ClienteForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}