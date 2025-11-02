package app.view;

import app.core.Sesion;
import app.dao.ReservaDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;


public class MisReservas extends JFrame
{
    private JPanel panelPrincipal;
    private JButton btnVolver;
    private JTable tblReservas;

    private final ReservaDAO reservaDAO = new ReservaDAO();
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

    public MisReservas()
    {
        setTitle("Mis Reservas");
        setContentPane(panelPrincipal);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        tblReservas.setModel(model);
        cargarMisReservas();


        btnVolver.addActionListener(e -> {
            dispose();
            new MenuClienteForm().setVisible(true);
        });
    }
    private  void cargarMisReservas()
    {
        if (!Sesion.isLogged()) {
            JOptionPane.showMessageDialog(null, "No hay cliente en sesión.");
            return;
        }
        int idCliente = Sesion.getUsuario().getId();
        model.setRowCount(0);
        try {
            List<Object[]> reservas = reservaDAO.listarReservasPorCliente(idCliente);
            for (Object[] r : reservas) {
                model.addRow(r);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar reservas: " + e.getMessage());
        }
    }
    //Launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MisReservas().setVisible(true));
    }
}
