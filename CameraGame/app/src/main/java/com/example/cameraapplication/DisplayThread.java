package com.example.cameraapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import com.example.cameraapplication.gameLogic.GameEngine;

// Thread that handles all drawing on gameView
public class DisplayThread extends Thread {

    private SurfaceHolder holder;
    private Context context;
    volatile private boolean running;
    private GameEngine gameEngine;
    //private Paint backgroundPaint;


    private final long DELAY = 20;

    public DisplayThread(SurfaceHolder holder, Context context, GameEngine gameEngine) {
        this.holder = holder;
        this.context = context;
        this.gameEngine = gameEngine;
        running = true;
    }

    @Override
    public void run() {

        //run as long as we are in game view
        while (running) {
            // update game logic
            gameEngine.update();
            //lock the canvas that we want to draw to
            Canvas canvas = holder.lockCanvas(null);

            if(canvas != null) {
                synchronized (holder) {
                    //draw a white background on the screen
                    canvas.drawColor(Color.WHITE);

                    // draw the game
                    gameEngine.draw(canvas);
                }

                holder.unlockCanvasAndPost(canvas);
            }


            // sleep this thread for DELAY milliseconds
            try {
                //TODO:Change to dynamic sleep
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                //TODO: Log
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stopRunning() {
        running = false;
    }
}
