package com.example.cameraapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/** A basic Camera preview class */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int imageFormat;
    private boolean mProcessInProgress = false;
    private Bitmap mBitmap = null;
    private ImageView myCameraPreview;
    private int[] pixels = null;
    private Camera.Parameters params;
    private TextView data1;

    public static final int WIDTH = 640;
    public static final int HEIGT = 480;

    public CameraPreview(Context context, Camera camera, ImageView mCameraPreview, LinearLayout layout, TextView data1) {
        super(context);
        mCamera = camera;
        params = mCamera.getParameters();
        imageFormat = params.getPreviewFormat();

        //Make sure that the preview size actually exists, and set it to our values
        for (Camera.Size previewSize: mCamera.getParameters().getSupportedPreviewSizes())
        {
            if(previewSize.width == WIDTH && previewSize.height == HEIGT) {
                params.setPreviewSize(previewSize.width, previewSize.height);
                break;
            }
        }

        mCamera.setParameters(params);
        myCameraPreview = mCameraPreview;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        layout.addView(myCameraPreview);

        this.data1 = data1;

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {

            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Taken care of in our activity.
    }

    /* If the application is allowed to rotate, here is where you would change the camera preview
    * size and other formatting changes.*/
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e){
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    /*This method is overridden from the camera class to do stuff on every frame that is taken
    * from the camera, in the form of the byte[] bytes array.
    *
    * */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (imageFormat == ImageFormat.NV21){
            if(mProcessInProgress){
                mCamera.addCallbackBuffer(bytes);
            }
            if (bytes == null){
                return;
            }
            mCamera.addCallbackBuffer(bytes);

            /*
            * Here we rotate the byte array (because of some wierd feature in my phone at least)
            * if your picture is horizontal, delete the rotation of the byte array.
            * */
            bytes = rotateYUV420Degree90(bytes, WIDTH, HEIGT);

            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(HEIGT, WIDTH,
                        Bitmap.Config.ARGB_8888);
                myCameraPreview.setImageBitmap(mBitmap);
            }

            myCameraPreview.invalidate();
            mProcessInProgress = true;
            mCamera.addCallbackBuffer(bytes);
            // Start our background thread to process images
            new ProcessPreviewDataTask().execute(bytes);

        }
    }

    /* This class is run on another thread in the background, and when it's done with the decoding,
    * onPostExectue is called to set the new pixel array to the image we have.
    * In doInBackground you can change the values of the RGB pixel array to correspond to your
    * preferred colors. */
    private class ProcessPreviewDataTask extends AsyncTask<byte[], Void, Boolean> {

        @Override
        protected Boolean doInBackground(byte[]... datas) {
            byte[] data = datas[0];
            // I use the tempWidth and tempHeight because of the rotation of the image, if your
            // picture is horizontal, use width and height instead.
            int tempWidth = HEIGT;
            int tempHeight = WIDTH;
            // Here we decode the image to a RGB array.
            pixels = decodeYUV420SP(data, tempWidth, tempHeight);
            /*TODO here you're going to change pixel colors.*/
            
            mCamera.addCallbackBuffer(data);
            mProcessInProgress = false;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result){
            myCameraPreview.invalidate();
            mBitmap.setPixels(pixels, 0, HEIGT,0, 0, HEIGT, WIDTH);
            myCameraPreview.setImageBitmap(mBitmap);

            data1.setText(Integer.toHexString(pixels[0]));
        }

        /* Decodes the image from the NV21 format into an RGB-array with integers.
         * Since the NV21 array is made out of bytes, and one pixel is made out of 1.5 bytes, this is
         * quite hard to understand. If you want more information on this you can read about it on
         * */
        public int[] decodeYUV420SP(byte[] yuv, int width, int height) {

            final int frameSize = width * height;

            int rgb[] = new int[width * height];
            final int ii = 0;
            final int ij = 0;
            final int di = +1;
            final int dj = +1;

            int a = 0;
            for (int i = 0, ci = ii; i < height; ++i, ci += di) {
                for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                    int y = (0xff & ((int) yuv[ci * width + cj]));
                    int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                    int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                    y = y < 16 ? 16 : y;

                    int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                    int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                    int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                    r = r < 0 ? 0 : (r > 255 ? 255 : r);
                    g = g < 0 ? 0 : (g > 255 ? 255 : g);
                    b = b < 0 ? 0 : (b > 255 ? 255 : b);

                    rgb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
                }
            }
            return rgb;
        }
    }

    /*Decoding and rotating methods from github
    * This method rotates the NV21 image (standard image that comes from the preview)
    * since this is a byte array, it must be switched correctly to match the pixels*/
    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2)
        {
            for(int y = 0;y < imageHeight/2;y++)
            {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
        return yuv;
    }
}
