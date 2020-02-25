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

    // Constructor:
    // set height and width of the frames
    // initialize the lists that stores previous values and add 0 as element to them
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

        // Used to communicate with game engine
        change = new PropertyChangeSupport(this);
    }

    //create matrices
    public void initProcessor() {
        tmp = new Mat(height, width, CvType.CV_8UC1);
        currentGray = new Mat(height, width, CvType.CV_8UC1);
        binary = new Mat(height, width, CvType.CV_8UC1);
    }

    // add listener
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        change.addPropertyChangeListener(listener);
    }

    // public method that does all processing
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

    //Get the background
    private void getBackground() {
        //if no background exists before, this frame is the background
        if(backgroundGray == null) {
            backgroundGray = currentGray;

        }
        // Else combine current frame with previous background
        else {
            Core.multiply(currentGray, new Scalar(ALPHA), tmp);
            Core.multiply(backgroundGray, new Scalar(1-ALPHA), backgroundGray);
            Core.add(tmp, backgroundGray, backgroundGray);
        }

        //filter the current background with gauss
        // NOTE: we store the filtered background, this may be wrong!!!!
        Imgproc.GaussianBlur(backgroundGray, backgroundGray, new Size(gauss,gauss), 0);
    }

    // this - backgroundGray
    // convert to gray and convert to binary
    private void getBinaryImage() {
        //apply gaussianfilter
        Imgproc.GaussianBlur(currentGray, currentGray, new Size(gauss,gauss), 0);

        // subtract backgroundGray from current image and store in currentGray
        Core.subtract(currentGray, backgroundGray, currentGray);

        //get Binary image using Otsu
        Imgproc.threshold(currentGray, binary, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
    }


    //process the binary image to reduce noice and make the person stand out
    private void postProcessing() {
        // create the element for erode
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT,
                new Size(2 * KERNELSIZE_ERODE + 1,2 * KERNELSIZE_ERODE + 1 ),
                new Point(KERNELSIZE_ERODE, KERNELSIZE_ERODE));
        // create the element for dialte
        Mat dialElement = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT,
                new Size(2 * KERNELSIZE_DILATE + 1,2 * KERNELSIZE_DILATE + 1 ),
                new Point(KERNELSIZE_DILATE, KERNELSIZE_DILATE));

        // erode image to remove small noise
        Imgproc.erode(binary, binary, erodeElement);

        // dilate the image DILATETIMES to increase what we see
        for (int i = 0; i < DILATETIMES; i++)
            Imgproc.dilate(binary, binary, dialElement);
    }

    //TODO: This must be redone to handle when the centroid is strange
    //store value for future usage
    private void calculateMovement() {
        // calculate the centroid of binary image
        Moments moments = Imgproc.moments(binary, true);
        int centroid = (int)(moments.m10/moments.m00);

        Log.d(TAG, "centroid: " + centroid);
        Log.d(TAG, "previous Position: " + centroidPositions.getFirst());


        // Find if the new centroid is close to the last centroid
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

        //Add the current centroid to storage list and remove the last if the list is "full"
        // NOTE: May be wrong as this is a strange value??
        centroidPositions.addFirst(centroid);
        if (centroidPositions.size() > MAX_STORED_VALUE)
            centroidPositions.removeLast();

        // if noise, then add 0 as movement and remove last value to keep size consistent
        // NOTE: we have to decide a size to use here
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

        //check if the previous tempMovements is 0, if so remove noise, Will create one frame delay
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

        // if the box moved in positive direction, move it 20 pixels in positive directions
        if(meanBoxMove > 0.1) {
            boxMovement.addFirst(20);
        }
        // if the box moved in negative direction, move it 20 pixels in negative directions
        else if (meanBoxMove < -0.1) {
            boxMovement.addFirst(-20);
        }

        // else don't move at all
        else {
            boxMovement.addFirst(0);
        }

        boxMovement.removeLast();
    }
}
