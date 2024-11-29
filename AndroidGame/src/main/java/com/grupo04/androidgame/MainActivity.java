package com.grupo04.androidgame;

import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.ads.AdView;
import com.grupo04.androidengine.AndroidEngine;
import com.grupo04.gamelogic.GameManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private AndroidEngine androidEngine;

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
        GameManager gameManager = new GameManager(this.androidEngine, fileName, shopFileName);
        this.androidEngine.setScene(gameManager);

        this.androidEngine.getMobile().initializeNotifications(R.string.channel_id,
                R.string.channel_name, R.string.channel_description,
                R.string.notifications_workers_tag);

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
        this.androidEngine.getMobile().programNotification(3, TimeUnit.SECONDS,
                "Reward",
                "¡Entra ahora para conseguir tu recompensa diaria!",
                "TEXTO TEXTO TEXTO", R.mipmap.ic_launcher,
                NotificationCompat.PRIORITY_HIGH, NotificationCompat.VISIBILITY_PUBLIC);
        this.androidEngine.onStop();
    }
}
