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
    private static int MIN_THRESH = 5;
    private static int MAX_THRESH = 75;
    private static int MAX_STORED_BOXES = 2;
    private static int MAX_STORED_CENTROIDS = 3;
    private static int MAX_STORED_FOREGROUNDS = 3;
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

    private LinkedList<Integer> centroids;
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
        centroids = new LinkedList<>();
        centroids.add(0);
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

    // remove listener
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        change.removePropertyChangeListener(listener);
    }

    // public method that does all processing
    public Mat ProcessImage(Mat newFrame) {
        current = newFrame;

        tmp = new Mat((int)current.size().height, (int)current.size().width, CvType.CV_8UC1);

        currentGray = new Mat(height, width, CvType.CV_8UC1);
        Imgproc.cvtColor(current, currentGray, Imgproc.COLOR_RGBA2GRAY);
        // Processing before calculating a position
        getBackground();
        getBinaryImage();
        postProcessing();

        /*
//            IF A DELTA MOVEMENT IS CALCULATED USE THESE FUNCTIONS
        //If the  white in the binary image takes up more than X% of the screen the we calculate the position of the centroid, otherwise ignore the image
        if ((double)Core.countNonZero(binary)  > (double)binary.total() * 0.005) {
            Log.e(TAG, "White takes up more than 2.5%");
            calculateCentroidWithThresh();
            calculateMovement();
            calculateBoxMovement();
            change.firePropertyChange("position", null, boxMovement.getFirst());
        }
        //Log.e(TAG, "NonZero: " + (double)Core.countNonZero(binary) + ", total: " + (double)binary.total() + ", 2.5%: " + (double)binary.total() * 0.005);
        else {
            // To little was detected, add an empty frame and move nothing
            addToList(centroids, 0, MAX_STORED_CENTROIDS);
            addToList(tempForegroundMovement, 0, MAX_STORED_FOREGROUNDS);
            addToList(foregroundMovement, 0, MAX_STORED_FOREGROUNDS);
            addToList(boxMovement, 0, MAX_STORED_BOXES);
        }
*/

//        IF THE CENTROID POSITION IS USED AS THE GOAL POSITION USE THIS

        if ((double)Core.countNonZero(binary)  > (double)binary.total() * 0.005) {
            Log.e(TAG, "White takes up more than 2.5%");
            calculateCentroid();
            calculateBoxPosition();
            change.firePropertyChange("position", boxMovement.get(1), boxMovement.getFirst());
        }
        //Log.e(TAG, "NonZero: " + (double)Core.countNonZero(binary) + ", total: " + (double)binary.total() + ", 2.5%: " + (double)binary.total() * 0.005);
        else {
            // To little was detected, add an empty frame and move nothing
            addToList(centroids, 0, MAX_STORED_CENTROIDS);
            addToList(tempForegroundMovement, 0, MAX_STORED_FOREGROUNDS);
            addToList(foregroundMovement, 0, MAX_STORED_FOREGROUNDS);
            addToList(boxMovement, 0, MAX_STORED_BOXES);
        }
        //Log.e(TAG, "Send delta Movement: " + boxMovement.getFirst());
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

    private void calculateCentroidWithThresh() {
        // calculate the centroid of binary image
        Moments moments = Imgproc.moments(binary, true);
        int centroid = (int)(moments.m10/moments.m00);

        //Log.e(TAG, "Centroid: " + centroid);

        // get the delta movement
        int delta = centroid - centroids.getFirst();

        //find out if centroid is close to last centroid, if not, count as noise
//        dont
        boolean noise = false;
        int loops = 1;
        for(int c : centroids) {
            if (Math.abs(centroid - c) < MIN_THRESH) {
                noise = true;
                break;
            } else if (Math.abs(centroid - c) > MAX_THRESH * loops) {
                noise = true;
                break;
            }

            loops++;
        }

        //add current centroid and remove last if full
        addToList(centroids, centroid, MAX_STORED_CENTROIDS);

        //Log.e(TAG, "Noise: " + noise);


        if(noise) {
            // centroid is noise, store the movement as 0
            addToList(tempForegroundMovement, 0, MAX_STORED_FOREGROUNDS);
        } else {
            // Centroid is close to previous centroids, therefore a valid value
            addToList(tempForegroundMovement, delta, MAX_STORED_FOREGROUNDS);
        }
    }

    private void calculateCentroid() {
        Moments moments = Imgproc.moments(binary, true);
        int centroid = (int)(moments.m10/moments.m00);
        addToList(centroids, centroid, MAX_STORED_CENTROIDS);
    }

    //Calculate how much the centroid moved
    private void calculateMovement() {
        Iterator<Integer> itr = tempForegroundMovement.iterator();
        // skipp the first value as this is the current frame
        itr.next();

        // if any of the previous movements is zero then we count this as a noise
        // and set the actual movement to 0
        boolean noise = false;
        while (itr.hasNext()) {
            int tempMovement = itr.next();
            if (tempMovement == 0) {
                addToList(foregroundMovement, 0, MAX_STORED_FOREGROUNDS);
                noise = true;
                break;
            }
        }

        if (!noise) {
            // no noise, we add the temp movement to the actuall movement
            addToList(foregroundMovement, tempForegroundMovement.getFirst(), MAX_STORED_FOREGROUNDS);
        }
    }

    //Calculate box movement fixed delta
    private void calculateBoxMovement() {
        //calculate a weighted sum of the previous movements
        int totalMovement = 0;
        int i = 0;
        int weightSum = 0;
        for (int m : foregroundMovement) {
            weightSum += (10-i*2);
            totalMovement += m*(10-i*2);
            i++;
        }

        //average movement Will be in whole numbers so this is not totally accurate
        int meanMovement = totalMovement / weightSum;

        //Log.e(TAG, "meanMovement: " + meanMovement);

        if(meanMovement > 0.1) {
            addToList(boxMovement, 20, MAX_STORED_BOXES);
        }
        else if (meanMovement < -0.1) {
            addToList(boxMovement, -20, MAX_STORED_BOXES);
        }
        else {
            addToList(boxMovement, 0, MAX_STORED_BOXES);
        }
    }

    //calculate box movement and use centroid position
    private void calculateBoxPosition() {
        int newRange = 940;
        int value = ((centroids.getFirst() * newRange / 720)) + 20;
        addToList(boxMovement, value, MAX_STORED_BOXES);
    }

    private void addToList(LinkedList list, int element, int maxStoredValues) {
        list.addFirst(element);
        if (list.size() > maxStoredValues)
            list.removeLast();
    }
}
