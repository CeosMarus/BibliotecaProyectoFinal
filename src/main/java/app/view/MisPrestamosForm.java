package app.view;

import app.core.Sesion;
import app.dao.PrestamoDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class MisPrestamosForm extends JFrame {

    private JPanel panelPrincipal;
    private JTable tablaPrestamos;
    private JButton btnRegresar;
    private DefaultTableModel model;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    public MisPrestamosForm() {
        setTitle("Mis Préstamos");
        setContentPane(panelPrincipal);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(
                new Object[]{"ID", "Libro", "Autor", "Código Ejemplar", "Fecha Préstamo", "Fecha Vencimiento", "Fecha Devolución", "Estado"},
                0
        );
        tablaPrestamos.setModel(model);

        cargarMisPrestamos();

        btnRegresar.addActionListener(e -> {
            dispose();
            new MenuClienteForm().setVisible(true);
        });
    }

    private void cargarMisPrestamos() {
        try {
            if (!Sesion.isLogged()) {
                JOptionPane.showMessageDialog(this, "No hay cliente en sesión.");
                return;
            }

            int idCliente = Sesion.getUsuario().getId();
            List<Object[]> lista = prestamoDAO.listarPrestamosClienteDetalle(idCliente);

            model.setRowCount(0);
            for (Object[] fila : lista) {
                model.addRow(fila);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar préstamos: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MisPrestamosForm().setVisible(true));
    }
}