package app.view;

import javax.swing.*;
import java.awt.*;
import app.core.Sesion;

public class MenuClienteForm extends JFrame {

    public JPanel panelPrincipal;
    private JButton btnMisPrestamos;
    private JButton btnMisMultas;
    private JButton btnSalir;

    public MenuClienteForm() {
        setTitle("Menú Cliente");
        setContentPane(panelPrincipal);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        btnMisPrestamos.addActionListener(e -> {
            JFrame f = new MisPrestamosForm();
            f.setVisible(true);
            dispose();
        });

        btnMisMultas.addActionListener(e -> {
            JFrame f = new MisMultasForm();
            f.setVisible(true);
            dispose();
        });

        btnSalir.addActionListener(e -> {
            // Regresar al menú principal
            dispose();
            // Aquí se llamaría al menú principal real del sistema:
            // new MainMenu().setVisible(true);
            JOptionPane.showMessageDialog(null, "Regresando al menú principal...");
        });
    }

    public static void main(String[] args) {
        // Simular cliente logueado para pruebas
        // Debes tener Sesion.setUsuario(cliente) antes en tu login real
        SwingUtilities.invokeLater(() -> new MenuClienteForm().setVisible(true));
    }
}