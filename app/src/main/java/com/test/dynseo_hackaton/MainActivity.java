package com.test.dynseo_hackaton;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // -- PROPERTIES
    // Declare an instance of SnakeEngine
    private SnakeEngine snakeEngine;

    // UI
    private SurfaceView gameSurfaceView;

    // D-PAD BUTTONS
    private AppCompatButton upButton;
    private AppCompatButton downButton;
    private AppCompatButton leftButton;
    private AppCompatButton rightButton;


    // -- VIEW LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init game layout
        setContentView(R.layout.activity_main);

        // Init Game View
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        gameSurfaceView = findViewById(R.id.gameSurfaceView);
        gameSurfaceView.getLayoutParams().width = (int)(width * 0.6) ;
        gameSurfaceView.getLayoutParams().height = (int)(height * 0.8) ;
        gameSurfaceView.setZOrderOnTop(true);

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
        size.x = gameSurfaceView.getLayoutParams().width;
        size.y = gameSurfaceView.getLayoutParams().height;

        // Create a new instance of the SnakeEngine class
        snakeEngine = new SnakeEngine(this, size, gameSurfaceView);

        // Make snakeEngine the view of the Activity
        //setContentView(snakeEngine);

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


    // -- METHODS
    @Override
    public void onClick(View view) {
        snakeEngine.setHeading(view.getId());
    }

}