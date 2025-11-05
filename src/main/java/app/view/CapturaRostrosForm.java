package app.view;

import app.dao.RostroUsuarioDAO;
import app.facerec.FaceService;
import app.facerec.FaceTrainer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_ANY;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_AVFOUNDATION;

public class CapturaRostrosForm {
    public JPanel panelPrincipal;
    private JTextField txtIdUsuario;
    private JSpinner spnMuestras;
    private JButton btnCapturar;
    private JButton btnCerrar;
    private JLabel lblStatus;
    private JLabel lblPreview;

    private static final int BACKEND = CAP_AVFOUNDATION;
    private static final int CAMERA_INDEX = 0;
    private volatile boolean running = false;
    private Integer forcedUserId = null;
    private Thread previewThread;
    private volatile boolean previewRunning = false;

    // Número fijo de capturas (enrolamiento rápido)
    private static final int NUM_CAPTURAS = 5;

    public CapturaRostrosForm(int userId)
    {
        this();
        this.forcedUserId = userId;
        startPreview();
        txtIdUsuario.setText(String.valueOf(userId));
        txtIdUsuario.setEditable(false); }

    public CapturaRostrosForm() {
        panelPrincipal.setPreferredSize(new Dimension(900, 680));
        lblPreview.setPreferredSize(new Dimension(640, 480));
        lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
        setStatus("Listo para capturar rostros");

        btnCapturar.addActionListener(e -> {
            if (running) {
                running = false;
                btnCapturar.setText("Capturar");
                setStatus("Captura detenida");
            } else {
                startCapture();
            }
        });

        btnCerrar.addActionListener(e -> {
            running = false;
            previewRunning = false;
            Window w = SwingUtilities.getWindowAncestor(panelPrincipal);
            if (w != null) w.dispose();
        });
    }
    private void startPreview()
    {
        if (previewRunning) return;
        previewRunning = true;
        previewThread = new Thread(() -> {
            VideoCapture cam = new VideoCapture(CAMERA_INDEX, BACKEND);
            if (!cam.isOpened())
            {
                setStatus("No se pudo abrir la webcam");
                previewRunning = false;
                return;
            }
            Mat frame = new Mat();
            while (previewRunning)
            {
                if (!cam.read(frame) || frame.empty()) continue;
                updatePreview(frame);
                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }
            cam.release();
        });
        previewThread.start();
    }

