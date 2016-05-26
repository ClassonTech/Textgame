package net.classon.www.textadventureplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    public static final String PREFS_NAME = "MyPrefsFile";
    MediaPlayer myMediaPlayer;

    private Button startButton;
    private Button clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this.getApplicationContext(), MainGameActivity.class);
                startActivity(intent);
            }
        });

        clearButton = (Button)findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear();
                editor.commit();
            }
        });

    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();
        }
        catch(IllegalStateException e){

        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        myMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.title);
        myMediaPlayer.setLooping(true);
        myMediaPlayer.start();
    }

}
