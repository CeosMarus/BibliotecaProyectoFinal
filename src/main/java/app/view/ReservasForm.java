package app.view;

import app.dao.ClienteDAO;
import app.dao.LibroDAO;
import app.dao.ReservaDAO;
import app.model.Cliente;
import app.model.Libro;
import app.model.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class ReservasForm
{
    private JButton btnConfirmar;
    private JButton btnActualizar;
    private JButton btnCargar;
    private JComboBox cboCliente;
    private JTextField txtNit;
    private JComboBox cboLibro;
    private JComboBox cboEstado;
    private JTable tblReservas;
    public JPanel panelPrincipal;
    private JLabel infoEstado;
    private JButton btnEliminar;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private List<Libro> librosActivos = new ArrayList<>();
    private List<Cliente> clientesActivos = new ArrayList<>();

    private Timer timerVencidas;


    //Definimos la estructura de nuestra tabla
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Cliente ", "Nit", "Libro" , "Fecha Reserva", "Estado", "Posicion"}, 0
    )
    { //Evitamos que las celdas sean editables
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // ninguna celda editable
        }
    };

    public  ReservasForm()
    {
        //Dimensiones de la ventana
        panelPrincipal.setPreferredSize(new Dimension(900, 600));
        //establecemos el modelo de la tabla
        tblReservas.setModel(model);

        //Llenar combos
        cargarClientesActivos();
        configurarAutoCompletarLibros();


        //Listeners
        btnCargar.addActionListener(e -> cargarReservas());
        cboCliente.addActionListener(e -> mostrarNitClienteSeleccionado());
        btnConfirmar.addActionListener(e -> confirmarReserva());
        tblReservas.getSelectionModel().addListSelectionListener(e -> seleccionarReserva());
        btnEliminar.addActionListener(e -> eliminarReserva());

        //Iniciar el Timer al construir el formulario
        iniciarTimerVencidas();
    }
    //CARGAR CLIENTES ACTIVOS
    private void cargarClientesActivos() {
        cboCliente.setRenderer(new DefaultListCellRenderer() {
            //Render personalizadao
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Cliente cliente) {
                    setText(cliente.getNombre()); // 🔹 Solo muestra el nombre
                }

                return this;
            }
        });

        try {
            cboCliente.removeAllItems();
            for (Cliente c : clienteDAO.listarActivos()) {
                cboCliente.addItem(c);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al listar clientes: " + e.getMessage());
        }
    }
    //Autocompletado de NIT
    private void mostrarNitClienteSeleccionado() {
        Cliente c = (Cliente) cboCliente.getSelectedItem();
        if (c != null) txtNit.setText(c.getNit());
    }

    //AUTOCOMPLETADO DE LIBROS ACTIVOS
    private void configurarAutoCompletarLibros() {
        List<Libro> libros = libroDAO.listar();
        DefaultComboBoxModel<String> modelLibros = new DefaultComboBoxModel<>();
        for (Libro l : libros) modelLibros.addElement(l.getTitulo());

        cboLibro.setModel(modelLibros);
        cboLibro.setEditable(true);

        JTextField editor = (JTextField) cboLibro.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String texto = editor.getText().toLowerCase();
                modelLibros.removeAllElements();
                for (Libro l : libros) {
                    if (l.getTitulo().toLowerCase().contains(texto)) {
                        modelLibros.addElement(l.getTitulo());
                    }
                }
                cboLibro.setPopupVisible(modelLibros.getSize() > 0);
            }
        });
    }

    //CARGAR RESERVAS
    private void cargarReservas() {
        model.setRowCount(0);
        try {
            List<Object[]> reservas = reservaDAO.listarReservasConDetalles();
            for (Object[] r : reservas) {
                model.addRow(r);
            }
            btnEliminar.setEnabled(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar reservas: " + e.getMessage());
        }
    }
    // ELIMINAR RESERVA SELECCIONADA
    private void eliminarReserva() {
        int fila = tblReservas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(null, "Seleccione una reserva para eliminar.");
            return;
        }

        int idReserva = (int) model.getValueAt(fila, 0);
        String tituloLibro = (String) model.getValueAt(fila, 3);
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "¿Seguro que desea eliminar esta reserva?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                reservaDAO.eliminar(idReserva);

                int idLibro = obtenerIdLibroPorTitulo(tituloLibro);
                reservaDAO.actualizarPosicionesCola(idLibro);

                JOptionPane.showMessageDialog(null, "Reserva eliminada correctamente.");
                cargarReservas();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al eliminar: " + e.getMessage());
            }
        }
    }

    //CONFIRMAR NUEVA RESERVA
    private void confirmarReserva() {
        try {
            Cliente cliente = (Cliente) cboCliente.getSelectedItem();
            String tituloLibro = (String) cboLibro.getSelectedItem();

            if (cliente == null || tituloLibro == null || tituloLibro.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Debe seleccionar cliente y libro.");
                return;
            }

            int idLibro = obtenerIdLibroPorTitulo(tituloLibro);
            int disponibles = reservaDAO.verificarEjemplaresDisponibles(tituloLibro);

            Reserva nueva = new Reserva();
            nueva.setIdCliente(cliente.getId());
            nueva.setIdLibro(idLibro);
            nueva.setFechaReserva(new java.util.Date());

            if (disponibles > 0) {
                //Hay ejemplares disponibles
                int opcion = JOptionPane.showConfirmDialog(
                        null,
                        "Hay ejemplares disponibles.\n¿Desea confirmar el préstamo directamente?",
                        "Ejemplar disponible",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (opcion == JOptionPane.YES_OPTION) {
                    // 👉 Abrir formulario de préstamos y no registrar reserva
                    abrirPrestamos();
                    return;
                } else {
                    // 👉 Registrar la reserva con estado = 2 (Ejemplar Disponible)
                    nueva.setEstadoReserva(2);
                    nueva.setPosicionCola(reservaDAO.calcularPosicionCola(nueva.getIdLibro()));
                    reservaDAO.insertar(nueva);
                    JOptionPane.showMessageDialog(null, "Reserva registrada como 'Ejemplar Disponible'.");
                    cargarReservas();
                    return;
                }
            }

            //Si NO hay ejemplares → agregar a cola
            nueva.setEstadoReserva(1);
            //Calcular posicion en cola
            int posicion = reservaDAO.calcularPosicionCola(idLibro);
            nueva.setPosicionCola(posicion);
            // Insertar la reserva con su posición correcta
            reservaDAO.insertar(nueva);
            reservaDAO.actualizarPosicionesCola(idLibro);

            JOptionPane.showMessageDialog(null, "Reserva agregada a la cola de espera.");
            cargarReservas();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al confirmar reserva: " + e.getMessage());
        }
    }

    //SELECCIONAR FILA
    private void seleccionarReserva() {
        int fila = tblReservas.getSelectedRow();
        if (fila >= 0) {
            cboCliente.setSelectedItem(model.getValueAt(fila, 1));
            txtNit.setText(model.getValueAt(fila, 2).toString());
            cboLibro.setSelectedItem(model.getValueAt(fila, 3));
        }
    }


    //UTILIDADES
    private int obtenerIdLibroPorTitulo(String titulo) throws SQLException {
        for (Libro l : libroDAO.listar()) {
            if (l.getTitulo().equalsIgnoreCase(titulo)) return l.getId();
        }
        throw new SQLException("No se encontró el libro con título: " + titulo);
    }

    //TIMER PARA MARCAR RESERVAS VENCIDAS
    private void iniciarTimerVencidas() {
        timerVencidas = new Timer(true); // Daemon
        timerVencidas.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<Reserva> reservas = reservaDAO.listar();
                    Date ahora = new Date();
                    for (Reserva r : reservas) {
                        // si está "Disponible" o "En Cola" y pasó más de 24h
                        if ((r.getEstadoReserva() == 2)
                                && horasTranscurridas(r.getFechaReserva(), ahora) >= 24) {
                            reservaDAO.actualizarEstadoReserva(r.getId(), 3); // 3 = Vencida
                            System.out.println("Reserva ID " + r.getId() + " marcada como VENCIDA.");
                        }
                    }
                    //SwingUtilities.invokeLater(() -> cargarReservas());
                } catch (Exception e) {
                    System.err.println("Error al actualizar vencidas: " + e.getMessage());
                }
            }
        }, 60 * 1000L, 60 * 1000L); // cada 60 segundos
    }

    private long horasTranscurridas(Date inicio, Date fin) {
        long diffMs = fin.getTime() - inicio.getTime();
        return diffMs / (1000 * 60 * 60);
    }
    private void abrirPrestamos() {
        JFrame f = new JFrame("Préstamos");
        f.setContentPane(new PrestamosForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
    //Launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Reservas");
            f.setContentPane(new ReservasForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}

