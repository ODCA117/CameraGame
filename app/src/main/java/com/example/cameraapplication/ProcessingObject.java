package com.example.cameraapplication;

import org.apache.commons.math3.linear.RealMatrix;
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

    private List<Integer> previousPositions;

    ProcessingObject(int width, int height) {
        this.width = width;
        this.height = height;

        previousPositions = new LinkedList<>();
    }

    public int ProcessImage(int[] newFrame) {

        return 480;



    }

    private void getBackground() {


    }

    // this - background
    // convert to gray and get a threashold
    private void getTheDifferenceImage() {
    }

    //covert image to binary
    private void getBinaryImage() {

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
