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
    private int leftBoarder, rightBoarder;
    private Rect rect;
    private Paint paint;

    private static final int MoveSpeed = 5;

    Player (int x, int y, int size, int direction, int leftBoarder, int rightBoarder) {
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

        if (Math.abs(goalX.get() - x) < 10 || x <= leftBoarder || x >= rightBoarder) {
            speed = 0;
        } else {
            speed = MoveSpeed;
        }
        x += speed * direction;
        rect.set(x, y, x+size, y+size);

    }

    // Set the position we want to move to
    void setGoalPosition(int x) {
        goalX.set(goalX.get() + x);
        if (goalX.get() + x < leftBoarder)
            goalX.set(leftBoarder);

        else if (goalX.get() + x > rightBoarder)
            goalX.set(rightBoarder);
        Log.e(TAG, "player goal position: " + goalX.get());
        Log.e(TAG, "player current position: " + this.x);

        if (x > 0)
            direction = 1;
        else if (x < 0)
            direction = -1;
    }


    void drawPlayer(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

    Rect getBounds() {
        return rect;
    }
}
