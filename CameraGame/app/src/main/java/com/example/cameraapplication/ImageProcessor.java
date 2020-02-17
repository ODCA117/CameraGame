package com.example.cameraapplication;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

public class ImageProcessor {

    private static final String TAG = "Video::ImageProcessor";
    private static final int gauss = 13; //Must be an odd numver!!

    private static double ALPHA = 0.7;
    private static int MIN_THRESH = 5;
    private static int MAX_THRESH = 25;
    private int width, height;

    private Mat backgroundGray;
    private Mat current;
    private Mat currentGray;
    private Mat previous;
    private Mat binary;
    private Mat tmp;

    private List<Integer> previousPositions;

    ImageProcessor(int width, int height) {
        this.width = width;
        this.height = height;
        previousPositions = new LinkedList<>();

    }

    public void initProcessor() {
        tmp = new Mat(height, width, CvType.CV_8UC1);
        currentGray = new Mat(height, width, CvType.CV_8UC1);
        binary = new Mat(height, width, CvType.CV_8UC1);
    }

    public Mat ProcessImage(Mat newFrame) {
        current = newFrame;

            tmp = new Mat((int)current.size().height, (int)current.size().width, CvType.CV_8UC1);

            currentGray = new Mat(height, width, CvType.CV_8UC1);
        Imgproc.cvtColor(current, currentGray, Imgproc.COLOR_RGBA2GRAY);
        getBackground();
        getBinaryImage();

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
    private void reduceNoise() {

    }

    //store value for future usage
    private void calculateTheCentroid() {

    }

    // based on current centroid and previous centroids
    private void detectDirection() {

    }
}
