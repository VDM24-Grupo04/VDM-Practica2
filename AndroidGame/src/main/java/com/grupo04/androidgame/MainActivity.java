package com.grupo04.androidgame;

import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import com.grupo04.androidengine.AndroidEngine;
import com.grupo04.gamelogic.GameManager;

public class MainActivity extends AppCompatActivity {
    private AndroidEngine androidEngine;
    private GameManager gameManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se encarga de cargar los assets
        AssetManager assetManager = this.getAssets();

        // Creamos el SurfaceView que "contendrá" nuestra escena
        SurfaceView window = new SurfaceView(this);
        setContentView(window);

        // Creacion del motor
        this.androidEngine = new AndroidEngine(window, assetManager, this, 5);

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
}
