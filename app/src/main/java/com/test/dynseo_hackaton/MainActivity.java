package com.test.dynseo_hackaton;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // -- PROPERTIES
    // UI
    private View gameView;

    // D-PAD BUTTONS
    private Button upButton;
    private Button downButton;
    private Button leftButton;
    private Button rightButton;


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

    }


    // -- METHODS
    public void dPadButtonsOnClick(View v) {
        switch (v.getId()) {
            case upButton:
                //heading = Heading.UP;
                break;
            case downButton:
                //doSomething3();
                break;
            case rightButton:
                //doSomething4();
                break;
            case leftButton:
                //doSomething5();
                break;
            default:
                break;
        }
    }


}