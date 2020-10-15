package com.test.dynseo_hackaton;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class SnakeEngine extends SurfaceView implements Runnable {

    private final static String TAG = "SnakeEngine" ;

    // Game thread for the main game loop
    private Thread thread = null ;

    private Context context ;

    // Sound effects handle
    private SoundPool soundPool ;
    private int eatPrey = -1 ;
    private int snakeCrash = -1 ;

    // Track movement heading
    public enum Heading {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    // Movement to keep ; start to the right
    private Heading heading = Heading.RIGHT ;

    // Screen size
    private int screenX ;
    private int screenY ;

    // Snake size
    private int snakeLength ;

    // Prey position
    private int preyX ;
    private int preyY ;

    // The size in pixels of a snake segment
    private int blockSize ;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40 ;
    private int numBlocksHigh ;

    // Control pausing between updates
    private long nextFrameTime ;
    // Update the game 10 times per second
    private final long FPS = 10 ;
    private final long MILLIS_PER_SECOND = 1000 ; // TODO : Might be useless

    // Number of points
    private int score ;

    /* Location in the grid of all segments */

    // snakeXs will hold the horizontal coordinates of each segment of the snake
    private int[] snakeXs ;

    // snakeYs will hold the vertical coordinates of each segment of the snake.
    private int[] snakeYs ;

    /* DRAWING */

    // Is the game playing ?
    private volatile boolean isPlaying ;

    // Canvas for our paint
    private Canvas canvas ;

    // Required to use canvas
    private SurfaceHolder surfaceHolder ;

    // Some paint for our canvas
    private Paint paint ;

    public SnakeEngine(Context context, Point size) {
        super(context) ;

        this.context = context ;

        screenX = size.x ;
        screenY = size.y;

        // Work out how many pixels each block is
        blockSize = screenX / NUM_BLOCKS_WIDE ;
        // How many blocks of the same will fit into the height
        numBlocksHigh = screenY / blockSize ;

        // Set the sound up
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor assetFileDescriptor ;

            // Prepare the two sounds in memory
            assetFileDescriptor = assetManager.openFd(""); // TODO : Add a sound
            eatPrey = soundPool.load(assetFileDescriptor, 0) ;

            assetFileDescriptor = assetManager.openFd("") ; // TODO : Add a sound
            snakeCrash = soundPool.load(assetFileDescriptor, 0);
        } catch (IOException exception) {
            Log.e(TAG, "EXCEPTION : " + exception.toString());
        }

        // Initialize the drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        // If you score 200 you are rewarded with a crash achievement
        snakeXs = new int[200] ;
        snakeYs = new int[200] ;

        // Start the game
        newGame();
    }

    @Override
    public void run() {

        while (isPlaying) {
            // Update 10 times a second
            if (updateRequired()) {
                update() ;
                draw();
            }
        }
    }

    /**
     *  Called by the game Activity when Android or the player causes the app to call onPause. The
     *  pause method will make the [Thread] instance stop.
     */
    public void pause() {
        isPlaying = false ;
        try {
            thread.join();
        } catch (InterruptedException exception) {
            Log.e(TAG, "EXCEPTION " + exception.toString());
        }
    }

    /**
     *  Called by the game Activity when Android or the player causes the app to call onResume. The
     *  resume method will create a new instance of [Thread] and launch it.
     */
    public void resume() {
        isPlaying = true ;
        thread = new Thread(this);
        thread.start();
    }

    /**
     *  Prepare the snake. The length is set up to just one block, and the head of the snake
     *  (represented by the first cell of [snakeXs] and [snakeYs]) is set to the center of the
     *  screen. We will only use the head for collision detection.
     *  Then, the prey is prepared for his terrible destiny with [spawnBob()] ; and the score is
     *  initialized to 0.
     *  Finally, the [nextFrameTime] is set to the current time. This will cause the [update] and
     *  [draw] methods run.
     */
    public void newGame() {

        // Start with a single snake segment
        snakeLength = 1 ;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2 ;
        snakeYs[0] = numBlocksHigh / 2 ;

        // Get the prey ready for dinner
        spawnPrey() ;

        // Reset the score
        score = 0 ;

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis() ;
    }

    /**
     *  Use two random int values : The first between 0 and [NUM_BLOCKS_WIDE] range ; the second
     *  between 0 and [numBlocksHigh].
     *  These two random values are used to setup the horizontal and vertical location of the prey.
     *  TODO :  Optimization : Instancing a new instance of [Random] is slow and could be done in
     *  TODO :      constructor.
     */
    public void spawnPrey() {
        Random random = new Random() ;
        preyX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        preyY = random.nextInt(numBlocksHigh - 1) + 1 ;
    }

    /**
     *  The snake's length is increased by one block, a new prey is spawned, 1 is added to the score
     *  and a sound effect is played.
     */
    private void eatPrey() {
        snakeLength++ ;
        spawnPrey();
        score += 1 ;
        soundPool.play(eatPrey, 1, 1, 0, 0, 1);
    }

    /**
     *  The for loop starts at the last block of the snake in [snakeXs] and [snakeYs] and advance it
     *  into the location previously occupied by the block ahead of it.
     *  To move the head we switch on the current value of [heading] and add or substract 1 from
     *  either the heads vertical of horizontal position.
     */
    private void moveSnake() {
        // Move the body
        for (int i = snakeLength ; i > 0 ; i--) {
            // Start at the back and move it to the position of the segment in front of it
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];
            // The head is excluded because it has nothing in front of it, and it is never a good
            // idea to try to access the [-1] position of an array.
        }

        // Move the head in the appropriate heading
        switch (heading) {
            case UP:
                snakeYs[0]-- ;
                break ;

            case RIGHT:
                snakeXs[0]++ ;
                break ;

            case DOWN:
                snakeYs[0]++ ;
                break ;

            case LEFT:
                snakeXs[0]-- ;
                break ;
        }
    }

    /**
     *  Handle collision detection. Check if the snake head bumped into the edge of the screen, and
     *  check if the snake head bumped into the snake's body.
     *  @return boolean - True if the snake is dead.
     */
    private boolean detectDeath() {
        boolean isDead = false ;

        // Hit the screen edges
        if (snakeXs[0] == -1 || snakeXs[0] >= NUM_BLOCKS_WIDE
                || snakeYs[0] == -1 || snakeYs[0] == numBlocksHigh)
            isDead = true ;

        // Hit its own body
        for (int i = snakeLength - 1 ; i > 0 ; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                isDead = true;
                break;
            }
        }

        return isDead ;
    }

    /**
     *  Checks if the head has touched the prey. If it does, then [eatPrey()] method is call.
     *  Calls [moveSnake()] method.
     *  Calls [detectDeath()] method, if it returns [true] a sound is played and the game begins
     *  again.
     */
    public void update() {
        // Did the head touched the prey ?
        if (snakeXs[0] == preyX && snakeYs[0] == preyY)
            eatPrey();

        moveSnake();

        if (detectDeath()) {
            soundPool.play(snakeCrash, 1, 1, 0, 0, 1);

            newGame();  // TODO : End the game ?
        }
    }

    /**
     *  First, we lock the surface as required by Android. If it works, we clear the screen with
     *  [drawColor()] and change the color of all future objects, for the snake and for the prey.
     *  We use a for loop to draw a block to represent each block of the snake. The code positions
     *  the blocks to screen coordinates by using their grid position (contained in the array)
     *  multiplied by [blockSize] (which was determined in the constructor based on screen
     *  resolution).
     *  Then, draw a single block to represent the prey.
     */
    public void draw() {
        // Get a lock on the canvas
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // Fill the screen with color
            canvas.drawColor(Color.argb(255, 26, 128, 182)); // TODO : Set correct colors

            // Set the color of the paint to draw the snake
            paint.setColor(Color.argb(255, 255, 255, 255)); // TODO : Set correct color

            // Scale the HUD text
            paint.setTextSize(90) ;
            canvas.drawText("Score : " + score, 10, 70, paint); // TODO : Might be useless, use Stim'Art score display

            // Draw the snake one block at a time
            for (int i = 0 ; i < snakeLength ; i++) {
                // TODO : Draw our custom sprite instead
                canvas.drawRect(
                        snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize,
                        paint);
            }

            // Set the color of the paint to draw the prey
            paint.setColor(Color.argb(255, 255, 0, 0)); // TODO : Set correct colors

            // Draw the prey
            canvas.drawRect(
                    preyX * blockSize,
                    (preyY * blockSize),
                    (preyX * blockSize) + blockSize,
                    (preyY * blockSize) + blockSize,
                    paint
            );

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     *  Checks if [nextFrameTime] variable has been exceeded by the actual current time. If it does,
     *  then a new time is retrieved and put back in [nextFrameTime].
     *  @return boolean - True if we need to execute draw and update.
     */
    public boolean updateRequired() {
        // Are we due to update the frame ?
        if (nextFrameTime <= System.currentTimeMillis()) {
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / FPS ;

            // Returns true so [update()] & [draw()] functions are returned
            return true ;
        }

        return false ;
    }

    public void setHeading(Heading heading) {
        this.heading = heading ;
    }
}
