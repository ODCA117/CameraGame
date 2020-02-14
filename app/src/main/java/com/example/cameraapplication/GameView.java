package com.example.cameraapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.example.cameraapplication.gameLogic.GameEngine;

import static android.content.ContentValues.TAG;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private DisplayThread displayThread;
    private GameEngine gameEngine;
    private SurfaceHolder holder;


    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
    }

    public void init(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        displayThread = new DisplayThread(holder, getContext(), gameEngine);
        Log.d(TAG, "Surfaced size: " + this.getWidth() + ", " + this.getHeight() );

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if(!displayThread.isRunning()) {
            displayThread.start();
        } else {
            displayThread.start();
        }

        gameEngine.initGame(getHeight(), getWidth());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surfaced Changed:");


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        displayThread.stopRunning();
        boolean retry = true;

        while (retry) {
            try {
                displayThread.join();
            } catch (Exception e) {
                //TODO: Catch exception
            }
        }
        //TODO: stop thread here
        displayThread = null;
    }
}
