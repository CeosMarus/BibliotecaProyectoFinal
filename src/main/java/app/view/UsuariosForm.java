package app.view;

import app.dao.UsuarioDAO;
import app.model.Usuario;
import app.model.ComboItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UsuariosForm {
    //elementos utilizados
    public JPanel panelPrincipal;
    private JTextField txtUsername;
    private JTextField txtNombre;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;
    private JComboBox<ComboItem> cboRol;
    private JComboBox<String> cboEstado;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnCargar;
    private JButton btnLimpiar;
    private JButton btnBuscarUsername;
    private JButton btnSalir;
    private JTable tbUsuarios;
    private JCheckBox chkMostrar;
    private JButton btnCapturaRostro;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Username", "Nombre", "Password", "Rol", "Estado"}, 0
    );
    private Integer selectedId = null;
    private char echoPass, echoConf;

    public UsuariosForm() {
        panelPrincipal.setPreferredSize(new Dimension(900, 600));
        tbUsuarios.setModel(model);
        tbUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cboEstado.addItem("1 - Activo");
        cboEstado.addItem("0 - Inactivo");

        // Configuración de password visible/invisible
        echoPass = txtPassword.getEchoChar();
        echoConf = txtConfirm.getEchoChar();
        chkMostrar.addActionListener(e -> togglePasswordEcho());

        // Cargar roles
        cargarRolesEnCombo();

        // Eventos botones
        btnGuardar.addActionListener(e -> onGuardar());
        btnActualizar.addActionListener(e -> onActualizar());
        btnEliminar.addActionListener(e -> onEliminar());
        btnCargar.addActionListener(e -> cargarTabla());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnBuscarUsername.addActionListener(e -> onBuscarPorUsername());
        btnCapturaRostro.addActionListener(e -> onCapturarRostro());
        btnSalir.addActionListener(e -> onSalir()); //salir agregado

        tbUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tbUsuarios.getSelectedRow();
            if (row == -1) {
                selectedId = null;
                limpiarFormulario();
                return;
            }

            selectedId = Integer.parseInt(model.getValueAt(row, 0).toString());
            txtUsername.setText(model.getValueAt(row, 1).toString());
            txtNombre.setText(model.getValueAt(row, 2).toString());
            txtPassword.setText(model.getValueAt(row, 3).toString());

            String rolNombre = model.getValueAt(row, 4).toString();
            seleccionarRolPorNombre(rolNombre);

            String estadoTxt = model.getValueAt(row, 5).toString();
            cboEstado.setSelectedIndex("Activo".equalsIgnoreCase(estadoTxt) ? 0 : 1);
        });

        // Cargar tabla inicial
        cargarTabla();
    }

    /* Carga los roles al momento de la seleccion y creacion*/
    private void cargarRolesEnCombo() {
        try {
            cboRol.removeAllItems();
            cboRol.addItem(new ComboItem(1, "Admin"));
            cboRol.addItem(new ComboItem(2, "Bibliotecario"));
            cboRol.addItem(new ComboItem(3, "Financiero"));
            cboRol.addItem(new ComboItem(4, "Cliente"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void seleccionarRolPorNombre(String nombre) {
        for (int i = 0; i < cboRol.getItemCount(); i++) {
            ComboItem item = cboRol.getItemAt(i);
            if (item.getLabel().equalsIgnoreCase(nombre)) {
                cboRol.setSelectedIndex(i);
                return;
            }
        }
    }

    private void togglePasswordEcho() {
        boolean show = chkMostrar.isSelected();
        txtPassword.setEchoChar(show ? (char) 0 : echoPass);
        txtConfirm.setEchoChar(show ? (char) 0 : echoConf);
    }

    /* Crear nuevo usuario */
    private void onGuardar() {
        String username = txtUsername.getText().trim();
        String nombre = txtNombre.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());
        ComboItem rolItem = (ComboItem) cboRol.getSelectedItem();
        int estado = (cboEstado.getSelectedIndex() == 0) ? 1 : 0;

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(panelPrincipal, "El username debe tener al menos 3 caracteres.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pass.length() < 6) {
            JOptionPane.showMessageDialog(panelPrincipal, "La contraseña debe tener al menos 6 caracteres.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(panelPrincipal, "Las contraseñas no coinciden.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (username.isEmpty() || nombre.isEmpty() || rolItem == null) {
            JOptionPane.showMessageDialog(panelPrincipal, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            usuarioDAO.crearUsuario(username, pass, nombre, rolItem.getLabel(), estado);
            limpiarFormulario();
            cargarTabla();
            JOptionPane.showMessageDialog(panelPrincipal, "Usuario guardado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal, "Error al guardar usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Actualizar usuario */
    private void onActualizar() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(panelPrincipal, "Seleccione un usuario de la tabla para actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = txtUsername.getText().trim();
        String nombre = txtNombre.getText().trim();
        ComboItem rolItem = (ComboItem) cboRol.getSelectedItem();
        int estado = (cboEstado.getSelectedIndex() == 0) ? 1 : 0;

        if (username.isEmpty() || nombre.isEmpty() || rolItem == null) {
            JOptionPane.showMessageDialog(panelPrincipal, "Los campos username, nombre y rol son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Usuario u = new Usuario(selectedId, username, nombre, null, rolItem.getLabel(), estado);
            boolean ok = usuarioDAO.actualizar(u);
            if (ok) {
                cargarTabla();
                seleccionarFilaPorId(selectedId);
                JOptionPane.showMessageDialog(panelPrincipal, "Usuario actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal, "Error al actualizar usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Eliminación lógica */
    private void onEliminar() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(panelPrincipal, "Seleccione un usuario para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(panelPrincipal, "¿Desea desactivar este usuario?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = usuarioDAO.eliminar(selectedId);
                if (ok) {
                    cargarTabla();
                    limpiarFormulario();
                    JOptionPane.showMessageDialog(panelPrincipal, "Usuario desactivado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panelPrincipal, "Error al eliminar usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onBuscarPorUsername() {
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Ingrese un nombre de usuario para buscar.", "Campo Obligatorio", JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            return;
        }

        try {
            List<Usuario> usuarios = usuarioDAO.buscarPorUsername(username);
            model.setRowCount(0);
            if (usuarios.isEmpty()) {
                JOptionPane.showMessageDialog(panelPrincipal, "No se encontraron usuarios con ese nombre.", "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Usuario u : usuarios) {
                    model.addRow(new Object[]{
                            u.getId(),
                            u.getUsername(),
                            u.getNombre(),
                            "",
                            u.getRol(),
                            u.getEstado() == 1 ? "Activo" : "Inactivo"
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal, "Error al buscar usuarios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTabla() {
        try {
            List<Usuario> lista = usuarioDAO.listar();
            model.setRowCount(0);
            for (Usuario u : lista) {
                model.addRow(new Object[]{
                        u.getId(),
                        u.getUsername(),
                        u.getNombre(),
                        "",
                        u.getRol(),
                        u.getEstado() == 1 ? "Activo" : "Inactivo"
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panelPrincipal, "Error al cargar usuarios.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtUsername.setText("");
        txtNombre.setText("");
        txtPassword.setText("");
        txtConfirm.setText("");
        if (cboRol.getItemCount() > 0) cboRol.setSelectedIndex(0);
        cboEstado.setSelectedIndex(0);
        tbUsuarios.clearSelection();
        selectedId = null;
    }

    private void seleccionarFilaPorId(Integer id) {
        if (id == null) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 0);
            if (val != null && Integer.parseInt(val.toString()) == id) {
                tbUsuarios.setRowSelectionInterval(i, i);
                break;
            }
        }
    }
    //Captura facial para el usuario seleccionado
    private void onCapturarRostro() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(panelPrincipal,
                    "Seleccione un usuario antes de capturar el rostro.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        abrirCapturaRostro(selectedId);
    }

    //Abre el formulario de captura facial
    private void abrirCapturaRostro(int idUsuario) {
        CapturaRostrosForm form = new CapturaRostrosForm();
        form.setUsuarioId(idUsuario); // agrega este método al formulario
        JFrame f = new JFrame("Captura de Rostros");
        f.setContentPane(form.panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    /* Salir */
    private void onSalir() {
        Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (window != null) {
            window.dispose(); // Cierra solo esta ventana
        }
    }
//Main para pruebas independientes
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Gestión de Usuarios");
            f.setContentPane(new UsuariosForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}