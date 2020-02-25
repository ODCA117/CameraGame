package com.example.cameraapplication;

import android.hardware.Camera;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

public class VideoActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "Video::Activity";
    private static final int WIDTH = 480;
    private static final int HEIGHT = 620;


    Camera mCamera;
    CameraPreview mPreview;
    ImageView convertedImageView;
    LinearLayout layoutForImage;
    FrameLayout preview;
    GameView gameView;
    TextView data1;
    GameEngine gameEngine;
    ImageProcessor imageProcessor;


    private MyJavaCameraView mOpenCvCameraView;
    private Mat mRgba;
    private Mat mRgbaF;
    private Mat mRgbaT;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Find the camera view from the layout and set the preview size.
        layoutForImage = findViewById(R.id.camera_layout);
        mOpenCvCameraView = findViewById(R.id.camera_surface_View);

        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        //Add this activity as a listener to get frames from the camera preview
        mOpenCvCameraView.setCvCameraViewListener(this);

        //Create an Image processor that does all processing of the image
        imageProcessor = new ImageProcessor(WIDTH, HEIGHT);

        //initialize gameView and Game Engine
        gameView = findViewById(R.id.gameView);
        gameEngine = new GameEngine(imageProcessor);
        gameView.init(gameEngine);
    }


    // This is connected to the lifecycle of the activity
    // Release everything when the app is paused
    @Override
    protected void onPause() {
        super.onPause();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        gameView.stop();
        gameView = null;
        gameEngine = null;
        imageProcessor = null;

        Log.e(TAG, "onPause");
    }

    // This is connected to the lifecycle of the activity
    // Create everything when the app is resumed after a pause
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

        imageProcessor = new ImageProcessor(WIDTH,HEIGHT);
        gameEngine = new GameEngine(imageProcessor);
        gameView = findViewById(R.id.gameView);
        gameView.init(gameEngine);

        mOpenCvCameraView = findViewById(R.id.camera_surface_View);
        Log.e(TAG, "onResume");

    }

    // Release and stop everything when app is shut down
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

        if (gameView != null) {
            gameView.stop();
            gameView = null;
        }
        gameEngine = null;
        imageProcessor = null;
        Log.e(TAG, "onDestroy");
    }

    //needed to be able to get preview frames
    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    // Method for start game button which will reset the game
    public void onClick(View view) {
        restartGame();
    }

    // resets the game
    public void restartGame() {
        gameEngine.restart();
    }

    // When the preview is started we create some matrices, These must be created after openCV is initilized
    // which is why this is done here
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
        imageProcessor.initProcessor();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    // Method called every time a new frame arrives
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //get the frame in color
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 1 );

        //process the image
        mRgba = imageProcessor.ProcessImage(mRgba);


        return mRgba; // return the matrix we want to display
    }
}


