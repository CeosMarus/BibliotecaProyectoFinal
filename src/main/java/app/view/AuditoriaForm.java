package app.view;

import app.dao.AuditoriaDAO;
import app.dao.UsuarioDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;


public class AuditoriaForm extends JFrame
{
    private JComboBox<String> cboFiltro;
    private JLabel infoNombre;
    private JTextField txtNombre;
    private JTable tblAuditoria;
    private JButton btnCargar;
    public JPanel panelPrincipal;
    private JComboBox cboOpcion;
    private JButton btnLimpiar;
    private JButton btnSalir;

    //Definimos la estructura de nuestra tabla
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Fecha y Hora 📅 ", "Usuario 👤", "Rol", "Modulo", "Accion", "Detalle"}, 0
    )
    { //Evitamos que las celdas sean editables
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // ninguna celda editable
        }
    };

    public AuditoriaForm() {

        //Dimensiones de la ventana
        panelPrincipal.setPreferredSize(new Dimension(900, 600));

        //establecemos el modelo de la tabla
        tblAuditoria.setModel(model);
         //mostrar contenido en ventana emergente
        tblAuditoria.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblAuditoria.rowAtPoint(e.getPoint());
                    int col = tblAuditoria.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        Object value = tblAuditoria.getValueAt(row, col);
                        JOptionPane.showMessageDialog(null,
                                value == null ? "" : value.toString(),
                                "Contenido de celda",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        //Cargar opciones en el combo Filtro
        cboFiltro.addItem("Modulo");
        cboFiltro.addItem("Usuario");
        cboFiltro.addItem("Todos");
        cboOpcion.setEnabled(false);

        //Listener al escoger filtro
        cboFiltro.addActionListener(e -> {
            String filtro = (String)cboFiltro.getSelectedItem();
            if(filtro.equals("Modulo"))
            {
                cboOpcion.setEnabled(true);
                infoNombre.setVisible(true);
                cboOpcion.setVisible(true);
                cargarCBOModulo();
            }
            else if(filtro.equals("Usuario"))
            {
                cboOpcion.setEnabled(true);
                infoNombre.setVisible(true);
                cboOpcion.setVisible(true);
                cargarUsernamesEnCombo(cboOpcion);
            }
            else if(filtro.equals("Todos"))
            {
                infoNombre.setVisible(false);
                cboOpcion.setVisible(false);
            }
        });

        // Acción del botón Cargar
        btnCargar.addActionListener(e -> cargarAuditorias());

        //Accion del boton limpiar
        btnLimpiar.addActionListener(e -> limpiarTabla());

        //Acción para Salir
        btnSalir.addActionListener(e -> onSalir());
    }
    private void cargarAuditorias()
    {
        String filtro = (String) cboFiltro.getSelectedItem();
        String opcion = (String) cboOpcion.getSelectedItem();

        // Validaciones
        if (!"Todos".equalsIgnoreCase(filtro))
        {
            if("Seleccione un modulo...".equalsIgnoreCase(opcion))
            {
                JOptionPane.showMessageDialog(null,
                        "Debe seleccionar un modulo valido ");
                return;
            }
            else if("Seleccione un usuario...".equalsIgnoreCase(opcion))
            {
                JOptionPane.showMessageDialog(null,
                        "Debe seleccionar un usuario valido ");
                return;
            }
        }

       limpiarTabla();

        try {
            AuditoriaDAO dao = new AuditoriaDAO();
            List<Map<String, Object>> lista;

            switch (filtro) {
                case "Modulo" -> {
                    if (!dao.existeModulo(opcion)) {
                        JOptionPane.showMessageDialog(null,
                                "No exsiten registros para el modulo: " + opcion,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    lista = dao.listarPorModulo(opcion);
                }
                case "Usuario" -> {
                    Integer idUsuario = dao.obtenerIdUsuarioPorUsername(opcion);
                    if (idUsuario == null) {
                        JOptionPane.showMessageDialog(null,
                                "No se encontró ningún usuario:  '" + opcion + "'.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    lista = dao.listarPorUsuario(idUsuario);
                }
                default -> lista = dao.listarConUsuario();
            }

            // Cargar los resultados en la tabla
            for (Map<String, Object> fila : lista) {
                model.addRow(new Object[]{
                        fila.get("id"),
                        fila.get("fechaHora"),
                        fila.get("usuario"),
                        fila.get("rol"),
                        fila.get("modulo"),
                        fila.get("accion"),
                        fila.get("detalle")
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar auditorías: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public void limpiarTabla()
    {
        // Limpiar la tabla antes de cargar nuevos datos
        DefaultTableModel model = (DefaultTableModel) tblAuditoria.getModel();
        model.setRowCount(0);
    }

    public void cargarCBOModulo()
    {
        cboOpcion.removeAllItems();
        cboOpcion.addItem("Seleccione un modulo..."); // opción inicial
        //Cargar opciones en el combo Opcion
        cboOpcion.addItem("Auditoria");
        cboOpcion.addItem("Catalogo");
        cboOpcion.addItem("Cliente");
        cboOpcion.addItem("Compras");
        cboOpcion.addItem("Financiero");
        cboOpcion.addItem("Inventarios");
        cboOpcion.addItem("Prestamos");
        cboOpcion.addItem("Reservas");
        cboOpcion.addItem("Seguridad");
        cboOpcion.addItem("Reportes");
        cboOpcion.addItem("SolicitudesCompra");
        cboOpcion.addItem("Usuarios");

    }
    //Cargamos el cbOpcion
    private void cargarUsernamesEnCombo(JComboBox<String> cboOpcion) {
        try {
            UsuarioDAO dao = new UsuarioDAO();
            List<String> usernames = dao.listarUsernamesActivos();

            cboOpcion.removeAllItems();
            cboOpcion.addItem("Seleccione un usuario..."); // opción inicial

            for (String username : usernames) {
                cboOpcion.addItem(username);
            }

        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al cargar los usernames: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSalir() {
        if (JOptionPane.showConfirmDialog(panelPrincipal, "¿Deseas cerrar el formulario?",
                "Confirmar salida", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Window window = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (window != null) {
                window.dispose(); // Cierra solo esta ventana
            }
        }
    }

    //Launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Auditoria");
            f.setContentPane(new AuditoriaForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }



}
