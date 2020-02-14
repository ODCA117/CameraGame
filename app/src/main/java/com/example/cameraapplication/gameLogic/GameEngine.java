package com.example.cameraapplication.gameLogic;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.nfc.Tag;
import android.util.Log;

import com.example.cameraapplication.CameraPreview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class GameEngine implements PropertyChangeListener {

    private int width;
    private int height;
    private Player player;
    private List<Obstacle> obstacles;
    private boolean gameOn;
    private long timeToSpawn;
    private Random random;

    private int goalPosition;
    private int counter;

    public GameEngine(CameraPreview cameraPreview) {
        cameraPreview.addPropertyChangeListener(this);
        goalPosition = 500;
        counter = 0;
        gameOn = false;
    }

    public void update() {
        if(!gameOn)
            return;

        //Log.d(TAG, "Update game");
        updatePlayerPosition();
        updateObstaclePosition();
        checkCollision();
        spawnObstacles();
    }

    public void draw(Canvas canvas) {

        //Log.e(TAG, "width: " + canvas.getWidth());
        if(!gameOn)
            return;
        Log.d(TAG, "Draw game");
        player.drawPlayer(canvas);

        for (Obstacle o : obstacles) {
            o.drawObstacle(canvas);
        }
    }

    public void initGame(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public void restart() {
        player = new Player(20, height - 120, 100, 1);
        obstacles = new ArrayList<>();
        random = new Random(0);
        timeToSpawn = System.currentTimeMillis() + 2000;
        gameOn = true;
    }

    public boolean running() {
        return gameOn;
    }

    private void checkCollision() {
        Rect playerRect = player.getBounds();

        for(Obstacle o : obstacles) {
            if(Rect.intersects(playerRect, o.getBounds())) {
                Log.d(TAG, "Game Over");
                gameOn = false;
            }
        }
    }

    private void updatePlayerPosition() {
        player.updatePosition();
    }

    private void updateObstaclePosition() {
        int y;
        List<Obstacle> removeThese = new ArrayList<>();
        for (Obstacle o : obstacles) {
            y = o.getY();
            if (y > height + 50)
                removeThese.add(o);

            o.updatePosition();
        }

        obstacles.removeAll(removeThese);
    }

    private void spawnObstacles() {

        //spawn new obstacle
        if(timeToSpawn < System.currentTimeMillis()) {
            int x = 20 + random.nextInt(width - 40);
            Obstacle o = new Obstacle(x, -120, 100, 100, 15);
            obstacles.add(o);
            timeToSpawn = System.currentTimeMillis() + 2000;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!gameOn)
            return;

        //Log.d(TAG, "player goal position: " + (int) evt.getNewValue());

        player.setGoalPosition((int)evt.getNewValue());
    }
}
