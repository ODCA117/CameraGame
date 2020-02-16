package com.example.cameraapplication.gameLogic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Obstacle {

    private int x,y, sizeX, sizeY;
    private int speed;
    private Rect rect;
    private Paint paint;

    Obstacle (int x, int y, int sizeX, int sizeY, int speed) {
        this.x = x;
        this.y = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.speed = speed;
        rect = new Rect(x, y, x + sizeX, y + sizeY);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
    }

    void updatePosition() {
        y += speed;
        rect.set(x, y, x + sizeX, y + sizeY);
    }

    void drawObstacle(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

    int getY() {
        return y;
    }

    Rect getBounds() {
        return rect;
    }
}
