package app.view;

import app.dao.RostroUsuarioDAO;
import app.utility.PlantillaFacialUtil;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

public class CapturaRostrosForm {
    public JPanel panelPrincipal;
    private JTextField txtIdUsuario;
    private JLabel lblPreview;
    private JButton btnCapturar;
    private JButton btnCerrar;
    private JLabel lblStatus;

    private volatile boolean running = false;
    private Thread camThread;
    private static final String CASCADE_PATH = "src/main/resources/haarcascades/haarcascade_frontalface_default.xml";
    //Constructores
    public CapturaRostrosForm()
    {
        initUI();
    }
    public CapturaRostrosForm(int userId)
    {
        initUI();
        txtIdUsuario.setText(String.valueOf(userId));
        txtIdUsuario.setEditable(false);
    }
    private void initUI()
    {
        panelPrincipal.setPreferredSize(new Dimension(800, 600));
        lblPreview.setPreferredSize(new Dimension(640, 480));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        btnCapturar.addActionListener(e -> onCapturar());
        btnCerrar.addActionListener(e -> cerrar());
        setStatus("Listo para capturar rostro.");
    }
    private void onCapturar() {
        if (running) {
            running = false;
            setStatus("Captura detenida.");
            return;
        }

        String idText = txtIdUsuario.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(panelPrincipal, "Ingrese o seleccione un usuario primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idUsuario = Integer.parseInt(idText);
        running = true;
        setStatus("Iniciando cámara...");

        camThread = new Thread(() -> {
            VideoCapture cam = new VideoCapture(0);
            if (!cam.isOpened()) {
                setStatus("No se pudo abrir la cámara.");
                return;
            }

            CascadeClassifier detector = new CascadeClassifier(CASCADE_PATH);
            Mat frame = new Mat();

            while (running) {
                if (!cam.read(frame) || frame.empty()) continue;

                Mat gray = new Mat();
                opencv_imgproc.cvtColor(frame, gray, COLOR_BGR2GRAY);

                RectVector faces = new RectVector();
                detector.detectMultiScale(gray, faces);

                for (int i = 0; i < faces.size(); i++) {
                    Rect face = faces.get(i);
                    rectangle(frame, face, new Scalar(0, 255, 0, 0), 2, 8, 0);
                }

                updatePreview(frame);

                if (faces.size() > 0) {
                    Rect face = faces.get(0);
                    Mat rostro = new Mat(gray, face);
                    opencv_imgproc.resize(rostro, rostro, new Size(200, 200));

                    String ruta = System.getProperty("java.io.tmpdir") + "/face_" + idUsuario + ".jpg";
                    opencv_imgcodecs.imwrite(ruta, rostro);

                    byte[] plantilla = PlantillaFacialUtil.generarPlantilla(ruta);
                    if (plantilla != null) {
                        try {
                            new RostroUsuarioDAO().guardarPlantilla(idUsuario, plantilla);
                            setStatus("Plantilla registrada correctamente.");
                        } catch (Exception e) {
                            setStatus("Error guardando plantilla: " + e.getMessage());
                        }
                    } else {
                        setStatus("No se pudo generar plantilla válida.");
                    }

                    running = false;
                    break;
                }

                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }

            cam.release();
        });

        camThread.start();
    }
    private void updatePreview(Mat frame) {
        BufferedImage img = matToBufferedImage(frame);
        if (img == null) return;
        SwingUtilities.invokeLater(() -> lblPreview.setIcon(new ImageIcon(img)));
    }
    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.data().get(b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), b);
        return image;
    }
    private void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> lblStatus.setText(msg));
    }
    private void cerrar() {
        running = false;
        if (camThread != null) camThread.interrupt();
        Window w = SwingUtilities.getWindowAncestor(panelPrincipal);
        if (w != null) w.dispose();
    }
    //Launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Captura de Rostros");
            f.setContentPane(new CapturaRostrosForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }




}
