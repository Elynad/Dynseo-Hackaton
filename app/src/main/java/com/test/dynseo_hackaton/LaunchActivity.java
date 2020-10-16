package com.test.dynseo_hackaton;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

    Button  easyButton ;
    Button  mediumButton ;
    Button  hardButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        easyButton = findViewById(R.id.easyButton);
        mediumButton = findViewById(R.id.mediumButton);
        hardButton = findViewById(R.id.hardButton);

    }

    @Override
    public void onClick(View view) {
        int gameLevel = -1 ;
        switch (view.getId()) {
            case R.id.easyButton :
                gameLevel = 0 ;
                break ;
            case R.id.mediumButton :
                gameLevel = 1 ;
                break ;
            case R.id.hardButton :
                gameLevel = 2 ;
                break ;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.level_param), gameLevel);
        startActivity(intent);
    }
}