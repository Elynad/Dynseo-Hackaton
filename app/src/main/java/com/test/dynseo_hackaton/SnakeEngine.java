package com.test.dynseo_hackaton;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Random;


public class SnakeEngine extends SurfaceView implements Runnable {

    // -- PROPERTIES
    private final static String TAG = "SnakeEngine" ;

    // Game thread for the main game loop
    private Thread thread = null ;

    private Context context ;

    int level ;

    private Random random ;

    // Sound effects handle
    private MediaPlayer eatPreyMediaPlayer ;
    private MediaPlayer crashMediaPlayer ;


    // Track movement heading
    public enum Heading {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    // Movement to keep ; start to the right
    private Heading heading = Heading.RIGHT ;

    // The size in pixels of a snake segment
    private int blockSize ;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 20 ;
    private int numBlocksHigh ;

    // Control pausing between updates
    private long nextFrameTime ;

    // Update the game 10 times per second
    private long fps = 7 ;
    private final long MILLIS_PER_SECOND = 1000 ; // TODO : Might be useless

    // Number of points
    private int score ;

    /* Location in the grid of all segments of the snake */
    // snakeXs will hold the horizontal coordinates of each segment of the snake
    private int[] snakeXs ;
    // snakeYs will hold the vertical coordinates of each segment of the snake.
    private int[] snakeYs ;
    // Screen size
    private int screenX ;
    private int screenY ;
    // Snake size
    private int snakeLength ;

    /* Obstacles */
    private ArrayList<Integer> obstacleXs ;
    private ArrayList<Integer> obstacleYs ;
    // Obstacle count
    private int obstaclesCount ;

    /* Preys */
    private ArrayList<Drawable> preys ;
    private int preysDrawableIndex ;
    // Prey position
    private int preyX ;
    private int preyY ;

    /* DRAWING */
    // Is the game playing ?
    private volatile boolean isPlaying ;
    // Canvas for our paint
    private Canvas canvas ;
    // Required to use canvas
    private SurfaceHolder surfaceHolder ;
    // Some paint for our canvas
    private Paint paint ;


    // -- INIT
    public SnakeEngine(Context context, Point size, SurfaceView gameSurfaceView, int level) {
        super(context) ;

        this.context = context ;
        this.level = level ;

        random = new Random() ;

        screenX = size.x ;
        screenY = size.y;

        // Work out how many pixels each block is
        blockSize = screenX / NUM_BLOCKS_WIDE ;
        // How many blocks of the same will fit into the height
        numBlocksHigh = screenY / blockSize ;

        // Set the sound up
        eatPreyMediaPlayer = MediaPlayer.create(context, R.raw.hiss);
        crashMediaPlayer = MediaPlayer.create(context, R.raw.roblox_death);

        // Initialize the drawing objects
        surfaceHolder = gameSurfaceView.getHolder();
        //surfaceHolder = getHolder();
        paint = new Paint();

        // If you score 200 you are rewarded with a crash achievement
        snakeXs = new int[200] ;
        snakeYs = new int[200] ;

        // Initialize obstacles
        obstacleXs = new ArrayList<>() ;
        obstacleYs = new ArrayList<>() ;

        preys = new ArrayList<>() ;
        preys.add(ResourcesCompat.getDrawable(getResources(), R.drawable.prey_1, null));
        preys.add(ResourcesCompat.getDrawable(getResources(), R.drawable.prey_2, null));
        preys.add(ResourcesCompat.getDrawable(getResources(), R.drawable.prey_3, null));

        // Start the game
        newGame();
    }


    // -- METHODS
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
        snakeLength = 3 ;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2 ;
        snakeYs[0] = numBlocksHigh / 2 ;

        // Get the prey ready for dinner
        spawnPrey() ;

        // Reset the score
        score = 0;
        ((MainActivity)context).setScore(score);

        // Reset the game speed
        fps = context.getResources().getIntArray(R.array.game_base_speed)[level];

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis() ;
        obstacleXs.clear();
        obstacleYs.clear();
        obstaclesCount = 0;
    }

    /**
     *  Use two random int values : The first between 0 and [NUM_BLOCKS_WIDE] range ; the second
     *  between 0 and [numBlocksHigh].
     *  These two random values are used to setup the horizontal and vertical location of the prey.
     *  TODO :  Optimization : Instancing a new instance of [Random] is slow and could be done in
     *  TODO :      constructor.
     */
    public void spawnPrey() {
        preyX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        preyY = random.nextInt(numBlocksHigh - 1) + 1 ;

        for (int i = 0 ; i < snakeLength ; i++) {
            if (snakeXs[i] == preyX && snakeYs[i] == preyY)
                spawnPrey();
        }
        preysDrawableIndex = random.nextInt(preys.size());
    }

