package app.view;

import app.core.Sesion;
import app.dao.MultaDAO;
import app.model.MultaDetalle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class MisMultasForm extends JFrame {

    private JPanel panelPrincipal;
    private JTable tablaMultas;
    private JButton btnRegresar;
    private DefaultTableModel model;

    private final MultaDAO multaDAO = new MultaDAO();

    public MisMultasForm() {
        setTitle("Mis Multas");
        setContentPane(panelPrincipal);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(
                new Object[]{"ID", "Préstamo", "Monto", "Días Atraso", "Estado Pago", "Fecha Pago", "Observaciones"},
                0
        );
        tablaMultas.setModel(model);

        cargarMisMultas();

        btnRegresar.addActionListener(e -> {
            dispose();
            new MenuClienteForm().setVisible(true);
        });
    }

    private void cargarMisMultas() {
        try {
            if (!Sesion.isLogged()) {
                JOptionPane.showMessageDialog(this, "No hay cliente en sesión.");
                return;
            }

            int idCliente = Sesion.getUsuario().getId();
            List<MultaDetalle> lista = multaDAO.listarMultasPorCliente(idCliente);

            model.setRowCount(0);
            for (MultaDetalle m : lista) {
                model.addRow(new Object[]{
                        m.getId(),
                        m.getIdPrestamo(),
                        m.getMonto(),
                        m.getDiasAtraso(),
                        m.getEstadoPagoDescripcion(),
                        m.getFechaPago(),
                        m.getObservaciones()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar multas: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MisMultasForm().setVisible(true));
    }
}