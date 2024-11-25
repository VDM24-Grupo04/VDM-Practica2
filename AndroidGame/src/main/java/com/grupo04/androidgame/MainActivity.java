package com.grupo04.androidgame;

import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdView;
import com.grupo04.androidengine.AndroidEngine;
import com.grupo04.gamelogic.GameManager;

public class MainActivity extends AppCompatActivity {
    private AndroidEngine androidEngine;
    private GameManager gameManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SurfaceView window = findViewById(R.id.SurfaceView);
        // Se encarga de cargar los assets
        AssetManager assetManager = this.getAssets();
        AdView adView = findViewById(R.id.AdView);

        // Creacion del motor
        this.androidEngine = new AndroidEngine(this, window, assetManager, adView, 5);

        // Creacion de la escena
        String fileName = "game.json";
        String shopFileName = "shop.json";
        this.gameManager = new GameManager(this.androidEngine, fileName, shopFileName);
        this.androidEngine.setScene(this.gameManager);

        // Bloquear la orientacion
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.androidEngine.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.androidEngine.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.androidEngine.onStop();
    }
}
