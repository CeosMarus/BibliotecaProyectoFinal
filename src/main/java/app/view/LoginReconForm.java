package app.view;

import app.core.Sesion;
import app.dao.RostroUsuarioDAO;
import app.dao.UsuarioDAO;
import app.model.Usuario;
import app.utility.PlantillaFacialUtil;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgcodecs;


import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginReconForm extends JFrame {
    public JPanel panelPrincipal;
    private JLabel lblCamara;
    private JLabel lblStatus;
    private JButton btnLoginFacial;
    private JButton btnCancelar;

    private OpenCVFrameGrabber grabber;
    private volatile boolean corriendo = true;

    public LoginReconForm() {
        setTitle("Login Facial");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panelPrincipal = new JPanel(new BorderLayout());
        lblCamara = new JLabel("", SwingConstants.CENTER);
        lblStatus = new JLabel("Iniciando...", SwingConstants.CENTER);
        btnLoginFacial = new JButton("Verificar Rostro");
        btnCancelar = new JButton("Volver");

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnLoginFacial);
        panelBotones.add(btnCancelar);

        panelPrincipal.add(lblCamara, BorderLayout.CENTER);
        panelPrincipal.add(lblStatus, BorderLayout.NORTH);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        add(panelPrincipal);

        btnLoginFacial.addActionListener(e -> verificarRostro());
        btnCancelar.addActionListener(e -> cerrar());

        iniciarCamara();
    }

    private void iniciarCamara() {
        new SwingWorker<Void, Frame>() {
            @Override
            protected Void doInBackground() throws Exception {
                grabber = new OpenCVFrameGrabber(0);
                grabber.start();
                OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                Java2DFrameConverter java2D = new Java2DFrameConverter();

                while (corriendo) {
                    Frame frame = grabber.grab();
                    if (frame != null) {
                        Image img = java2D.getBufferedImage(frame);
                        lblCamara.setIcon(new ImageIcon(img.getScaledInstance(lblCamara.getWidth(), lblCamara.getHeight(), Image.SCALE_SMOOTH)));
                    }
                }
                return null;
            }
        }.execute();
    }

    private void verificarRostro() {
        try {
            lblStatus.setText("Capturando rostro...");
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            Frame frame = grabber.grab();
            Mat mat = converter.convert(frame);
            String ruta = System.getProperty("java.io.tmpdir") + "/rostro_temp.jpg";
            opencv_imgcodecs.imwrite(ruta, mat);

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            RostroUsuarioDAO rostroDAO = new RostroUsuarioDAO();

            List<Usuario> usuarios = usuarioDAO.listar();
            for (Usuario u : usuarios) {
                byte[] plantillaBD = rostroDAO.obtenerPlantilla(u.getId());
                if (plantillaBD == null) continue;

                boolean coincide = PlantillaFacialUtil.compararPlantillas(plantillaBD, ruta);
                if (coincide) {
                    Sesion.login(u);
                    lblStatus.setText("Bienvenido, " + u.getNombre());
                    JOptionPane.showMessageDialog(this, "Autenticación facial exitosa.");
                    cerrar();
                    return;
                }
            }

            lblStatus.setText("Rostro no reconocido. Intente de nuevo.");
        } catch (Exception e) {
            lblStatus.setText("Error: " + e.getMessage());
        }
    }

    private void cerrar() {
        try {
            corriendo = false;
            if (grabber != null) grabber.stop();
        } catch (Exception ignored) {}
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginReconForm().setVisible(true));
    }
}