    private void startCapture() {
        int idUsuario;
        if (forcedUserId != null)
        {
            idUsuario = forcedUserId;
        } else
        {
            String idTxt = txtIdUsuario.getText().trim();
            if (idTxt.isEmpty())
            {
                setStatus("Ingrese idUsuario");
                return;
            }
            try
            {
                idUsuario = Integer.parseInt(idTxt);
            } catch (NumberFormatException ex)
            {
                setStatus("idUsuario inválido");
                return;
            }
        }

        running = true;
        btnCapturar.setText("Detener");
        setStatus("Iniciando cámara...");

        new Thread(() -> {
            try {
                FaceService faceService = new FaceService(getCascadePath());
                VideoCapture cam = new VideoCapture(CAMERA_INDEX, BACKEND);
                if (!cam.isOpened()) cam = new VideoCapture(CAMERA_INDEX, CAP_ANY);
                if (!cam.isOpened()) {
                    setStatus("No se pudo abrir la cámara");
                    running = false;
                    return;
                }

                Mat frame = new Mat();
                File outDir = Paths.get("data", "faces", String.valueOf(idUsuario)).toFile();
                if (!outDir.exists()) outDir.mkdirs();
                int count = 0;
                while (running && count < NUM_CAPTURAS) {
                    if (!cam.read(frame) || frame.empty()) continue;

                    RectVector faces = faceService.detectFaces(frame);
                    Mat preview = frame.clone();
                    for (int i = 0; i < faces.size(); i++) {
                        Rect r = faces.get(i);
                        rectangle(preview, r, new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
                    }
                    updatePreview(preview);
                    if (faces.size() > 0)
                    {
                        Rect r = faces.get(0);
                        Mat face = new Mat(frame, r);
                        Mat gray = new Mat();
                        cvtColor(face, gray, COLOR_BGR2GRAY);
                        resize(gray, gray, new Size(200, 200));

                        String filename = new File(outDir, "u" + idUsuario + "_" + System.currentTimeMillis() + ".png").getAbsolutePath();
                        imwrite(filename, gray);
                        count++;
                        setStatus("Captura " + count + " / " + NUM_CAPTURAS);
                        Thread.sleep(500);
                    }
                }
                cam.release();
                setStatus("Captura completa. Entrenando modelo...");

                new Thread(() ->
                {
                    try
                    {
                        String facesRoot = "data/faces";
                        String modelPath = "data/modelos/lbph_model.xml";

                        FaceTrainer.train(facesRoot, modelPath);

                        // Convertir modelo a bytes
                        File modelFile = new File(modelPath);
                        byte[] modeloBytes = readFileToBytes(modelFile);

                        // Guardar plantilla en BD
                        RostroUsuarioDAO dao = new RostroUsuarioDAO();
                        dao.insertarOActualizar(idUsuario, modeloBytes);

                        SwingUtilities.invokeLater(() ->
                        {
                            setStatus("Enrolamiento completado y guardado en BD");
                            JOptionPane.showMessageDialog(panelPrincipal,
                                    "Rostro registrado exitosamente.\nPlantilla almacenada en BD.",
                                    "Captura de Rostros", JOptionPane.INFORMATION_MESSAGE);
                            });
                    }catch (Exception ex)
                    {
                        ex.printStackTrace();
                        setStatus("Error entrenando: " + ex.getMessage());
                    }
                }).start();
                running = false;
                SwingUtilities.invokeLater(() -> btnCapturar.setText("Capturar"));
            }catch (Exception ex)
            {
                ex.printStackTrace();
                setStatus("Error: " + ex.getMessage());
                running = false;
                SwingUtilities.invokeLater(() -> btnCapturar.setText("Capturar"));
            }
        }).start();
    }
    private void updatePreview(Mat bgr) {
        BufferedImage src = matToBufferedImage(bgr);
        if (src == null) return;

        int w = lblPreview.getWidth() > 0 ? lblPreview.getWidth() : lblPreview.getPreferredSize().width;
        int h = lblPreview.getHeight() > 0 ? lblPreview.getHeight() : lblPreview.getPreferredSize().height;
        BufferedImage scaled = scaleToFit(src, w, h);
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
    private BufferedImage scaleToFit(BufferedImage src, int targetW, int targetH) {
        double arSrc = (double) src.getWidth() / src.getHeight();
        double arDst = (double) targetW / targetH;
        int w, h;
        if (arSrc > arDst) { w = targetW; h = (int) Math.round(targetW / arSrc); }
        else { h = targetH; w = (int) Math.round(targetH * arSrc); }
        BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = dst.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, targetW, targetH);
        int x = (targetW - w) / 2;
        int y = (targetH - h) / 2;
        g.drawImage(src, x, y, w, h, null);
        g.dispose();
        return dst;
    }

    private void setStatus(String s) {
        SwingUtilities.invokeLater(() -> lblStatus.setText(s));
    }
    private String getCascadePath() {
        String resourceName = "haarcascades/haarcascade_frontalface_default.xml";
        try (var in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) throw new IllegalStateException("No se encontró " + resourceName);
            java.nio.file.Path temp = java.nio.file.Files.createTempFile("cascade_", ".xml");
            java.nio.file.Files.copy(in, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            temp.toFile().deleteOnExit();
            return temp.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error preparando cascade: " + e.getMessage(), e);
        }
    }

    private byte[] readFileToBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }
    private int usuarioId;
    public void setUsuarioId(int id) {
        this.usuarioId = id;
        if (txtIdUsuario != null) txtIdUsuario.setText(String.valueOf(id));
    }
        //Laucher
        public static void main(String[] args)
        {
            SwingUtilities.invokeLater(() -> { JFrame f = new JFrame("Captura de Rostros (Híbrido)");
            f.setContentPane(new CapturaRostrosForm().panelPrincipal);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true); }); }
}
