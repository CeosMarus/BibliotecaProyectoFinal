package app.view;

import app.dao.BitacoraInventarioDAO;
import app.dao.EjemplarDAO;
import app.dao.InventarioFisicoDAO;
import app.model.BitacoraInventario;
import app.model.Ejemplar;
import app.model.InventarioFisico;
import app.core.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Formulario para Gestión de Inventario Físico
 *
 * FUNCIONALIDADES:
 * - Crear inventarios (auditorías/conteos)
 * - Registrar diferencias encontradas
 * - Ver bitácora de cambios
 * - Buscar por fecha
 */
public class InventarioForm extends JFrame {

    // ===== COMPONENTES UI =====
    private JPanel mainPanel;

    // Datos del inventario
    private JTextField txtFecha;
    private JTextField txtResponsable;
    private JTextArea txtObservaciones;

    // Registro de diferencias
    private JComboBox<String> comboEjemplar;
    private JTextArea txtDiferencia;
    private JTextArea txtAccion;
    private JButton btnRegistrarDiferencia;

    // Tablas
    private JTable tablaInventarios;
    private JTable tablaBitacora;

    // Botones CRUD
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnAnular;
    private JButton btnLimpiar;
    private JButton btnBuscar;
    private JButton btnVerTodos;
    private JButton btnSalir;

    // ===== VARIABLES DE CONTROL =====
    private int idInventarioSeleccionado = -1;
    private InventarioFisicoDAO inventarioDAO;
    private BitacoraInventarioDAO bitacoraDAO;
    private EjemplarDAO ejemplarDAO;

