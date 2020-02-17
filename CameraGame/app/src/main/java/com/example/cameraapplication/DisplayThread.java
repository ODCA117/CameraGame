package com.example.cameraapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import com.example.cameraapplication.gameLogic.GameEngine;

public class DisplayThread extends Thread {

    private SurfaceHolder holder;
    private Context context;
    volatile private boolean running;
    private GameEngine gameEngine;
    //private Paint backgroundPaint;


    private final long DELAY = 4;

    public DisplayThread(SurfaceHolder holder, Context context, GameEngine gameEngine) {
        this.holder = holder;
        this.context = context;
        this.gameEngine = gameEngine;
        running = true;
    }

    @Override
    public void run() {

        while (running) {
            gameEngine.update();
            Canvas canvas = holder.lockCanvas(null);

            if(canvas != null) {

                synchronized (holder) {
                    canvas.drawColor(Color.WHITE);

                    //TODO: Draw game
                    gameEngine.draw(canvas);
                }
                holder.unlockCanvasAndPost(canvas);
            }

            try {
                //TODO:Change to dynamic sleep
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                //TODO: Log
            }
            try {
                this.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
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
