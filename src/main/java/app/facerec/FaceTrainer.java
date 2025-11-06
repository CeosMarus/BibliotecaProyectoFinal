package app.facerec;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

public class FaceTrainer {

    private final String cascadePath;
    private final int numCapturas;

    public FaceTrainer(String cascadePath, File ignoredFacesDir, int numCapturas) {
        this.cascadePath = cascadePath;
        this.numCapturas = numCapturas;
    }

    public File enrolUser(int userId, int cameraIndex) throws Exception {
        CascadeClassifier faceDetector = new CascadeClassifier(cascadePath);
        if (faceDetector.empty()) {
            throw new Exception("No se pudo cargar el clasificador de rostros");
        }

        VideoCapture camera = new VideoCapture(cameraIndex);
        if (!camera.isOpened()) {
            throw new Exception("No se pudo abrir la cámara");
        }

        List<Mat> rostros = new ArrayList<>();
        List<Integer> etiquetas = new ArrayList<>();

        Mat frame = new Mat();
        int capturadas = 0;

        System.out.println("Iniciando captura de rostros para el usuario " + userId);

        while (capturadas < numCapturas) {
            if (!camera.read(frame) || frame.empty()) continue;

            Mat gray = new Mat();
            opencv_imgproc.cvtColor(frame, gray, COLOR_BGR2GRAY);

            RectVector rostrosDetectados = new RectVector();
            faceDetector.detectMultiScale(gray, rostrosDetectados);

            if (rostrosDetectados.size() == 0) continue;

            for (int i = 0; i < rostrosDetectados.size(); i++) {
                Rect rect = rostrosDetectados.get(i);
                Mat rostro = new Mat(gray, rect);
                resize(rostro, rostro, new Size(160, 160));

                rostros.add(rostro.clone());
                etiquetas.add(userId);
                capturadas++;

                System.out.println("Captura " + capturadas + "/" + numCapturas);

                if (capturadas >= numCapturas) break;
                Thread.sleep(500);
            }
        }

        camera.release();

        // Crear o cargar modelo global
        File modelFile = new File("data/modelos/lbph_model.xml");
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create(1, 8, 8, 8, 75);

        if (modelFile.exists()) {
            recognizer.read(modelFile.getAbsolutePath());
            System.out.println("Modelo existente cargado");
        }

        // Preparar imágenes y etiquetas
        MatVector imagenes = new MatVector(rostros.size());
        Mat labels = new Mat(rostros.size(), 1, opencv_core.CV_32SC1);
        IntBuffer etiquetasBuf = labels.createBuffer();

        for (int i = 0; i < rostros.size(); i++) {
            imagenes.put(i, rostros.get(i));
            etiquetasBuf.put(i, etiquetas.get(i));
        }

        // Actualizar modelo con nuevos datos
        if (modelFile.exists()) {
            recognizer.update(imagenes, labels);
            System.out.println("Modelo LBPH actualizado con usuario " + userId);
        } else {
            recognizer.train(imagenes, labels);
            System.out.println("Modelo LBPH entrenado desde cero con usuario " + userId);
        }

        // Guardar modelo global
        modelFile.getParentFile().mkdirs();
        recognizer.save(modelFile.getAbsolutePath());

        return modelFile;
    }
}
