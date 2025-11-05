package app.facerec;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class FaceTrainer {
    private final CascadeClassifier detector;
    private final File dataDir;
    private final int samples;

    public FaceTrainer(String cascadePath, File dataDir, int samples) {
        this.detector = new CascadeClassifier(cascadePath);
        this.dataDir = dataDir;
        this.samples = Math.max(3, samples);
        if (!dataDir.exists()) dataDir.mkdirs();
    }

    /**
     * Captura múltiples rostros del usuario y entrena un modelo LBPH
     */
    public File enrolUser(int userId, int cameraIndex) throws Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(cameraIndex);
        grabber.start();
        OpenCVFrameConverter.ToMat conv = new OpenCVFrameConverter.ToMat();

        JFrame ventana = new JFrame("Captura de Rostro");
        JLabel lbl = new JLabel();
        ventana.setSize(640, 480);
        ventana.add(lbl);
        ventana.setVisible(true);

        int count = 0;
        List<Mat> faces = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        while (count < samples) {
            Frame frame = grabber.grab();
            if (frame == null) continue;

            Mat mat = conv.convert(frame);
            if (mat == null || mat.empty()) continue;

            Mat gray = new Mat();
            cvtColor(mat, gray, COLOR_BGR2GRAY);
            equalizeHist(gray, gray);

            RectVector rostros = new RectVector();
            detector.detectMultiScale(gray, rostros);

            for (int i = 0; i < rostros.size(); i++) {
                Rect r = rostros.get(i);
                rectangle(mat, r, new Scalar(0, 255, 0, 0));

                Mat rostro = new Mat(gray, r);
                resize(rostro, rostro, new Size(160, 160));
                faces.add(rostro);
                labels.add(userId);

                count++;
                String filename = new File(dataDir, "user." + userId + "." + count + ".jpg").getAbsolutePath();
                imwrite(filename, rostro);

                System.out.println("Captura " + count + "/" + samples);
                Thread.sleep(500);
            }

            Image img = new Java2DFrameConverter().getBufferedImage(frame);
            lbl.setIcon(new ImageIcon(img.getScaledInstance(lbl.getWidth(), lbl.getHeight(), java.awt.Image.SCALE_SMOOTH)));
            ventana.repaint();
        }

        grabber.stop();
        ventana.dispose();

        // Entrenar modelo LBPH
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        MatVector mats = new MatVector(faces.size());
        Mat labelsMat = new Mat(faces.size(), 1, org.bytedeco.opencv.global.opencv_core.CV_32SC1);
        IntBuffer labelsBuf = labelsMat.createBuffer();

        for (int i = 0; i < faces.size(); i++) {
            mats.put(i, faces.get(i));
            labelsBuf.put(i, labels.get(i));
        }

        recognizer.train(mats, labelsMat);

        File modelFile = new File(dataDir, "lbph_model.xml");
        recognizer.save(modelFile.getAbsolutePath());
        return modelFile;
    }
}
