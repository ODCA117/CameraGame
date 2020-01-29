package com.example.cameraapplication.gameLogic;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class GameEngine {

    private int width;
    private int height;
    private Player player;
    private List<Obstacle> obstacles;
    private boolean gameOn;
    private long timeToSpawn;
    private Random random;

    public GameEngine() {

    }

    public boolean update() {
        //TODO: Game updates
        Log.d(TAG, "Update game");
        updatePlayerPosition();
        updateObstaclePosition();
        checkCollision();
        spawnObstacles();
        return gameOn;
    }

    public void draw(Canvas canvas) {
        //TODO: Draw game here
        Log.d(TAG, "Draw game");
        player.drawPlayer(canvas);

        for (Obstacle o : obstacles) {
            o.drawObstacle(canvas);
        }
    }

    public void initGame(int height, int width) {
        this.height = height;
        this.width = width;
        player = new Player(20, height - 120, 100, 5, 1);
        obstacles = new ArrayList<>();
        random = new Random(0);
        timeToSpawn = System.currentTimeMillis() + 2000;
        gameOn = true;
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
        int x = player.getX();
        if (x > width - 120)
            player.changeDirection();
        if(x < 20)
            player.changeDirection();

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
            Obstacle o = new Obstacle(x, -120, 100, 100, 5);
            obstacles.add(o);
            timeToSpawn = System.currentTimeMillis() + 2000;
        }
    }
}
