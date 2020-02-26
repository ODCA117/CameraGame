package com.example.cameraapplication.gameLogic;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.example.cameraapplication.ImageProcessor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine implements PropertyChangeListener {

    private int width;
    private int height;
    private Player player;
    private List<Obstacle> obstacles;
    private boolean gameOn;
    private long timeToSpawn;
    private Random random;
    private ImageProcessor processor;

    private static final String TAG = "GameEngine";

    //private int goalPosition;

    public GameEngine(ImageProcessor processor) {
        this.processor = processor;
        gameOn = false;
    }

    // update game
    public void update() {
        if(!gameOn)
            return;

        updatePlayerPosition();
        updateObstaclePosition();
        checkCollision();
        spawnObstacles();
    }

    // draw every object on the canvas
    public void draw(Canvas canvas) {
        if(!gameOn)
            return;
        Log.d(TAG, "Draw game");

        player.drawPlayer(canvas);

        for (Obstacle o : obstacles) {
            o.drawObstacle(canvas);
        }
    }

    //set up the game boarders
    public void initGame(int height, int width) {
        this.height = height;
        this.width = width;
    }

    // Start the game
    public void restart() {
        // Create a player at (x,y) = (20, heigth - 120) (lower part is 20 pixels from bottom of the gameview)
        // player size 100*100, movement direction = positive,
        // player restricted to move between 20 from left and right boarder
        player = new Player(20, height - 120, 100, 1, 20, width - 120);
        player.initPlayer(20, 500);

        // create a list of obstacles
        obstacles = new ArrayList<>();
        random = new Random(0);

        //create a spawn rate of 1 obstacle every 2 seconds
        timeToSpawn = System.currentTimeMillis() + 2000;

        //Add this as listener
        processor.addPropertyChangeListener(this);

        // start game
        gameOn = true;
    }

    public boolean running() {
        return gameOn;
    }

    // check if any collision between player and obstacles
    private void checkCollision() {
        Rect playerRect = player.getBounds();

        // loop over all obstacles
        for(Obstacle o : obstacles) {
            if(Rect.intersects(playerRect, o.getBounds())) {
                Log.d(TAG, "Game Over");
                gameOn = false;
                processor.removePropertyChangeListener(this);
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

        //spawn new obstacle if 2 seconds since last spawn
        if(timeToSpawn < System.currentTimeMillis()) {
            int x = 20 + random.nextInt(width - 40); // find a position in horizontal axis
            Obstacle o = new Obstacle(x, -120, 100, 100, 15);
            obstacles.add(o);
            timeToSpawn = System.currentTimeMillis() + 2000; // calculate when next spawn should happen
        }
    }

    // Called from Image processor and will update the position the player should move to
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!gameOn){
            Log.e(TAG, "Game off");
            return;
        }
        Log.e(TAG, "Game on");
        int newDelta = (int) evt.getNewValue();

        player.moveGoalPosition(newDelta);

        Log.e(TAG, "player Move: " + newDelta);
    }
}
