package app.view;

import app.dao.ClienteDAO;
import app.dao.PrestamoDAO;
import app.dao.MultaDAO;
import app.model.Cliente;
import app.model.Prestamo;
import app.model.Multa;
import app.model.MultaDetalle;
import app.model.ItemDesplegable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultaView extends JPanel {

    // ID del usuario logueado.
    private final int ID_USUARIO_ACTUAL = 1;

    // Componentes de la interfaz (Formulario)
    private JComboBox<ItemDesplegable> cmbClientes;
    private JComboBox<ItemDesplegable> cmbPrestamos;
    private JTextField txtIdMulta;
    private JTextField txtMonto;
    private JTextField txtDiasAtraso;
    private JComboBox<String> cmbEstadoPago;
    private JTextField txtFechaPago;
    private JTextArea txtObservaciones;

    // Componentes de Control
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnExoneraciones;

    // Componentes de la Tabla (Listado)
    private JTable tablaMultas;

    // Cabecera de la tabla con "Cliente"
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Cliente", "Préstamo ID", "Monto", "Atraso", "Estado Pago"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // Variables de estado
    private int idMultaSeleccionada = -1;

    // DAOs
    private final MultaDAO multaDAO = new MultaDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    // Utilidades
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MultaView() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(1100, 600));

        initComponents();
        cargarClientes();
        cargarTablaMultas();

        // Listeners
        btnGuardar.addActionListener(e -> guardarMulta());
        btnActualizar.addActionListener(e -> actualizarMulta());
        btnEliminar.addActionListener(e -> eliminarMulta());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnExoneraciones.addActionListener(e -> abrirVentanaExoneraciones());
        cmbClientes.addActionListener(e -> actualizarPrestamosPorCliente());
        cmbEstadoPago.addActionListener(e -> {
            if (cmbEstadoPago.getSelectedIndex() == 1) { // 1 - Pagado
                txtFechaPago.setText(dateFormat.format(new Date()));
            } else {
                txtFechaPago.setText("");
            }
        });

        // Listener para cargar datos del formulario al seleccionar una fila
        tablaMultas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaMultas.getSelectedRow() != -1) {
                cargarDatosFormularioDesdeTabla();
            }
        });

        // Inicializar botones de actualización/eliminación deshabilitados
        actualizarEstadoBotones(false);
    }

    private void initComponents() {
        // Título
        JLabel lblTitulo = new JLabel("Gestión de multas", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitulo, BorderLayout.NORTH);

        // Panel Central: Dividido en Formulario (Izquierda) y Tabla (Derecha)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35); // 35% para el formulario

        // A. Panel de Formulario (Izquierda)
        JPanel panelFormulario = createFormPanel();
        splitPane.setLeftComponent(panelFormulario);

        // B. Panel de Tabla y Controles (Derecha)
        JPanel panelTabla = createTablePanel();
        splitPane.setRightComponent(panelTabla);

        add(splitPane, BorderLayout.CENTER);

        // Panel de Botones (Inferior - Solo para botones especiales)
        JPanel panelBotonInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnExoneraciones = new JButton("Exoneraciones");

        panelBotonInferior.add(btnExoneraciones);
        add(panelBotonInferior, BorderLayout.SOUTH);
    }

    // --------------------------------------------------------------------------
    // CREACIÓN DE PANELES
    // --------------------------------------------------------------------------

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBorder(BorderFactory.createTitledBorder("Datos de la Multa"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicialización de componentes
        txtIdMulta = new JTextField(5);
        txtIdMulta.setEditable(false);
        txtMonto = new JTextField(15);
        txtDiasAtraso = new JTextField(15);
        cmbClientes = new JComboBox<>();
        cmbPrestamos = new JComboBox<>();
        cmbEstadoPago = new JComboBox<>(new String[]{"0 - Pendiente", "1 - Pagado"});
        txtFechaPago = new JTextField(15);
        txtObservaciones = new JTextArea(3, 15);
        JScrollPane scrollObservaciones = new JScrollPane(txtObservaciones);

        int row = 0;

        // Fila 0: ID Multa (Nuevo)
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; panelCampos.add(new JLabel("ID Multa:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; panelCampos.add(txtIdMulta, gbc);

        // Fila 1: Cliente
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; panelCampos.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; panelCampos.add(cmbClientes, gbc);

        // Fila 2: Préstamo
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; panelCampos.add(new JLabel("Préstamo Activo:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; panelCampos.add(cmbPrestamos, gbc);

        // Fila 3: Monto
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; panelCampos.add(new JLabel("Monto:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; panelCampos.add(txtMonto, gbc);

        // Fila 4: Días Atraso
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; panelCampos.add(new JLabel("Días Atraso:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; panelCampos.add(txtDiasAtraso, gbc);

        // Fila 5: Estado Pago
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; panelCampos.add(new JLabel("Estado de Pago:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; panelCampos.add(cmbEstadoPago, gbc);

        // Fila 6: Fecha Pago
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; panelCampos.add(new JLabel("Fecha Pago (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; panelCampos.add(txtFechaPago, gbc);

        // Fila 7: Observaciones
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTHWEST; panelCampos.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; gbc.weighty = 1.0; panelCampos.add(scrollObservaciones, gbc);

        panel.add(panelCampos, BorderLayout.CENTER);

        // Panel de Botones CRUD
        JPanel panelBotonesCRUD = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        btnGuardar = new JButton("➕ Nuevo Registro");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Mostrar Todo");

        panelBotonesCRUD.add(btnGuardar);
        panelBotonesCRUD.add(btnActualizar);
        panelBotonesCRUD.add(btnEliminar);
        panelBotonesCRUD.add(btnLimpiar);

        panel.add(panelBotonesCRUD, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Listado de Multas Activas"));

        tablaMultas = new JTable(modeloTabla);
        tablaMultas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaMultas.setAutoCreateRowSorter(true);

        JTextField txtBuscar = new JTextField(15);
        JButton btnBuscar = new JButton("Buscar"); // El texto se mantiene genérico

        JPanel pnlBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // 🆕 MODIFICACIÓN: Buscar por Nombre de Cliente
        pnlBusqueda.add(new JLabel("Buscar por Nombre Cliente:"));
        pnlBusqueda.add(txtBuscar);
        pnlBusqueda.add(btnBuscar);

        // 🆕 MODIFICACIÓN: Llamar al nuevo método de búsqueda por nombre
        btnBuscar.addActionListener(e -> buscarMultaPorNombreCliente(txtBuscar.getText().trim()));

        panel.add(pnlBusqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaMultas), BorderLayout.CENTER);

        return panel;
    }

    // --------------------------------------------------------------------------
    // LÓGICA DE TABLA Y CARGA DE DATOS
    // --------------------------------------------------------------------------

    /**
     * Carga la tabla usando MultaDetalle para mostrar el nombre.
     */
    private void cargarTablaMultas() {
        modeloTabla.setRowCount(0);
        try {
            // Llama al método del DAO que devuelve MultaDetalle
            List<MultaDetalle> listaMultas = multaDAO.listarConDetalles();

            for (MultaDetalle m : listaMultas) {
                // Usa el método getNombreCliente()
                String estadoPagoStr = (m.getEstadoPago() == 1) ? "Pagado" : "Pendiente";
                modeloTabla.addRow(new Object[]{
                        m.getId(),
                        m.getNombreCliente(),
                        m.getIdPrestamo(),
                        m.getMonto().toPlainString(),
                        m.getDiasAtraso(),
                        estadoPagoStr
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar multas: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MultaView.class.getName()).log(Level.SEVERE, "Error al cargar multas:", ex);
        }
    }

    private void cargarDatosFormularioDesdeTabla() {
        int fila = tablaMultas.getSelectedRow();
        if (fila == -1) return;

        // Se sigue usando la columna 0 (ID) para buscar la Multa completa en el DAO
        int id = (Integer) modeloTabla.getValueAt(fila, 0);

        try {
            // Se usa el DAO.buscarPorId() que devuelve el modelo base Multa
            Multa multa = multaDAO.buscarPorId(id);
            if (multa != null) {
                // 1. Cargar el ID
                idMultaSeleccionada = multa.getId();
                txtIdMulta.setText(String.valueOf(idMultaSeleccionada));

                // 2. Cargar Clientes y Préstamos
                seleccionarItemPorId(cmbClientes, multa.getIdCliente());
                // Forzar la carga de préstamos del cliente seleccionado
                actualizarPrestamosPorCliente();
                seleccionarItemPorId(cmbPrestamos, multa.getIdPrestamo());

                // 3. Cargar el resto de campos
                txtMonto.setText(multa.getMonto().toPlainString());
                txtDiasAtraso.setText(String.valueOf(multa.getDiasAtraso()));
                cmbEstadoPago.setSelectedIndex(multa.getEstadoPago());

                if (multa.getFechaPago() != null) {
                    txtFechaPago.setText(dateFormat.format(multa.getFechaPago()));
                } else {
                    txtFechaPago.setText("");
                }
                txtObservaciones.setText(multa.getObservaciones());

                // Habilitar botones de edición
                actualizarEstadoBotones(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos de la multa: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MultaView.class.getName()).log(Level.SEVERE, "Error al buscar multa por ID:", ex);
        }
    }

    /** Intenta seleccionar un ItemDesplegable por su ID. **/
    private void seleccionarItemPorId(JComboBox<ItemDesplegable> cmb, int id) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            ItemDesplegable item = cmb.getItemAt(i);
            if (item != null && item.getId() == id) {
                cmb.setSelectedIndex(i);
                return;
            }
        }
        if (cmb.getItemCount() > 0) {
            cmb.setSelectedIndex(0);
        }
    }

    /** Controla el estado de los botones Actualizar/Eliminar. **/
    private void actualizarEstadoBotones(boolean habilitarEdicion) {
        btnActualizar.setEnabled(habilitarEdicion);
        btnEliminar.setEnabled(habilitarEdicion);
        btnGuardar.setEnabled(!habilitarEdicion);
    }

    // --------------------------------------------------------------------------
    // LÓGICA CRUD: CREAR, ACTUALIZAR, ELIMINAR, BUSCAR
    // --------------------------------------------------------------------------

    private void guardarMulta() {
        try {
            // 1. Obtener IDs de los Comboboxes
            ItemDesplegable clienteItem = (ItemDesplegable) cmbClientes.getSelectedItem();
            ItemDesplegable prestamoItem = (ItemDesplegable) cmbPrestamos.getSelectedItem();

            if (clienteItem == null || prestamoItem == null || clienteItem.getId() <= 0 || prestamoItem.getId() <= 0) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Cliente y un Préstamo válidos.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idPrestamo = prestamoItem.getId();
            int idCliente = clienteItem.getId();

            // 2. Obtener y validar Monto
            String montoText = txtMonto.getText().trim().replace(',', '.');
            if (montoText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Monto es obligatorio.", "Campos Obligatorios", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BigDecimal monto = new BigDecimal(montoText);

            // 3. Obtener y validar Días Atraso
            String diasAtrasoText = txtDiasAtraso.getText().trim();
            if (diasAtrasoText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Los Días de Atraso son obligatorios.", "Campos Obligatorios", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int diasAtraso = Integer.parseInt(diasAtrasoText);

            // 4. Obtener Estado Pago y Fecha de Pago
            int estadoPago = Integer.parseInt(((String) cmbEstadoPago.getSelectedItem()).substring(0, 1));
            Date fechaPago = null;
            String fechaPagoText = txtFechaPago.getText().trim();

            if (estadoPago == 1 && fechaPagoText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Si el estado es 'Pagado', debe ingresar una Fecha de Pago.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!fechaPagoText.isEmpty()) {
                fechaPago = dateFormat.parse(fechaPagoText);
            }
            String observaciones = txtObservaciones.getText().trim();

            // 5. Crear Multa
            Multa nuevaMulta = new Multa(
                    idPrestamo, idCliente, monto, diasAtraso, estadoPago, fechaPago, observaciones, 1
            );

            // 6. Insertar en BD
            int idGenerado = multaDAO.insertar(nuevaMulta, ID_USUARIO_ACTUAL);

            if (idGenerado > 0) {
                JOptionPane.showMessageDialog(this, "Multa registrada con éxito. ID: " + idGenerado, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarTablaMultas();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar la multa (ID no generado).", "Error de Inserción", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MultaView.class.getName()).log(Level.SEVERE, "Error al guardar multa:", ex);
        }
    }

    private void actualizarMulta() {
        if (idMultaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "No hay multa seleccionada para actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 1. Obtener IDs de los Comboboxes
            ItemDesplegable clienteItem = (ItemDesplegable) cmbClientes.getSelectedItem();
            ItemDesplegable prestamoItem = (ItemDesplegable) cmbPrestamos.getSelectedItem();

            if (clienteItem == null || prestamoItem == null || clienteItem.getId() <= 0 || prestamoItem.getId() <= 0) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Cliente y un Préstamo válidos.", "Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int idPrestamo = prestamoItem.getId();
            int idCliente = clienteItem.getId();

            // 2. Obtener y validar datos
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim().replace(',', '.'));
            int diasAtraso = Integer.parseInt(txtDiasAtraso.getText().trim());
            int estadoPago = Integer.parseInt(((String) cmbEstadoPago.getSelectedItem()).substring(0, 1));
            Date fechaPago = null;
            String fechaPagoText = txtFechaPago.getText().trim();

            if (!fechaPagoText.isEmpty()) {
                fechaPago = dateFormat.parse(fechaPagoText);
            }
            String observaciones = txtObservaciones.getText().trim();

            // 3. Crear el objeto Multa con el ID seleccionado
            Multa multaActualizada = new Multa(
                    idMultaSeleccionada, idPrestamo, idCliente, monto, diasAtraso,
                    estadoPago, fechaPago, observaciones, 1
            );

            // 4. Persistir la actualización
            boolean ok = multaDAO.actualizar(multaActualizada);

            if (ok) {
                JOptionPane.showMessageDialog(this, "Multa ID " + idMultaSeleccionada + " actualizada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarTablaMultas();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar la multa.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MultaView.class.getName()).log(Level.SEVERE, "Error al actualizar multa:", ex);
        }
    }

    private void eliminarMulta() {
        if (idMultaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "No hay multa seleccionada para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean ok = multaDAO.eliminar(idMultaSeleccionada);

            if (ok) {
                JOptionPane.showMessageDialog(this, "Multa ID " + idMultaSeleccionada + " desactivada (eliminación lógica) con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarTablaMultas();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de BD al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MultaView.class.getName()).log(Level.SEVERE, "Error al eliminar multa:", ex);
        }
    }

    /**
     * 🆕 MODIFICACIÓN: Nuevo método de búsqueda por nombre de cliente.
     */
    private void buscarMultaPorNombreCliente(String nombre) {
        if (nombre.isEmpty()) {
            cargarTablaMultas(); // Si el campo está vacío, recarga la lista completa
            return;
        }

        modeloTabla.setRowCount(0); // Limpia la tabla para el nuevo resultado
        try {
            // Llama al nuevo método del DAO que devuelve MultaDetalle
            List<MultaDetalle> listaMultas = multaDAO.buscarPorNombreCliente(nombre);

            if (listaMultas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron multas activas para el cliente que contenga: " + nombre, "Búsqueda", JOptionPane.INFORMATION_MESSAGE);
                cargarTablaMultas();
                return;
            }

            for (MultaDetalle m : listaMultas) {
                String estadoPagoStr = (m.getEstadoPago() == 1) ? "Pagado" : "Pendiente";
                modeloTabla.addRow(new Object[]{
                        m.getId(),
                        m.getNombreCliente(), // Muestra el nombre
                        m.getIdPrestamo(),
                        m.getMonto().toPlainString(),
                        m.getDiasAtraso(),
                        estadoPagoStr
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de BD al buscar por nombre: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MultaView.class.getName()).log(Level.SEVERE, "Error al buscar multa por nombre:", ex);
        }
    }

    private void limpiarCampos() {
        txtIdMulta.setText("");
        idMultaSeleccionada = -1;
        cmbClientes.setSelectedIndex(0);
        txtMonto.setText("");
        txtDiasAtraso.setText("");
        cmbEstadoPago.setSelectedIndex(0);
        txtFechaPago.setText("");
        txtObservaciones.setText("");
        tablaMultas.clearSelection();
        actualizarEstadoBotones(false);
        actualizarPrestamosPorCliente();
        cargarTablaMultas();
    }

    // --------------------------------------------------------------------------
    // LÓGICA DE CARGA Y CASCADA DE COMBOBOXES
    // --------------------------------------------------------------------------

    /** Carga todos los clientes activos. **/
    private void cargarClientes() {
        cmbClientes.removeAllItems();
        try {
            List<Cliente> listaClientes = clienteDAO.listar();

            cmbClientes.addItem(new ItemDesplegable(0, "--- Seleccione un Cliente ---"));

            for(Cliente c : listaClientes) {
                if(c.getEstado() == 1) {
                    String descripcion = c.getNombre() + " (NIT: " + c.getNit() + ")";
                    cmbClientes.addItem(new ItemDesplegable(c.getId(), descripcion));
                }
            }
            actualizarPrestamosPorCliente();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes.", "Error BD", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MultaView.class.getName()).log(Level.SEVERE, "Error al cargar clientes:", ex);
        }
    }

    /** Actualiza el ComboBox de préstamos activos al cambiar el cliente. **/
    private void actualizarPrestamosPorCliente() {
        cmbPrestamos.removeAllItems();
        ItemDesplegable clienteSeleccionado = (ItemDesplegable) cmbClientes.getSelectedItem();

        if (clienteSeleccionado == null || clienteSeleccionado.getId() <= 0) {
            cmbPrestamos.addItem(new ItemDesplegable(0, "Seleccione primero un cliente"));
            return;
        }

        try {
            int idCliente = clienteSeleccionado.getId();
            List<Prestamo> prestamosActivos = prestamoDAO.listarPorCliente(idCliente);

            if (prestamosActivos.isEmpty()) {
                cmbPrestamos.addItem(new ItemDesplegable(0, "No hay préstamos activos para este cliente"));
                return;
            }

            for (Prestamo p : prestamosActivos) {
                String descripcion = "Préstamo ID [" + p.getId() + "] - Vence: " + dateFormat.format(p.getFechaVencimiento());
                cmbPrestamos.addItem(new ItemDesplegable(p.getId(), descripcion));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar préstamos. Verifique la conexión a BD.", "Error BD", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(MultaView.class.getName()).log(Level.SEVERE, "Error al cargar préstamos:", ex);
        }
    }


    // --------------------------------------------------------------------------
    // FUNCIÓN PARA ABRIR LA VISTA DE EXONERACIONES
    // --------------------------------------------------------------------------
    private void abrirVentanaExoneraciones() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gestión de Exoneraciones de Multas");
            frame.setContentPane(new ExoneracionView());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);
        });
    }

    // --------------------------------------------------------------------------
    // MÉTODO MAIN PARA PRUEBA INDIVIDUAL
    // --------------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(" Gestión de Multas (CRUD)");
            frame.setContentPane(new MultaView());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}