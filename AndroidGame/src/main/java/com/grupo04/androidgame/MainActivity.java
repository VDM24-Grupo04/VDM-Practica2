package com.grupo04.androidgame;

import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdView;
import com.grupo04.androidengine.AndroidEngine;
import com.grupo04.engine.interfaces.ISensor;
import com.grupo04.gamelogic.GameManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
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
        String notificationIconName = "ic_notification";
        this.gameManager = new GameManager(this.androidEngine, fileName, shopFileName, notificationIconName);
        this.androidEngine.setScene(this.gameManager);

        // Bloquear la orientacion
        // this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_GYROSCOPE:
                androidEngine.sensorChanged(
                        new com.grupo04.engine.Sensor(ISensor.SensorType.GYROSCOPE, sensorEvent.values));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
