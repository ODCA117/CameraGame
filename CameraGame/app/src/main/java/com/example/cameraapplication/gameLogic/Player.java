package com.example.cameraapplication.gameLogic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Player {
    private int x,y, size;
    private AtomicInteger goalX;
    private int speed, direction;
    private Rect rect;
    private Paint paint;

    private static final int MoveSpeed = 5;

    Player (int x, int y, int size, int direction) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = MoveSpeed;
        this.direction = direction;
        rect = new Rect(x, y, x+size, y+size);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        goalX = new AtomicInteger(100);
    }

    /**
     * updates position, May need to add delta time for dynamic update
     */
    void updatePosition() {

        Log.d(TAG, "abs(goalX - x) = " + Math.abs(goalX.get() - x));

        if (Math.abs(goalX.get() - x) < 10) {
            speed = 0;
        } else {
            speed = MoveSpeed;
        }
        x += speed * direction;
        rect.set(x, y, x+size, y+size);

    }

    void setGoalPosition(int x) {
        goalX.set(goalX.get() + x);
        Log.d(TAG, "player goal position: " + x);
        Log.d(TAG, "player current position: " + this.x);

        if(this.x < goalX.get()) {
            direction = 1;
        } else if (this.x > goalX.get())
            direction = -1;
    }

    void drawPlayer(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

    void changeDirection(int dir) {
        direction = dir;
    }

    int getX() {
       return x;
    }

    Rect getBounds() {
        return rect;
    }
}
