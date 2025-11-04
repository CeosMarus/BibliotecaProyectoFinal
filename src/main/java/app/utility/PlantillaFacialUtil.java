package app.utility;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.nio.ByteBuffer;
import java.nio.file.Paths;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;

public class PlantillaFacialUtil
{
    private static final String CASCADE_PATH = Paths.get("src/main/resources/haarcascades/haarcascade_frontalface_default.xml").toString();
    private static final double MIN_BRIGHTNESS = 70.0; // brillo mínimo recomendado

    // Genera un vector (plantilla) desde una imagen facial
    public static byte[] generarPlantilla(String rutaImagen) {
        Mat img = opencv_imgcodecs.imread(rutaImagen, opencv_imgcodecs.IMREAD_GRAYSCALE);

        //Validacion de brillo y deteccion de rostro
        if (img.empty()) return null;

        if (!rostroDetectado(img)) {
            System.out.println("No se detectó rostro en la imagen.");
            return null;
        }

        if (promedioBrillo(img) < MIN_BRIGHTNESS) {
            System.out.println("Iluminación insuficiente. Mejore la luz y reintente.");
            return null;
        }

        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.train(new MatVector(img), new Mat(new int[]{0}));

        // Serializar modelo temporalmente a bytes
        BytePointer bp = new BytePointer();
        recognizer.write(bp);
        byte[] bytes = new byte[(int) bp.limit()];
        bp.get(bytes);
        return bytes;
    }

    // Compara una plantilla almacenada con una imagen nueva
    public static boolean compararPlantillas(byte[] plantillaBD, String rutaImagen) {
        Mat img = opencv_imgcodecs.imread(rutaImagen, IMREAD_GRAYSCALE);
        if (img.empty() || !rostroDetectado(img)) {
            System.out.println("No se detectó rostro válido para comparación.");
            return false;
        }

        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.read(new BytePointer(ByteBuffer.wrap(plantillaBD)));

        int[] label = new int[1];
        double[] confidence = new double[1];
        recognizer.predict(img, label, confidence);

        System.out.println("Confianza: " + confidence[0]);
        return confidence[0] < 70;
    }

    // Detecta si hay al menos un rostro visible en la imagen
    private static boolean rostroDetectado(Mat img) {
        CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_PATH);
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(img, faces);
        return faces.size() > 0;
    }

    // Calcula el brillo promedio de la imagen
    private static double promedioBrillo(Mat img) {
        Mat mean = new Mat();
        Mat stddev = new Mat();
        opencv_core.meanStdDev(img, mean, stddev);
        return mean.createIndexer().getDouble(0);
    }
}
