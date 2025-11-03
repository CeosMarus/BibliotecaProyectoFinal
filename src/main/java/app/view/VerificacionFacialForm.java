package app.view;

import app.dao.RostroUsuarioDAO;
import app.utility.WebcamHelper;
import app.utility.PlantillaFacialUtil;
import org.bytedeco.javacpp.Loader;

import javax.swing.*;
import java.awt.*;

public class VerificacionFacialForm extends JFrame {
    private JPanel panelPrincipal;
    private JButton btnCapturar;
    private JLabel lblEstado;
    private JButton btnCancelar;
    private WebcamHelper camara;
    private final int idUsuario;

    public VerificacionFacialForm(int idUsuario) {
        this.idUsuario = idUsuario;
        setTitle("Verificación Facial - 2FA");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        panelPrincipal = new JPanel(new BorderLayout());
        lblEstado = new JLabel("Iniciando cámara...", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnCapturar = new JButton("Capturar Rostro");
        btnCancelar = new JButton("Cancelar");

        JPanel botones = new JPanel();
        botones.add(btnCapturar);
        botones.add(btnCancelar);

        panelPrincipal.add(lblEstado, BorderLayout.CENTER);
        panelPrincipal.add(botones, BorderLayout.SOUTH);
        add(panelPrincipal);

        iniciarCamara();

        btnCapturar.addActionListener(e -> verificarRostro());
        btnCancelar.addActionListener(e -> cerrar());
    }

    private void iniciarCamara() {
        try {
            camara = new WebcamHelper();
            camara.iniciarCamara();
            lblEstado.setText("Cámara lista. Presione Capturar para continuar.");
        } catch (Exception e) {
            lblEstado.setText("Error iniciando cámara: " + e.getMessage());
        }
    }

    private void verificarRostro() {
        try {
            String ruta = camara.capturarFoto();
            if (ruta == null) {
                lblEstado.setText("No se pudo capturar el rostro.");
                return;
            }

            RostroUsuarioDAO dao = new RostroUsuarioDAO();
            byte[] plantillaBD = dao.obtenerPlantilla(idUsuario);

            if (plantillaBD == null) {
                lblEstado.setText("No hay plantilla registrada. Contacte al administrador.");
                return;
            }

            boolean coincide = PlantillaFacialUtil.compararPlantillas(plantillaBD, ruta);
            if (coincide) {
                lblEstado.setText("Rostro verificado. Acceso concedido.");
                JOptionPane.showMessageDialog(this, "Autenticación facial exitosa.");
                cerrar();
                // Aquí podrías abrir tu menú principal:
                // new MenuPrincipal().setVisible(true);
            } else {
                lblEstado.setText("Rostro no coincide. Intente de nuevo.");
            }

        } catch (Exception e) {
            lblEstado.setText("Error: " + e.getMessage());
        }
    }

    private void cerrar() {
        try {
            if (camara != null) camara.detenerCamara();
        } catch (Exception ignored) {}
        dispose();
    }

    public static void main(String[] args) {
        try {
            Loader.load(org.bytedeco.opencv.global.opencv_core.class);
        } catch (Throwable t) {
            System.err.println("Error cargando librerías nativas: " + t.getMessage());
        }
        SwingUtilities.invokeLater(() -> new VerificacionFacialForm(1).setVisible(true));
    }
}
