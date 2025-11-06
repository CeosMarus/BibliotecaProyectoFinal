package app.view;

import app.core.Sesion;
import app.dao.UsuarioDAO;
import app.facerec.FaceAuth;
import app.model.Usuario;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2RGB;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

public class LoginReconForm extends JFrame {
    public JPanel panelPrincipal;
    private JLabel lblStatus;
    private JLabel lblPreview;
    private JButton btnLoginFacial, btnCancelar;

    private volatile boolean previewRunning = false;
    private Thread previewThread;

    public LoginReconForm() {
        setTitle("Login Facial");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        panelPrincipal = new JPanel(new BorderLayout());

        // Panel superior (estatus + preview)
        JPanel pnlTop = new JPanel(new BorderLayout());
        lblStatus = new JLabel("Listo para reconocimiento facial", SwingConstants.CENTER);

        lblPreview = new JLabel();
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblPreview.setPreferredSize(new Dimension(400, 280));
        pnlTop.add(lblStatus, BorderLayout.NORTH);
        pnlTop.add(lblPreview, BorderLayout.CENTER);

        // Botones
        btnLoginFacial = new JButton("Verificar rostro");
        btnCancelar = new JButton("Cancelar");
        JPanel pnlBtns = new JPanel();
        pnlBtns.add(btnLoginFacial);
        pnlBtns.add(btnCancelar);

        panelPrincipal.add(pnlTop, BorderLayout.CENTER);
        panelPrincipal.add(pnlBtns, BorderLayout.SOUTH);
        add(panelPrincipal);

        // Eventos
        btnLoginFacial.addActionListener(e -> onLoginFacial());
        btnCancelar.addActionListener(e -> {
            previewRunning = false;
            Window w = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (w != null) w.dispose();
        });

        startPreview();
    }

    private void startPreview() {
        previewRunning = true;
        previewThread = new Thread(() -> {
            VideoCapture cam = new VideoCapture(0);
            if (!cam.isOpened()) {
                setStatus("No se pudo abrir la cámara");
                return;
            }

            Mat frame = new Mat();
            while (previewRunning) {
                if (!cam.read(frame) || frame.empty()) continue;
                updatePreview(frame);
                try {
                    Thread.sleep(33);
                } catch (InterruptedException ignored) {
                }
            }

            cam.release();
        });
        previewThread.start();
    }

    private void updatePreview(Mat mat) {
        if (lblPreview == null) return;
        BufferedImage img = matToBufferedImage(mat);
        if (img == null) return;

        Image scaled = img.getScaledInstance(
                lblPreview.getWidth() > 0 ? lblPreview.getWidth() : 400,
                lblPreview.getHeight() > 0 ? lblPreview.getHeight() : 280,
                Image.SCALE_SMOOTH
        );

        SwingUtilities.invokeLater(() -> lblPreview.setIcon(new ImageIcon(scaled)));
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) return null;
        Mat rgb = new Mat();
        cvtColor(mat, rgb, COLOR_BGR2RGB);
        int w = rgb.cols(), h = rgb.rows(), ch = rgb.channels();
        byte[] data = new byte[w * h * ch];
        rgb.data().get(data);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        image.getRaster().setDataElements(0, 0, w, h, data);
        return image;
    }

    private void onLoginFacial() {
        try {
            setStatus("Iniciando reconocimiento...");
            previewRunning = false; // Detenemos preview

            FaceAuth auth = new FaceAuth(
                    "data/modelos/lbph_model.xml",
                    "src/main/resources/haarcascades/haarcascade_frontalface_default.xml",
                    65.0
            );

            Integer id = auth.predictUserIdFromWebcam(0, 30);
            if (id == null) {
                setStatus("No se reconoció el rostro (iluminación insuficiente o rostro inválido).");
                return;
            }

            Usuario u = new UsuarioDAO().buscarPorId(id);
            if (u == null) {
                setStatus("Usuario no encontrado en BD.");
                return;
            }

            Sesion.login(u);
            JOptionPane.showMessageDialog(this, "Bienvenido " + u.getNombre());

            cerrarVentanasLogin();
            abrirMenu();

        } catch (Exception ex) {
            ex.printStackTrace();
            setStatus("Error: " + ex.getMessage());
            startPreview(); // Reiniciar en caso de error
        }
    }

    private void cerrarVentanasLogin() {
        Window[] windows = Window.getWindows();
        for (Window w : windows) {
            if (w instanceof JFrame && w.isVisible() && !(w instanceof LoginReconForm)) {
                w.dispose();
            }
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        btnLoginFacial = new JButton();
        btnLoginFacial.setText("Login Facial");
        panelPrincipal.add(btnLoginFacial, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancelar = new JButton();
        btnCancelar.setText("Volver");
        panelPrincipal.add(btnCancelar, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblStatus = new JLabel();
        lblStatus.setText("Label");
        panelPrincipal.add(lblStatus, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelPrincipal;
    }
}
