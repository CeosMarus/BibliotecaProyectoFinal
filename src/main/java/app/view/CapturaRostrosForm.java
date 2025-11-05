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
    String CASCADE_PATH = getClass().getResource("/haarcascades/haarcascade_frontalface_default.xml").getPath();

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
            JOptionPane.showMessageDialog(panelPrincipal, "Seleccione un usuario antes de capturar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idUsuario = Integer.parseInt(idText);
        running = true;
        setStatus("Iniciando cámara...");

        camThread = new Thread(() -> {
            VideoCapture cam = new VideoCapture(0);
            if (!cam.isOpened()) {
                setStatus("No se pudo abrir la cámara.");
                running = false;
                return;
            }

            CascadeClassifier detector = new CascadeClassifier(CASCADE_PATH);
            if (detector.empty()) {
                setStatus("No se encontró el clasificador HaarCascade.");
                cam.release();
                running = false;
                return;
            }

            Mat frame = new Mat();
            long lastUpdate = System.currentTimeMillis();
            int fpsDelay = 1000 / 15; // Limitar a ~15 FPS

            while (running) {
                if (!cam.read(frame) || frame.empty()) continue;

                // Convertir a gris
                Mat gray = new Mat();
                opencv_imgproc.cvtColor(frame, gray, COLOR_BGR2GRAY);

                // Detección de rostros
                RectVector faces = new RectVector();
                detector.detectMultiScale(gray, faces);

                // Dibujar rectángulos en preview
                for (int i = 0; i < faces.size(); i++) {
                    Rect face = faces.get(i);
                    rectangle(frame, face, new Scalar(0, 255, 0, 0), 2, 8, 0);
                }

                // Mostrar frame cada X ms
                long now = System.currentTimeMillis();
                if (now - lastUpdate > fpsDelay) {
                    updatePreview(frame);
                    lastUpdate = now;
                }

                // Si detecta un rostro, procesarlo en otro hilo
                if (faces.size() > 0) {
                    running = false;
                    Rect face = faces.get(0);
                    Mat rostro = new Mat(gray, face);
                    opencv_imgproc.resize(rostro, rostro, new Size(200, 200));

                    String ruta = System.getProperty("java.io.tmpdir") + "/face_" + idUsuario + ".jpg";
                    opencv_imgcodecs.imwrite(ruta, rostro);

                    setStatus("Rostro detectado. Procesando plantilla...");

                    // Guardar en segundo plano
                    new Thread(() -> {
                        try {
                            byte[] plantilla = PlantillaFacialUtil.generarPlantilla(ruta);
                            if (plantilla != null) {
                                new RostroUsuarioDAO().guardarPlantilla(idUsuario, plantilla);
                                setStatus("Plantilla facial registrada correctamente.");
                                JOptionPane.showMessageDialog(panelPrincipal,
                                        "Plantilla registrada correctamente para el usuario ID " + idUsuario);
                            } else {
                                setStatus("⚠️ No se pudo generar plantilla válida (rostro borroso o iluminación baja).");
                            }
                        } catch (Exception e) {
                            setStatus("Error guardando plantilla: " + e.getMessage());
                        }
                    }).start();
                    break;
                }

                // Pausa pequeña entre frames
                try { Thread.sleep(40); } catch (InterruptedException ignored) {}
            }

            cam.release();
            setStatus("Cámara detenida.");
        }, "CamThread");

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
