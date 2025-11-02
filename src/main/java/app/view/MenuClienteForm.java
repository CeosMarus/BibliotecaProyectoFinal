package app.view;

import javax.swing.*;

public class MenuClienteForm extends JFrame {

    public JPanel panelPrincipal;
    private JButton btnMisPrestamos;
    private JButton btnMisMultas;
    private JButton btnSalir;
    private JButton btnMisReservas;

    public MenuClienteForm() {
        setTitle("Menú Cliente");
        setContentPane(panelPrincipal);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        btnMisPrestamos.addActionListener(e -> {
            JFrame f = new MisPrestamosForm();
            f.setVisible(true);
            setVisible(false);
           // dispose();
        });

        btnMisMultas.addActionListener(e -> {
            JFrame f = new MisMultasForm();
            f.setVisible(true);
            setVisible(false);
            //dispose();
        });

        btnMisReservas.addActionListener(e -> {
            JFrame f = new MisReservas();
            f.setVisible(true);
            setVisible(false);
        });

        btnSalir.addActionListener(e -> CerrarVentana());
    }
    private void CerrarVentana(){
        int option = JOptionPane.showConfirmDialog(
                this, "¿Desea Salir del  menú del cliente?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (option == JOptionPane.YES_OPTION) {
            setVisible(false);
            this.dispose();
        }
    }

    public static void main(String[] args) {
        // Simular cliente logueado para pruebas
        // Debes tener Sesion.setUsuario(cliente)
        SwingUtilities.invokeLater(() -> new MenuClienteForm().setVisible(true));
    }
}