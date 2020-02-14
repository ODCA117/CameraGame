package com.example.cameraapplication;

import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

public class ProcessingObject {

    private static double ALPHA = 0.7;
    private static int MIN_THRESH = 5;
    private static int MAX_THRESH = 25;
    private int width, height;

    private Mat currentMat, backgroundMat, previousMat, grayMat, diffMat, binMat;
    private List<int[]> previousPositions;

    ProcessingObject(int width, int height) {
        this.width = width;
        this.height = height;

        /*
        currentMat = new Mat(width, height, CvType.CV_32F);
        grayMat = new Mat(width, height, CvType.CV_32F);
        diffMat = new Mat();
        binMat = new Mat();
        */

        currentMat = new Mat();
        previousMat = new Mat();
        grayMat = new Mat();
        diffMat = new Mat();
        binMat = new Mat();


        previousPositions = new LinkedList<>();
    }

    public int ProcessImage(int[] newFrame) {
        previousMat = currentMat;
        currentMat.put(width,height, newFrame);
        getBackground();
        getTheDifferenceImage();

        return 480;

    }

    private void getBackground() {
        if(backgroundMat == null) {
            backgroundMat = currentMat;
        }
        else {
            //backgroundMat = (1-ALPHA)* currentMat + ALPHA * backgroundMat; //conversion between double and int does not work
        }


    }

    // this - background
    // convert to gray and get a threashold
    private void getTheDifferenceImage() {
        Imgproc.cvtColor(currentMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        grayMat.convertTo(grayMat, CvType.CV_32F);
        Core.subtract(Mat.ones(grayMat.size(),CvType.CV_32F),backgroundMat,diffMat); //why not converting background to grayscale??
    }

    //covert image to binary
    private void getBinaryImage() {
        Imgproc.cvtColor(diffMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        grayMat.convertTo(grayMat, CvType.CV_32F);
        Imgproc.threshold(grayMat, binMat, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
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
