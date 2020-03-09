package com.example.cameraapplication.gameLogic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;



public class Player {
    private int x,y, size;
    private AtomicInteger goalX;
    private int speed, direction;
    private int leftBoarder, rightBoarder;
    private Rect rect;
    private Paint paint;

    private static final String TAG = "Player";
    private static final int MoveSpeed = 10;

    Player (int x, int y, int size, int direction, int leftBoarder, int rightBoarder) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = MoveSpeed;
        this.direction = direction;
        this.leftBoarder = leftBoarder;
        this.rightBoarder = rightBoarder;
        rect = new Rect(x, y, x+size, y+size);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        goalX = new AtomicInteger(0);
    }

    public void initPlayer(int x, int goal) {
        this.x = x;
        goalX.set(goal);
        if(x < goal) {
            direction = 1;
        } else {
            direction = -1;
        }
    }

    /**
     * updates position, May need to add delta time for dynamic update
     */
    void updatePosition() {

        //Log.e(TAG, "goalX: " + goalX.get() + ", CurrentPosition: " + x  + ", speed: " + speed + ", direction: " + direction);

        if(x < goalX.get()) {
            direction = 1;
        } else {
            direction = -1;
        }

        if (Math.abs(goalX.get() - x) < 6) {
            speed = 0;
        } else {
            speed = MoveSpeed;
        }

        if(x + speed * direction < leftBoarder) {
            x = leftBoarder;
            //Log.e(TAG, "less Than Left boarder");
        }

        else if (x + speed * direction > rightBoarder) {
            x = rightBoarder;
            //Log.e(TAG, "greater Than right boarder");
        }

        else {
            x += speed * direction;
        }

        //Log.e(TAG, "After: goalX: " + goalX.get() + ", CurrentPosition: " + x  + ", speed: " + speed + ", direction: " + direction);

        rect.set(x, y, x+size, y+size);

    }

    // Set the position we want to move to
    void moveGoalPosition(int x) {
        //Log.e(TAG, "deltaX: " + x );

        if (goalX.get() + x < leftBoarder)
            goalX.set(leftBoarder);

        else if (goalX.get() + x > rightBoarder)
            goalX.set(rightBoarder);

        else
            goalX.set(goalX.get() + x);
        //Log.e(TAG, "player goal position: " + goalX.get());
        //Log.e(TAG, "player current position: " + this.x);
    }

    void updateGoalPosition(int x) {
        if ( x < leftBoarder)
            goalX.set(leftBoarder);

        else if ( x > rightBoarder)
            goalX.set(rightBoarder);

        else
            goalX.set(x);
    }


    void drawPlayer(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

    Rect getBounds() {
        return rect;
    }
}
