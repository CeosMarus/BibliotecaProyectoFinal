package app.utility;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

import java.io.File;

public class WebcamHelper
{
    private OpenCVFrameGrabber grabber;
    private CanvasFrame canvas;

    public void iniciarCamara() throws FrameGrabber.Exception {
        grabber = new OpenCVFrameGrabber(0); // Cámara principal
        grabber.start();
        canvas = new CanvasFrame("Verificación Facial", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        canvas.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
    }

    public String capturarFoto() throws Exception {
        if (grabber == null) return null;
        Frame frame = grabber.grab();
        if (frame == null) return null;

        // Convertir a Mat para procesamiento con OpenCV
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Mat mat = converter.convert(frame);

        String ruta = System.getProperty("java.io.tmpdir") + "/rostro_temp.jpg";
        imwrite(ruta, mat);
        return ruta;
    }

    public void detenerCamara() throws FrameGrabber.Exception {
        if (grabber != null) grabber.stop();
        if (canvas != null) canvas.dispose();
    }
}
