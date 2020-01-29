package com.example.cameraapplication;

import android.hardware.Camera;
import android.os.Bundle;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cameraapplication.gameLogic.GameEngine;

public class VideoActivity extends AppCompatActivity {
    Camera mCamera;
    CameraPreview mPreview;
    ImageView convertedImageView;
    LinearLayout layoutForImage;
    FrameLayout preview;
    GameView gameView;
    TextView data1;
    GameEngine gameEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Make a image to put our converted preview frame.
        convertedImageView = new ImageView(this);

        // Get the mobiles camera and set it to our camera object.
        mCamera = getCameraInstance();

        // Create a layout to put our image.
        layoutForImage = findViewById(R.id.camera_layout);

        //initialize gameView

        gameEngine = new GameEngine();
        gameView = findViewById(R.id.gameView);
        gameView.init(gameEngine);

        //initialize camera Data textview
        data1 = findViewById(R.id.camera_data1);


        // Creates our own camera preview object to  be able to make changes to the previews.
        mPreview = new CameraPreview(this, mCamera, convertedImageView, layoutForImage, data1);


        // Add our camerapreview to this activitys layout.
        preview = (FrameLayout) findViewById(R.id.frame_view);
        preview.addView(mPreview, 0);

        //add playerRectangle to this activitys view

        //This is done to not show the real preview frame, and only our ImageView.
        preview.setVisibility(View.INVISIBLE);
    }



    // This is connected to the lifecycle of the activity
    @Override
    protected void onPause() {
        super.onPause();
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

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}
