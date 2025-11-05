package app.utility;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;

public class PlantillaFacialUtil
{
    // Se obtiene la ruta del HaarCascade correctamente desde resources o fallback local
    private static final String CASCADE_PATH = obtenerCascadePath();
    private static final double MIN_BRIGHTNESS = 60.0; // más permisivo para poca luz

    private static String obtenerCascadePath() {
        try {
            // Buscar dentro de resources del proyecto
            var resource = PlantillaFacialUtil.class.getResource("/haarcascades/haarcascade_frontalface_default.xml");
            if (resource != null) {
                return new File(resource.toURI()).getAbsolutePath();
            }
        } catch (Exception ignored) { }

        // Fallback: ruta relativa (útil al correr desde IDE)
        return Paths.get("src", "main", "resources", "haarcascades", "haarcascade_frontalface_default.xml").toString();
    }

    // Genera una plantilla facial desde la imagen capturada
    public static byte[] generarPlantilla(String rutaImagen) {
        if (rutaImagen == null || rutaImagen.isEmpty()) {
            System.err.println("Ruta de imagen vacía o nula.");
            return null;
        }

        File file = new File(rutaImagen);
        if (!file.exists()) {
            System.err.println("No existe el archivo de imagen: " + rutaImagen);
            return null;
        }

        Mat img = opencv_imgcodecs.imread(rutaImagen, IMREAD_GRAYSCALE);
        if (img.empty()) {
            System.err.println("No se pudo leer la imagen (Mat vacío).");
            return null;
        }

        if (!rostroDetectado(img)) {
            System.out.println("No se detectó rostro en la imagen.");
            return null;
        }

        double brillo = promedioBrillo(img);
        if (brillo < MIN_BRIGHTNESS) {
            System.out.println("Iluminación insuficiente (" + brillo + "). Mejore la luz y reintente.");
            return null;
        }

        try {
            LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
            recognizer.train(new MatVector(img), new Mat(new int[]{0}));

            BytePointer bp = new BytePointer();
            recognizer.write(bp);

            byte[] bytes = new byte[(int) bp.limit()];
            bp.get(bytes);
            bp.close();

            return bytes;
        } catch (Exception e) {
            System.err.println("Error generando plantilla: " + e.getMessage());
            return null;
        }
    }

    // Compara una plantilla existente con una nueva imagen
    public static boolean compararPlantillas(byte[] plantillaBD, String rutaImagen) {
        if (plantillaBD == null || plantillaBD.length == 0) {
            System.err.println("Plantilla de BD vacía o nula.");
            return false;
        }

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

        System.out.println("Confianza obtenida: " + confidence[0]);
        return confidence[0] < 70; // umbral de similitud
    }
    private static boolean rostroDetectado(Mat img) {
        CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_PATH);
        if (faceDetector.empty()) {
            System.err.println("No se pudo cargar el clasificador HaarCascade.");
            return false;
        }

        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(img, faces, 1.1, 3, 0, new Size(80, 80), new Size(400, 400));
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
