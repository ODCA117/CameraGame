package com.example.cameraapplication;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;

public class ImageProcessor {

    private static final String TAG = "Video::ImageProcessor";
    private static final int gauss = 3; //Must be an odd numver!!

    private static double ALPHA = 0.7;
    private static int KERNELSIZE = 1;
    private static int DILATETIMES = 2;
    private static int MIN_THRESH = 2;
    private static int MAX_THRESH = 200;
    private static int MAX_STORED_VALUE = 5;
    private static int WEIGTH = 2;

    private int width, height;

    private Mat backgroundGray;
    private Mat current;
    private Mat currentGray;
    private Mat previous;
    private Mat binary;
    private Mat tmp;

    private LinkedList<Integer> previousPositions;
    private LinkedList<Integer> previousDeltas;

    private PropertyChangeSupport change;

    ImageProcessor(int width, int height) {
        this.width = width;
        this.height = height;
        previousPositions = new LinkedList<>();
        change = new PropertyChangeSupport(this);
    }

    public void initProcessor() {
        tmp = new Mat(height, width, CvType.CV_8UC1);
        currentGray = new Mat(height, width, CvType.CV_8UC1);
        binary = new Mat(height, width, CvType.CV_8UC1);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        change.addPropertyChangeListener(listener);
    }

    public Mat ProcessImage(Mat newFrame) {
        current = newFrame;

            tmp = new Mat((int)current.size().height, (int)current.size().width, CvType.CV_8UC1);

            currentGray = new Mat(height, width, CvType.CV_8UC1);
        Imgproc.cvtColor(current, currentGray, Imgproc.COLOR_RGBA2GRAY);
        getBackground();
        getBinaryImage();
        postProcessing();
        calculateTheCentroid();

        if(previousPositions.size() > 1)
            change.firePropertyChange("position", previousPositions.get(1), previousPositions.getFirst());

        return binary;
    }

    private void getBackground() {

        if(backgroundGray == null) {
            //set first frame, apply gaussian filter
            backgroundGray = currentGray;
            Imgproc.GaussianBlur(backgroundGray, backgroundGray, new Size(gauss,gauss), 0);

        } else {
            //calc next frame, convert to gray, apply gaussfilt
            Core.multiply(currentGray, new Scalar(ALPHA), tmp);
            Core.multiply(backgroundGray, new Scalar(0.5), backgroundGray);
            Core.add(tmp, backgroundGray, backgroundGray);

            Imgproc.GaussianBlur(backgroundGray, backgroundGray, new Size(gauss,gauss), 0);
        }
    }

    // this - backgroundGray
    // convert to gray and convert to binary
    private void getBinaryImage() {
        //apply gaussianfilter
        Imgproc.GaussianBlur(currentGray, currentGray, new Size(gauss,gauss), 0);

        // subtract backgroundGray
        Core.subtract(currentGray, backgroundGray, currentGray);

        //get Binary image using Otsu
        Imgproc.threshold(currentGray, binary, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
    }



    //process the binary image to reduce noice and make the person stand out
    private void postProcessing() {
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT,
                new Size(2 * KERNELSIZE + 1,2 * KERNELSIZE + 1 ),
                new Point(KERNELSIZE, KERNELSIZE));
        Imgproc.erode(binary, binary, element);
        for (int i = 0; i < DILATETIMES; i++)
            Imgproc.dilate(binary, binary, element);
    }

    //store value for future usage
    private void calculateTheCentroid() {
        Moments moments = Imgproc.moments(binary, true);
        int centroid = (int)(moments.m10/moments.m00);

        boolean noise = false;

        for(int p : previousPositions) {
            if(centroid - p < MIN_THRESH || centroid - p > MAX_THRESH) {
                centroid = 0;
                noise = true;
                break;
            }
        }

        if (!noise) {
            int delta = centroid - previousPositions.getFirst();

            previousPositions.addFirst(centroid);
            if (previousPositions.size() > MAX_STORED_VALUE)
                previousPositions.removeLast();

            previousDeltas.addFirst(delta);
            if (previousDeltas.size() > MAX_STORED_VALUE)
                previousDeltas.removeLast();





        }


    }

    // based on current centroid and previous centroids
    private void calculatePosition() {


    }
}