    // ===== CONSTRUCTOR =====
    public InventarioForm() {
        // Configuración básica de la ventana
        setTitle("Gestión de Inventario Físico");
        setContentPane(mainPanel);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Inicializar DAOs
        inventarioDAO = new InventarioFisicoDAO();
        bitacoraDAO = new BitacoraInventarioDAO();
        ejemplarDAO = new EjemplarDAO();

        // Inicializar formulario
        inicializarFormulario();

        // Conectar eventos
        btnGuardar.addActionListener(this::guardarInventario);
        btnActualizar.addActionListener(this::actualizarInventario);
        btnAnular.addActionListener(this::anularInventario);
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnRegistrarDiferencia.addActionListener(this::registrarDiferencia);
        btnBuscar.addActionListener(this::buscarPorFecha);
        btnVerTodos.addActionListener(e -> cargarInventarios());
        btnSalir.addActionListener(e -> salir());

        // Listener para tabla de inventarios
        tablaInventarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionInventario();
            }
        });
    }

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================

    private void inicializarFormulario() {
        configurarTablas();
        cargarEjemplares();
        cargarInventarios();
        establecerFechaActual();
    }

    /**
     * Configura las columnas de ambas tablas
     */
    private void configurarTablas() {
        // Tabla de inventarios
        tablaInventarios.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Fecha", "Responsable", "Usuario", "Estado", "Observaciones"}
        ));

        // Tabla de bitácora
        tablaBitacora.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Ejemplar", "Libro", "Diferencia", "Acción", "Fecha"}
        ));
    }

    /**
     * Carga todos los ejemplares activos en el combo
     */
    private void cargarEjemplares() {
        comboEjemplar.removeAllItems();

        try {
            List<Ejemplar> ejemplares = ejemplarDAO.listarConLibro();

            if (ejemplares.isEmpty()) {
                comboEjemplar.addItem("⚠️ No hay ejemplares activos");
                comboEjemplar.setEnabled(false);
            } else {
                comboEjemplar.setEnabled(true);
                for (Ejemplar e : ejemplares) {
                    String item = String.format("%d - %s (%s)",
                            e.getId(),
                            e.getCodigoInventario(),
                            e.getLibroNombre()
                    );
                    comboEjemplar.addItem(item);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar ejemplares: " + ex.getMessage());
        }
    }

    /**
     * Carga todos los inventarios en la tabla
     */
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

    /**
     * Establece la fecha actual en el campo de fecha
     */
    private void establecerFechaActual() {
        txtFecha.setText(LocalDate.now().toString());
    }

    // ============================================================
    // CRUD - INVENTARIOS
    // ============================================================

    /**
     * GUARDAR un nuevo inventario
     */
    private void guardarInventario(ActionEvent evt) {
        try {
            // Validar campos
            if (!validarCamposInventario()) return;

            // Validar que hay usuario en sesión
            if (!Sesion.isLogged()) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error: No hay usuario activo en la sesión");
                return;
            }

            // Crear objeto
            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            String responsable = txtResponsable.getText().trim();
            String observaciones = txtObservaciones.getText().trim();
            Integer idUsuario = Sesion.getUsuario().getId();

            InventarioFisico inventario = new InventarioFisico(
                    fecha, responsable, idUsuario, observaciones
            );

            // Insertar en BD
            int idGenerado = inventarioDAO.insertar(inventario);

            if (idGenerado > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Inventario guardado correctamente.\nID: " + idGenerado);
                limpiarCampos();
                cargarInventarios();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error al guardar el inventario");
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Formato de fecha incorrecto. Usa AAAA-MM-DD");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * ACTUALIZAR inventario existente
     */
    private void actualizarInventario(ActionEvent evt) {
        if (idInventarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Selecciona un inventario de la tabla primero");
            return;
        }

        try {
            if (!validarCamposInventario()) return;

            if (!Sesion.isLogged()) {
                JOptionPane.showMessageDialog(this,
                        "❌ Error: No hay usuario activo");
                return;
            }

            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            String responsable = txtResponsable.getText().trim();
            String observaciones = txtObservaciones.getText().trim();
            Integer idUsuario = Sesion.getUsuario().getId();

            InventarioFisico inventario = new InventarioFisico(
                    idInventarioSeleccionado, fecha, responsable,
                    idUsuario, observaciones, 1
            );

            boolean actualizado = inventarioDAO.actualizar(inventario);

            if (actualizado) {
                JOptionPane.showMessageDialog(this,
                        "✅ Inventario actualizado correctamente");
                limpiarCampos();
                cargarInventarios();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error al actualizar");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * ANULAR inventario (cambiar estado a 0)
     */
    private void anularInventario(ActionEvent evt) {
        if (idInventarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Selecciona un inventario para anular");
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Deseas ANULAR este inventario?\nEsta acción no se puede deshacer.",
                "Confirmar Anulación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmar == JOptionPane.YES_OPTION) {
            boolean anulado = inventarioDAO.anular(idInventarioSeleccionado);

            if (anulado) {
                JOptionPane.showMessageDialog(this,
                        "✅ Inventario anulado correctamente");
                limpiarCampos();
                cargarInventarios();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error al anular");
            }
        }
    }

    // ============================================================
    // REGISTRO DE DIFERENCIAS
    // ============================================================

    /**
     * REGISTRAR una diferencia encontrada durante el inventario
     */
    private void registrarDiferencia(ActionEvent evt) {
        // Validar que haya un inventario seleccionado
        if (idInventarioSeleccionado == -1) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Primero debes crear o seleccionar un inventario");
            return;
        }

        // Validar campos de diferencia
        if (!validarCamposDiferencia()) return;

        try {
            // Obtener ID del ejemplar seleccionado
            String item = (String) comboEjemplar.getSelectedItem();
            int idEjemplar = Integer.parseInt(item.split(" - ")[0]);

            // Crear registro de bitácora
            BitacoraInventario bitacora = new BitacoraInventario(
                    idInventarioSeleccionado,
                    idEjemplar,
                    txtDiferencia.getText().trim(),
                    txtAccion.getText().trim()
            );

            // Insertar en BD
            int idGenerado = bitacoraDAO.insertar(bitacora);

            if (idGenerado > 0) {
                JOptionPane.showMessageDialog(this,
                        "✅ Diferencia registrada correctamente");
                limpiarCamposDiferencia();
                cargarBitacora(idInventarioSeleccionado);
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Error al registrar diferencia");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * CARGAR bitácora de un inventario específico
     */
    private void cargarBitacora(int idInventario) {
        DefaultTableModel modelo = (DefaultTableModel) tablaBitacora.getModel();
        modelo.setRowCount(0);

        List<BitacoraInventario> bitacoras =
                bitacoraDAO.listarPorInventario(idInventario);

        for (BitacoraInventario bit : bitacoras) {
            modelo.addRow(new Object[]{
                    bit.getId(),
                    bit.getCodigoInventario(),
                    bit.getTituloLibro(),
                    bit.getDiferencia(),
                    bit.getAccionCorrectiva(),
                    bit.getFechaRegistro()
            });
        }
    }

    // ============================================================
    // BÚSQUEDA
    // ============================================================

    /**
     * BUSCAR inventarios por rango de fechas
     */
    private void buscarPorFecha(ActionEvent evt) {
        // Diálogo para ingresar fechas
        JTextField txtFechaInicio = new JTextField(10);
        JTextField txtFechaFin = new JTextField(10);

        txtFechaInicio.setText(LocalDate.now().minusMonths(1).toString());
        txtFechaFin.setText(LocalDate.now().toString());

        JPanel panel = new JPanel();
        panel.add(new JLabel("Fecha Inicio (AAAA-MM-DD):"));
        panel.add(txtFechaInicio);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Fecha Fin:"));
        panel.add(txtFechaFin);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Buscar por Rango de Fechas", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                LocalDate inicio = LocalDate.parse(txtFechaInicio.getText());
                LocalDate fin = LocalDate.parse(txtFechaFin.getText());

                List<InventarioFisico> inventarios =
                        inventarioDAO.buscarPorRangoFechas(inicio, fin);

                // Actualizar tabla
                DefaultTableModel modelo = (DefaultTableModel) tablaInventarios.getModel();
                modelo.setRowCount(0);

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

                JOptionPane.showMessageDialog(this,
                        "✅ Se encontraron " + inventarios.size() + " inventarios");

            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ Formato de fecha incorrecto");
            }
        }
    }

    // ============================================================
    // VALIDACIONES
    // ============================================================

    private boolean validarCamposInventario() {
        if (txtFecha.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ La fecha es obligatoria");
            txtFecha.requestFocus();
            return false;
        }

        if (txtResponsable.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ El responsable es obligatorio");
            txtResponsable.requestFocus();
            return false;
        }

        // Validar formato de fecha
        try {
            LocalDate.parse(txtFecha.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ Formato de fecha incorrecto. Usa AAAA-MM-DD");
            txtFecha.requestFocus();
            return false;
        }

        return true;
    }
//Mensajes de advertencia
    private boolean validarCamposDiferencia() {
        if (comboEjemplar.getSelectedItem() == null ||
                !comboEjemplar.isEnabled()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Selecciona un ejemplar");
            return false;
        }

        if (txtDiferencia.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Describe la diferencia encontrada");
            txtDiferencia.requestFocus();
            return false;
        }

        if (txtAccion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Describe la acción correctiva tomada");
            txtAccion.requestFocus();
            return false;
        }

        return true;
    }

    // UTILIDADES

    private void limpiarCampos() {
        idInventarioSeleccionado = -1;
        establecerFechaActual();
        txtResponsable.setText("");
        txtObservaciones.setText("");
        limpiarCamposDiferencia();
        tablaInventarios.clearSelection();

        // Limpiar tabla de bitácora
        DefaultTableModel modelo = (DefaultTableModel) tablaBitacora.getModel();
        modelo.setRowCount(0);
    }

    private void limpiarCamposDiferencia() {
        if (comboEjemplar.getItemCount() > 0) {
            comboEjemplar.setSelectedIndex(0);
        }
        txtDiferencia.setText("");
        txtAccion.setText("");
    }

    private void cargarSeleccionInventario() {
        int fila = tablaInventarios.getSelectedRow();

        if (fila != -1) {
            idInventarioSeleccionado = (int) tablaInventarios.getValueAt(fila, 0);

            // Cargar datos en campos
            txtFecha.setText(tablaInventarios.getValueAt(fila, 1).toString());
            txtResponsable.setText(tablaInventarios.getValueAt(fila, 2).toString());

            Object obs = tablaInventarios.getValueAt(fila, 5);
            txtObservaciones.setText(obs != null ? obs.toString() : "");

            // Cargar bitácora correspondiente
            cargarBitacora(idInventarioSeleccionado);
        }
    }

    private void salir() {
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Deseas cerrar el formulario?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmar == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    // MAIN (para pruebas independientes)

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            InventarioForm form = new InventarioForm();
            form.setVisible(true);
        });
    }
}