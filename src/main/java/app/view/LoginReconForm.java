package app.view;

import app.core.Sesion;
import app.dao.UsuarioDAO;
import app.facerec.FaceAuth;
import app.model.Usuario;

import javax.swing.*;
import java.awt.*;

public class LoginReconForm extends JFrame {
    public JPanel panelPrincipal;
    private JLabel lblStatus;
    private JButton btnLoginFacial, btnCancelar;

    public LoginReconForm() {
        setTitle("Login Facial");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panelPrincipal = new JPanel(new BorderLayout());
        lblStatus = new JLabel("Listo para reconocimiento facial", SwingConstants.CENTER);
        btnLoginFacial = new JButton("Verificar rostro");
        btnCancelar = new JButton("Cancelar");

        JPanel pnlBtns = new JPanel();
        pnlBtns.add(btnLoginFacial);
        pnlBtns.add(btnCancelar);

        panelPrincipal.add(lblStatus, BorderLayout.CENTER);
        panelPrincipal.add(pnlBtns, BorderLayout.SOUTH);
        add(panelPrincipal);

        btnLoginFacial.addActionListener(e -> onLoginFacial());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void onLoginFacial() {
        try {
            setStatus("Abriendo cámara...");

            FaceAuth auth = new FaceAuth(
                    "data/modelos/lbph_model.xml",
                    "src/main/resources/haarcascades/haarcascade_frontalface_default.xml",
                    65.0
            );

            // Inicia la cámara y predice ID del usuario
            Integer id = auth.predictUserIdFromWebcam(0, 30);
            if (id == null) {
                setStatus("No se reconoció el rostro.");
                return;
            }

            Usuario u = new UsuarioDAO().buscarPorId(id);
            if (u == null) {
                setStatus("Usuario no encontrado.");
                return;
            }

            // ✅ Guardamos la sesión antes de abrir el menú
            Sesion.login(u);
            JOptionPane.showMessageDialog(this, "Bienvenido " + u.getNombre());

            // ✅ Cerramos esta ventana y todas las relacionadas con el login
            Window[] windows = Window.getWindows();
            for (Window w : windows) {
                if (w instanceof JFrame && w.isVisible() && !(w instanceof LoginReconForm)) {
                    w.dispose();
                }
            }

            // ✅ Ahora abrimos el menú principal con la sesión activa
            abrirMenu();

        } catch (Exception ex) {
            ex.printStackTrace();
            setStatus("Error: " + ex.getMessage());
        }
    }

    private void abrirMenu() {
        JFrame f = new JFrame("Menú Principal – Biblioteca");
        f.setContentPane(new MainMenuForm().panelPrincipal);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void setStatus(String s) {
        SwingUtilities.invokeLater(() -> lblStatus.setText(s));
    }
}
