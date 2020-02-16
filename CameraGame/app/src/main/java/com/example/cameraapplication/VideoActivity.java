package com.example.cameraapplication;

import android.hardware.Camera;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cameraapplication.gameLogic.GameEngine;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Collections;
import java.util.List;

public class VideoActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Video::Activity";


    Camera mCamera;
    CameraPreview mPreview;
    ImageView convertedImageView;
    LinearLayout layoutForImage;
    FrameLayout preview;
    GameView gameView;
    TextView data1;
    GameEngine gameEngine;


    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat rgbMat;
    private Mat greyMat;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


/* ------------------ stuff that is done in the old way ---------------
        // Make a image to put our converted preview frame.
        convertedImageView = new ImageView(this);

        // Get the mobiles camera and set it to our camera object.
        mCamera = getCameraInstance();
        // Create a layout to put our image.



        //initialize camera Data textview


        // Creates our own camera preview object to  be able to make changes to the previews.
        mPreview = new CameraPreview(this, mCamera, convertedImageView, layoutForImage, data1);
        mPreview.setVisibility(View.INVISIBLE);

        // Add our camerapreview to this activitys layout.
        preview = (FrameLayout) findViewById(R.id.frame_view);
        preview.addView(mPreview, 0);
        */

        layoutForImage = findViewById(R.id.camera_layout);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_surface_View);
        mOpenCvCameraView.setMaxFrameSize(640, 480);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        data1 = findViewById(R.id.camera_data1);


        //initialize gameView and Game Engine initialization
        gameView = findViewById(R.id.gameView);
        gameEngine = new GameEngine();
        gameView.init(gameEngine);

        //This is done to not show the real preview frame, and only our ImageView.
        //preview.setVisibility(View.INVISIBLE);
    }


    // This is connected to the lifecycle of the activity
    @Override
    protected void onPause() {
        super.onPause();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        /*
        gameEngine = null;
        gameView = null;

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }

        mPreview = null;
        */


    }

    // This is connected to the lifecycle of the activity
    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        /*
        gameEngine = new GameEngine();
        gameView = findViewById(R.id.gameView);
        gameView.init(gameEngine);

        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera, convertedImageView,layoutForImage, data1);
        preview.addView(mPreview);
        preview.setVisibility(View.INVISIBLE);
        */
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //needed to be able to preview
    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

//    /** A safe way to get an instance of the Camera object. */
//    public static Camera getCameraInstance(){
//        Camera c = null;
//        try {
//            c = Camera.open(); // attempt to get a Camera instance
//        }
//        catch (Exception e){
//            // Camera is not available (in use or does not exist)
//        }
//        return c; // returns null if camera is unavailable
//    }

    public void onClick(View view) {

        restartGame();
        if (!gameEngine.running()) {

            //mPreview.setVisibility(View.VISIBLE);
        }
    }

    public void restartGame() {
        gameEngine.restart();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {



    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        rgbMat = inputFrame.rgba();
        greyMat = inputFrame.gray();

        Log.e(TAG, "called from the frame");
        return rgbMat;
    }
}

