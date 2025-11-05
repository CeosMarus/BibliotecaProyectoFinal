package app.facerec;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_core.mean;

public class FaceAuth {
    private LBPHFaceRecognizer recognizer;
    private CascadeClassifier detector;
    private double threshold;
    private static final double LUZ_MINIMA = 60.0;

    /**
     * Constructor
     * @param modelPath ruta del modelo LBPH entrenado (ej. data/modelos/lbph_model.xml)
     * @param cascadePath ruta del clasificador Haarcascade
     * @param threshold umbral de tolerancia (menor = más estricto)
     */
    public FaceAuth(String modelPath, String cascadePath, double threshold) {
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new RuntimeException("Modelo facial no encontrado: " + modelPath);
        }

        this.threshold = threshold;
        this.recognizer = LBPHFaceRecognizer.create();
        this.recognizer.read(modelFile.getAbsolutePath());
        this.detector = new CascadeClassifier(cascadePath);
    }

    /**
     * Abre la cámara y trata de reconocer un rostro
     * @param cameraIndex índice de la cámara (0 por defecto)
     * @param maxSegundos tiempo máximo antes de cancelar
     * @return ID del usuario reconocido o null si no hay coincidencia
     */
    public Integer predictUserIdFromWebcam(int cameraIndex, int maxSegundos) {
        Integer userId = null;
        long start = System.currentTimeMillis();

        try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(cameraIndex)) {
            grabber.start();
            OpenCVFrameConverter.ToMat conv = new OpenCVFrameConverter.ToMat();

            JFrame ventana = new JFrame("Verificación Facial");
            JLabel lbl = new JLabel();
            ventana.setSize(640, 480);
            ventana.add(lbl);
            ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventana.setVisible(true);

            while (System.currentTimeMillis() - start < maxSegundos * 1000L) {
                Frame f = grabber.grab();
                if (f == null) continue;
                Mat frame = conv.convert(f);
                if (frame == null || frame.empty()) continue;

                Mat gray = new Mat();
                cvtColor(frame, gray, COLOR_BGR2GRAY);
                equalizeHist(gray, gray);

                RectVector rostros = new RectVector();
                detector.detectMultiScale(gray, rostros);

                if (rostros.size() == 0) {
                    putText(frame, "No se detecta rostro", new Point(30, 30),
                            FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255, 0));
                } else {
                    for (int i = 0; i < rostros.size(); i++) {
                        Rect r = rostros.get(i);
                        rectangle(frame, r, new Scalar(0, 255, 0, 0));

                        Mat rostro = new Mat(gray, r);
                        Scalar brillo = mean(rostro);
                        if (brillo.get(0) < LUZ_MINIMA) {
                            putText(frame, "Iluminacion insuficiente", new Point(20, 50),
                                    FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 0, 255, 0));
                            continue;
                        }

                        IntPointer label = new IntPointer(1);
                        DoublePointer confidence = new DoublePointer(1);
                        recognizer.predict(rostro, label, confidence);
                        int predictedId = label.get(0);
                        double conf = confidence.get(0);

                        if (conf < threshold) {
                            userId = predictedId;
                            putText(frame, "Reconocido ID=" + userId, new Point(20, 30),
                                    FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 0, 0));
                            ventana.repaint();
                            Thread.sleep(1500);
                            ventana.dispose();
                            return userId;
                        } else {
                            putText(frame, "No coincide (" + (int) conf + ")", new Point(20, 30),
                                    FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255, 0));
                        }
                    }
                }

                Image img = new Java2DFrameConverter().getBufferedImage(f);
                lbl.setIcon(new ImageIcon(img.getScaledInstance(lbl.getWidth(), lbl.getHeight(), Image.SCALE_SMOOTH)));
                ventana.repaint();
            }

            ventana.dispose();
            grabber.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return userId;
    }
}
