package com.test.dynseo_hackaton;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.graphics.PixelFormat;
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
    private AppCompatTextView scoreTextView;


    // -- VIEW LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init game layout
        setContentView(R.layout.activity_main);

        // Init Game Surface View
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        SurfaceView gameSurfaceView = findViewById(R.id.game_surface_view);
        gameSurfaceView.getLayoutParams().width = (int)(width * 0.6) ;
        gameSurfaceView.getLayoutParams().height = (int)(height * 0.8) ;
        gameSurfaceView.setZOrderOnTop(true);
        gameSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        // Init Score text view
        scoreTextView = findViewById(R.id.score_text_view);

        // Get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);
        size.x = gameSurfaceView.getLayoutParams().width;
        size.y = gameSurfaceView.getLayoutParams().height;

        // Create a new instance of the SnakeEngine class
        snakeEngine = new SnakeEngine(this, size, gameSurfaceView);

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

    public void setScore(final int score) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTextView.setText(R.string.score + score) ;
            }
        });
    }

}