package com.qburst.fakeimagedetection.core.processor;

import com.qburst.fakeimagedetection.core.listener.NeuralnetProcessorListener;
import com.qburst.fakeimagedetection.core.multithread.NotifyingThread;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javax.imageio.ImageIO;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.imgrec.ImageRecognitionPlugin;

public class NeuralNetProcessor extends NotifyingThread {

    static BufferedImage image;
    public static NeuralNetwork nnet;
    static ImageRecognitionPlugin imageRecognition;
    public static String nNetworkpath = "nnet/CNN2.nnet";
    NeuralnetProcessorListener listener;

    public void setListener(NeuralnetProcessorListener listener) {
        this.listener = listener;
    }

    public static void main(String[] args) {
        try {
            System.out.println("usage java -jar nn.jar image_to_be_processed file_of_neural_network");
            System.out.println("Loading Image....");
            image = ImageIO.read(new File(args[0]));
            System.out.println("Loading NN....");
            File NNetwork = new File(args[1]);
            if (!NNetwork.exists()) {
                System.err.println("Cant Find NN");
                return;
            }
            nnet = NeuralNetwork.load(new FileInputStream(NNetwork)); // load trained neural network saved with Neuroph Studio
            System.out.println("Load Image Recog Plugin....");
            imageRecognition = (ImageRecognitionPlugin) nnet.getPlugin(ImageRecognitionPlugin.class); // get the image recognition plugin from neural network
            System.out.println("Recognize Image....");
            HashMap<String, Double> output = imageRecognition.recognizeImage(image);
            System.out.println("Output is....");
            System.out.println(output.toString());
        } catch (IOException ex) {
            Logger.getLogger(NeuralNetProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public NeuralNetProcessor(BufferedImage image) {
        this.image = image;
    }

    void notifyUser() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Neural Network Missing");
                alert.setHeaderText("Cant find network file");
                alert.setContentText("Please make sure that CNN2.nnet is located at nnet/CNN2.nnet");
                alert.showAndWait();
            }
        });
    }

    @Override
    public void doRun() {
        try {
            if (nnet == null) { //Bypass network reload during comeback through home button
                File NNetwork = new File(nNetworkpath);
                System.out.println("Nueral network loaded = " + NNetwork.getAbsolutePath());
                if (!NNetwork.exists()) {
                    notifyUser();
                    return;
                }
                nnet = NeuralNetwork.load(new FileInputStream(NNetwork)); // load trained neural network saved with Neuroph Studio
                System.out.println("Learning Rule = " + nnet.getLearningRule());
                imageRecognition = (ImageRecognitionPlugin) nnet.getPlugin(ImageRecognitionPlugin.class); // get the image recognition plugin from neural network
            }
            HashMap<String, Double> output = imageRecognition.recognizeImage(image);
            if (output == null) {
                System.err.println("Image Recognition Failed");
            }
            System.out.println(output.toString());
            listener.neuralnetProcessCompleted(output);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NeuralNetProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}