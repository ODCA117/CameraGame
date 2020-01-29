package com.example.cameraapplication.gameLogic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Player {
    private int x,y, size;
    private int speed, direction;
    private Rect rect;
    private Paint paint;

    Player (int x, int y, int size, int speed, int direction) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.direction = direction;
        rect = new Rect(x, y, x+size, y+size);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
    }

    /**
     * updates position, May need to add delta time for dynamic update
     */
    void updatePosition() {
        x += speed * direction;
        rect.set(x, y, x+size, y+size);
    }

    void drawPlayer(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

    void changeDirection() {
        direction *= -1;
    }

    int getX() {
       return x;
    }

    Rect getBounds() {
        return rect;
    }
}