    /**
     *  Use two random int values to create random location for obstacles.
     */
    public void spawnObstacle() {
        // The number of obstacles is the score divided by 10.
        obstaclesCount = score / 10;

        int i = obstacleXs.size() - 1;
        if (i < 0)
            i = 0;
        Log.d(TAG, "obstaclesXs size = " + obstacleXs.size() + " ; obstaclesCount = " + obstaclesCount);
        while (i < obstaclesCount) {
            Log.d(TAG, "Trying to insert new element at index " + i) ;
            obstacleXs.add(random.nextInt(NUM_BLOCKS_WIDE - 1) + 1);
            obstacleYs.add(random.nextInt(numBlocksHigh - 1) + 1);
            // If the obstacle is on the same point than the prey, decrease i to generate new
            // random points.
            if (obstacleXs.get(i) == preyX && obstacleYs.get(i) == preyY)
                i-- ;
            i++;
        }
    }

    /**
     *  The snake's length is increased by one block, a new prey is spawned, 1 is added to the score
     *  and a sound effect is played.
     */
    private void eatPrey() {
        snakeLength++ ;
        spawnPrey();
        if (level > 1)
            spawnObstacle();
        score += 1 ;
        ((MainActivity) context).setScore(score);
        eatPreyMediaPlayer.start();
        if (score % 10 == 0)
            fps++ ;
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

        // Hit an obstacle
        for (int i = 0 ; i < obstacleXs.size() ; i++) {
            if (snakeXs[0] == obstacleXs.get(i) && snakeYs[0] == obstacleYs.get(i)) {
                isDead = true;
                break ;
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
            crashMediaPlayer.start();
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
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

            // Draw the head of the snake, given the direction
            int headDrawableId = 0 ;
            switch (heading) {
                case UP:
                    headDrawableId = R.drawable.snake_head_up ;
                    break ;
                case RIGHT:
                    headDrawableId = R.drawable.snake_head_right ;
                    break ;
                case DOWN:
                    headDrawableId = R.drawable.snake_head_down ;
                    break ;
                case LEFT:
                    headDrawableId = R.drawable.snake_head_left ;
                    break ;
            }
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), headDrawableId, null) ;
            if (drawable != null) {
                drawable.setBounds(
                        snakeXs[0] * blockSize,
                        snakeYs[0] * blockSize,
                        (snakeXs[0] * blockSize) + blockSize,
                        (snakeYs[0] * blockSize) + blockSize);
                drawable.draw(canvas);
            }

            // Draw the snake body one block at a time
            int j = 1 ;
            while (j < snakeLength) {
                drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.snake_body, null);
                if (drawable != null) {
                    drawable.setBounds(
                            snakeXs[j] * blockSize,
                            snakeYs[j] * blockSize,
                            (snakeXs[j] * blockSize) + blockSize,
                            (snakeYs[j] * blockSize) + blockSize);
                    drawable.draw(canvas);
                }
                j++;
            }

            // Draw the prey
            drawable = preys.get(preysDrawableIndex);
            drawable.setBounds(
                    preyX * blockSize,
                    preyY * blockSize,
                    (preyX * blockSize) + blockSize,
                    (preyY * blockSize) + blockSize
            );
            drawable.draw(canvas);

            // Draw the obstacles
            drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.wall,
                    null);
            for (int i = 0 ; i < obstacleXs.size() ; i++) {
                // TODO : Draw our custom sprite instead
                if (drawable != null) {
                    drawable.setBounds(
                            obstacleXs.get(i) * blockSize,
                            obstacleYs.get(i) * blockSize,
                            (obstacleXs.get(i) * blockSize) + blockSize,
                            (obstacleYs.get(i) * blockSize) + blockSize
                    );
                    drawable.draw(canvas);
                }
            }

            Rect r = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
            // Draw border of surface view
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(15);
            paint.setColor(Color.BLACK);
            canvas.drawRect(r, paint);

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
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / fps;

            // Returns true so [update()] & [draw()] functions are returned
            return true ;
        }

        return false ;
    }

    public void setHeading(int id) {
        switch (id) {
            case R.id.up_button:
                if (heading != Heading.DOWN)
                    heading = Heading.UP ;
                break;
            case R.id.down_button:
                if (heading != Heading.UP)
                    heading = Heading.DOWN ;
                break;
            case R.id.right_button:
                if (heading != Heading.LEFT)
                    heading = Heading.RIGHT ;
                break;
            case R.id.left_button:
                if (heading != Heading.RIGHT)
                    heading = Heading.LEFT ;
                break;
            default:
                break;
        }
    }

}
