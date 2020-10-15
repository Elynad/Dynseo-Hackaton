package com.test.dynseo_hackaton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // -- PROPERTIES
    // UI
    private View gameView;

    // D-PAD BUTTONS
    private AppCompatButton upButton;
    private AppCompatButton downButton;
    private AppCompatButton leftButton;
    private AppCompatButton rightButton;

    SnakeEngine snakeEngine ;

    // -- VIEW LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init game layout
        setContentView(R.layout.activity_main);

        // Init Game View
        gameView = findViewById(R.id.gameView);

        // Init D-Pad buttons
        upButton = findViewById(R.id.up_button);
        downButton = findViewById(R.id.down_button);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);

        // Get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the SnakeEngine class
        snakeEngine = new SnakeEngine(this, size);

        // Make snakeEngine the view of the Activity
        setContentView(snakeEngine);

    }


    // -- METHODS
    public void dPadButtonsOnClick(View v) {
        switch (v.getId()) {
            case R.id.up_button:
                snakeEngine.setHeading(SnakeEngine.Heading.UP);
                break;
            case R.id.down_button:
                snakeEngine.setHeading(SnakeEngine.Heading.DOWN);
                break;
            case R.id.right_button:
                snakeEngine.setHeading(SnakeEngine.Heading.RIGHT);
                break;
            case R.id.left_button:
                snakeEngine.setHeading(SnakeEngine.Heading.LEFT);
                break;
            default:
                break;
        }
    }

    // Start the thread in snakeEngine
    @Override
    protected void onResume() {
        super.onResume();
        snakeEngine.resume();
    }

    // Stop the thread in snakeEngine
    @Override
    protected void onPause() {
        super.onPause();
        snakeEngine.pause();
    }
}