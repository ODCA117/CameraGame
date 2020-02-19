package com.example.cameraapplication;

import android.util.Log;

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
import java.util.Iterator;
import java.util.LinkedList;

public class ImageProcessor {

    private static final String TAG = "Video::ImageProcessor";
    private static final int gauss = 3; //Must be an odd number!!

    private static double ALPHA = 0.6;
    private static int KERNELSIZE_ERODE = 5;
    private static int KERNELSIZE_DILATE = 1;
    private static int DILATETIMES = 1;
    private static int MIN_THRESH = 2;
    private static int MAX_THRESH = 200;
    private static int MAX_STORED_VALUE = 5;
    private static int BOX_MOVE_WEIGTH = 10;
    private static int BOX_MOVE_WEIGTH_REDUCE = 2;
    private static int TOTAL_BOXES = 30;


    private int width, height;

    private Mat backgroundGray;
    private Mat current;
    private Mat currentGray;
    private Mat previous;
    private Mat binary;
    private Mat tmp;

    private LinkedList<Integer> centroidPositions;
    private LinkedList<Integer> foregroundMovement;
    private LinkedList<Integer> tempForegroundMovement;
    private LinkedList<Integer> boxMovement;

    private PropertyChangeSupport change;

    ImageProcessor(int width, int height) {
        this.width = width;
        this.height = height;
        centroidPositions = new LinkedList<>();
        centroidPositions.add(0);
        foregroundMovement = new LinkedList<>();
        foregroundMovement.add(0);
        foregroundMovement.add(0);
        tempForegroundMovement = new LinkedList<>();
        tempForegroundMovement.add(0);
        tempForegroundMovement.add(0);
        boxMovement = new LinkedList<>();
        boxMovement.add(0);
        boxMovement.add(0);
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
        calculateMovement();
        calculatePosition();


        change.firePropertyChange("position", boxMovement.get(1), boxMovement.getFirst());

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
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT,
                new Size(2 * KERNELSIZE_ERODE + 1,2 * KERNELSIZE_ERODE + 1 ),
                new Point(KERNELSIZE_ERODE, KERNELSIZE_ERODE));
        Mat dialElement = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT,
                new Size(2 * KERNELSIZE_DILATE + 1,2 * KERNELSIZE_DILATE + 1 ),
                new Point(KERNELSIZE_DILATE, KERNELSIZE_DILATE));
        Imgproc.erode(binary, binary, erodeElement);

        for (int i = 0; i < DILATETIMES; i++)
            Imgproc.dilate(binary, binary, dialElement);
    }

    //store value for future usage
    private void calculateMovement() {
        Moments moments = Imgproc.moments(binary, true);
        int centroid = (int)(moments.m10/moments.m00);

        Log.d(TAG, "centroid: " + centroid);
        Log.d(TAG, "previous Position: " + centroidPositions.getFirst());


        boolean noise = false;

        Iterator<Integer> itr = centroidPositions.iterator();
        int delta = centroid - itr.next();

        if (delta < MIN_THRESH || delta > MAX_THRESH) {
            noise = true;
        }

        int loops = 1;
        while (itr.hasNext()) {
            int d = itr.next();
            if(d < MIN_THRESH || d > MAX_THRESH * loops) {
                //set tempForegroundMovement to 0 as this is noise
                noise = true;
                break;
            }
            loops++;
        }

        centroidPositions.addFirst(centroid);
        if (centroidPositions.size() > MAX_STORED_VALUE)
            centroidPositions.removeLast();

        if(noise) {
            //set tempForegroundMovement to 0 and movement to 0;
            //remove the last element in list as it is replaced
            tempForegroundMovement.addFirst(0);
            tempForegroundMovement.removeLast();

            foregroundMovement.addFirst(0);
            foregroundMovement.removeLast();
            //return as position will not change
            return;
        }

        //The tempForegroundMovement is valid and should be stored
        tempForegroundMovement.addFirst(delta);
        tempForegroundMovement.removeLast();

        //check if the previous tempMovements is 0 to remove impulse noise
        noise = false;
        for(int m : tempForegroundMovement) {
            if (m == 0) {
                noise = true;
            }
        }
        if (noise) {
            //impules detected, add 0 to movement,
            //remove the last element in Movement and return

            foregroundMovement.addFirst(0);
            foregroundMovement.removeLast();
            return;
        }

        //the delta is valid as movement and shall be stored
        foregroundMovement.addFirst(delta);
        foregroundMovement.removeLast();
        return;

    }

    // based on current centroid and previous centroids
    private void calculatePosition() {
        //low pass filter the last foregroundMovement
        int sum = 0;
        int i = 0;
        for (int b : boxMovement) {
            sum = b * (BOX_MOVE_WEIGTH - (i * BOX_MOVE_WEIGTH_REDUCE));
        }

        double meanBoxMove = sum / TOTAL_BOXES;

        if(meanBoxMove > 0.1) {
            boxMovement.addFirst(20);
        }
        else if (meanBoxMove < -0.1) {
            boxMovement.addFirst(-20);


        }
        else {
            boxMovement.addFirst(0);

        }
        boxMovement.removeLast();
    }
}
