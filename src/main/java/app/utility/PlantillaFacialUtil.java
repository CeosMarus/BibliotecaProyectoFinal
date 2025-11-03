package app.utility;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.global.opencv_imgcodecs;

import java.nio.ByteBuffer;

public class PlantillaFacialUtil
{
    // Genera un vector (plantilla) desde una imagen facial
    public static byte[] generarPlantilla(String rutaImagen) {
        Mat img = opencv_imgcodecs.imread(rutaImagen, opencv_imgcodecs.IMREAD_GRAYSCALE);

        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
        recognizer.train(new MatVector(img), new Mat(new int[]{0}));

        // Serializar modelo temporalmente a bytes
        BytePointer bp = new BytePointer();
        recognizer.write(bp);
        byte[] bytes = new byte[(int) bp.limit()];
        bp.get(bytes);
        return bytes;
    }

    // Reconstruir modelo desde bytes y comparar
    public static boolean compararPlantillas(byte[] plantillaBD, String rutaImagen) {
        LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();

        recognizer.read(new BytePointer(ByteBuffer.wrap(plantillaBD)));

        Mat img = opencv_imgcodecs.imread(rutaImagen, opencv_imgcodecs.IMREAD_GRAYSCALE);
        int[] label = new int[1];
        double[] confidence = new double[1];
        recognizer.predict(img, label, confidence);

        System.out.println("Confianza: " + confidence[0]);
        return confidence[0] < 70; // Ajustar tolerancia
    }
}
