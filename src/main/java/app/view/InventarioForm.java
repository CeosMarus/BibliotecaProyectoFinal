package app.view;

import app.dao.BitacoraInventarioDAO;
import app.dao.EjemplarDAO;
import app.dao.InventarioFisicoDAO;
import app.dao.UsuarioDAO;
import app.model.BitacoraInventario;
import app.model.Ejemplar;
import app.model.InventarioFisico;
import app.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class InventarioForm extends JFrame {

    private JPanel mainPanel;

    private JTextField txtFecha;
    private JComboBox<Usuario> cbResponsable;
    private JTextArea txtObservaciones;

    private JComboBox<String> comboEjemplar;
    private JTextArea txtDiferencia;
    private JTextArea txtAccion;
    private JButton btnRegistrarDiferencia;

    private JTable tablaInventarios;
    private JTable tablaBitacora;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnAnular;
    private JButton btnLimpiar;
    private JButton btnBuscar;
    private JButton btnVerTodos;
    private JButton btnSalir;

    private int idInventarioSeleccionado = -1;
    private InventarioFisicoDAO inventarioDAO;
    private BitacoraInventarioDAO bitacoraDAO;
    private EjemplarDAO ejemplarDAO;

    public InventarioForm() {
        setTitle("Gestión de Inventario Físico");
        setContentPane(mainPanel);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        inventarioDAO = new InventarioFisicoDAO();
        bitacoraDAO = new BitacoraInventarioDAO();
        ejemplarDAO = new EjemplarDAO();

        inicializarFormulario();

        btnGuardar.addActionListener(this::guardarInventario);
        btnActualizar.addActionListener(this::actualizarInventario);
        btnAnular.addActionListener(this::anularInventario);
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRegistrarDiferencia.addActionListener(this::registrarDiferencia);
        btnBuscar.addActionListener(this::buscarPorFecha);
        btnVerTodos.addActionListener(e -> cargarInventarios());
        btnSalir.addActionListener(e -> onSalir());

        tablaInventarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccionInventario();
        });
    }

    private void inicializarFormulario() {
        configurarTablas();
        cargarResponsables();  // ✅ cargar administradores y bibliotecarios
        cargarEjemplares();
        cargarInventarios();
        establecerFechaActual();
    }

    private void cargarResponsables() {
        try {
            UsuarioDAO udao = new UsuarioDAO();
            List<Usuario> lista = udao.listarEncargadosInventario();

            cbResponsable.removeAllItems();
            for (Usuario u : lista) cbResponsable.addItem(u);

            cbResponsable.setRenderer(new DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {

                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Usuario u) {
                        setText(u.getNombre() + " (" + u.getRol() + ")");
                    }
                    return this;
                }
            });

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando responsables: " + ex.getMessage());
        }
    }

    private void configurarTablas() {
        tablaInventarios.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Fecha", "Responsable", "Usuario", "Estado", "Observaciones"}
        ));

        tablaBitacora.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Ejemplar", "Libro", "Diferencia", "Acción", "Fecha"}
        ));
    }

    private void cargarEjemplares() {
        comboEjemplar.removeAllItems();

        try {
            List<Ejemplar> ejemplares = ejemplarDAO.listarConLibro();
            for (Ejemplar e : ejemplares) {
                comboEjemplar.addItem(e.getId() + " - " + e.getCodigoInventario() + " (" + e.getLibroNombre() + ")");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar ejemplares: " + ex.getMessage());
        }
    }

    private void cargarInventarios() {
        DefaultTableModel modelo = (DefaultTableModel) tablaInventarios.getModel();
        modelo.setRowCount(0);

        List<InventarioFisico> inventarios = inventarioDAO.listar();

        for (InventarioFisico inv : inventarios) {
            modelo.addRow(new Object[]{
                    inv.getId(),
                    inv.getFecha(),
                    inv.getResponsable(),
                    inv.getNombreUsuario(),
                    inv.getEstadoDescripcion(),
                    inv.getObservaciones()
            });
        }
    }

    private void establecerFechaActual() {
        txtFecha.setText(LocalDate.now().toString());
    }

    private void guardarInventario(ActionEvent evt) {
        try {
            if (cbResponsable.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un responsable");
                return;
            }

            Usuario responsableSel = (Usuario) cbResponsable.getSelectedItem();
            String responsable = responsableSel.getNombre();
            Integer idUsuario = responsableSel.getId();

            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            String observaciones = txtObservaciones.getText().trim();

            InventarioFisico inventario = new InventarioFisico(fecha, responsable, idUsuario, observaciones);
            int idGenerado = inventarioDAO.insertar(inventario);

            if (idGenerado > 0) {
                JOptionPane.showMessageDialog(this, "✅ Inventario guardado correctamente.\nID: " + idGenerado);
                limpiarCampos();
                cargarInventarios();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void actualizarInventario(ActionEvent evt) {
        if (idInventarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un inventario");
            return;
        }

        try {
            Usuario responsableSel = (Usuario) cbResponsable.getSelectedItem();
            String responsable = responsableSel.getNombre();
            Integer idUsuario = responsableSel.getId();

            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            String observaciones = txtObservaciones.getText().trim();

            InventarioFisico inventario = new InventarioFisico(
                    idInventarioSeleccionado, fecha, responsable, idUsuario, observaciones, 1
            );

            if (inventarioDAO.actualizar(inventario)) {
                JOptionPane.showMessageDialog(this, "✅ Inventario actualizado");
                limpiarCampos();
                cargarInventarios();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void anularInventario(ActionEvent evt) {
        if (idInventarioSeleccionado == -1) return;

        if (JOptionPane.showConfirmDialog(this, "¿Anular inventario?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (inventarioDAO.anular(idInventarioSeleccionado)) {
                JOptionPane.showMessageDialog(this, "✅ Anulado correctamente");
                limpiarCampos();
                cargarInventarios();
            }
        }
    }

    private void registrarDiferencia(ActionEvent evt) {
        if (idInventarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un inventario primero");
            return;
        }

        if (comboEjemplar.getSelectedItem() == null) return;

        int idEjemplar = Integer.parseInt(comboEjemplar.getSelectedItem().toString().split(" - ")[0]);

        BitacoraInventario bit = new BitacoraInventario(
                idInventarioSeleccionado,
                idEjemplar,
                txtDiferencia.getText().trim(),
                txtAccion.getText().trim()
        );

        bitacoraDAO.insertar(bit);
        cargarBitacora(idInventarioSeleccionado);
        limpiarCamposDiferencia();
    }

    private void cargarBitacora(int idInventario) {
        DefaultTableModel modelo = (DefaultTableModel) tablaBitacora.getModel();
        modelo.setRowCount(0);

        List<BitacoraInventario> bitacoras = bitacoraDAO.listarPorInventario(idInventario);
        for (BitacoraInventario b : bitacoras) {
            modelo.addRow(new Object[]{
                    b.getId(), b.getCodigoInventario(), b.getTituloLibro(),
                    b.getDiferencia(), b.getAccionCorrectiva(), b.getFechaRegistro()
            });
        }
    }

    private void limpiarCampos() {
        idInventarioSeleccionado = -1;
        establecerFechaActual();
        cbResponsable.setSelectedIndex(0);
        txtObservaciones.setText("");
        limpiarCamposDiferencia();
        tablaInventarios.clearSelection();
        ((DefaultTableModel) tablaBitacora.getModel()).setRowCount(0);
    }

    private void limpiarCamposDiferencia() {
        comboEjemplar.setSelectedIndex(0);
        txtDiferencia.setText("");
        txtAccion.setText("");
    }

    private void cargarSeleccionInventario() {
        int fila = tablaInventarios.getSelectedRow();
        if (fila == -1) return;

        idInventarioSeleccionado = (int) tablaInventarios.getValueAt(fila, 0);
        txtFecha.setText(tablaInventarios.getValueAt(fila, 1).toString());

        String responsable = tablaInventarios.getValueAt(fila, 2).toString();
        for (int i = 0; i < cbResponsable.getItemCount(); i++) {
            Usuario u = cbResponsable.getItemAt(i);
            if (u.getNombre().equals(responsable)) {
                cbResponsable.setSelectedIndex(i);
                break;
            }
        }

        txtObservaciones.setText(
                tablaInventarios.getValueAt(fila, 5) != null ?
                        tablaInventarios.getValueAt(fila, 5).toString() : ""
        );

        cargarBitacora(idInventarioSeleccionado);
    }

    private void buscarPorFecha(ActionEvent evt) {
        JTextField txtInicio = new JTextField(LocalDate.now().minusMonths(1).toString());
        JTextField txtFin = new JTextField(LocalDate.now().toString());

        JPanel p = new JPanel();
        p.add(new JLabel("Inicio:"));
        p.add(txtInicio);
        p.add(new JLabel("Fin:"));
        p.add(txtFin);

        if (JOptionPane.showConfirmDialog(this, p, "Buscar por fecha", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                LocalDate i = LocalDate.parse(txtInicio.getText());
                LocalDate f = LocalDate.parse(txtFin.getText());

                List<InventarioFisico> inventarios = inventarioDAO.buscarPorRangoFechas(i, f);
                DefaultTableModel modelo = (DefaultTableModel) tablaInventarios.getModel();
                modelo.setRowCount(0);

                for (InventarioFisico inv : inventarios) {
                    modelo.addRow(new Object[]{
                            inv.getId(), inv.getFecha(), inv.getResponsable(),
                            inv.getNombreUsuario(), inv.getEstadoDescripcion(), inv.getObservaciones()
                    });
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Fecha inválida");
            }
        }
    }

    private void onSalir() {
        if (JOptionPane.showConfirmDialog(this, "¿Deseas cerrar el formulario?",
                "Confirmar salida", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) {
                window.dispose(); // Cierra solo esta ventana
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new InventarioForm().setVisible(true);
        });
    }
}
