package com.grupo04.androidgame;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.ads.AdView;
import com.grupo04.androidengine.AndroidEngine;
import com.grupo04.gamelogic.GameManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final int SENSOR_TYPE = Sensor.TYPE_GYROSCOPE;
    private final float SHAKE_ACCELERATION = 1;

    private AndroidEngine androidEngine;
    private GameManager gameManager;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private float acceleration = 0.0f;
    private float currentAcceleration = 0.0f;

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
        this.androidEngine.setScene(gameManager);

        this.androidEngine.getMobile().initializeNotifications(R.string.channel_id,
                R.string.channel_name, R.string.channel_description,
                R.string.workers_tag);

        // Bloquear la orientacion
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Opcional: sensor de acelerometro
        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.gyroscopeSensor = this.sensorManager.getDefaultSensor(SENSOR_TYPE);
        this.sensorManager.registerListener(this, this.gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.sensorManager.registerListener(this, this.gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        this.androidEngine.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.sensorManager.unregisterListener(this);
        this.androidEngine.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.androidEngine.getMobile().programNotification(3, TimeUnit.SECONDS,
                "Reward1",
                "¡Entra ahora para conseguir tu recompensa diaria!",
                "1 moneda gratis para ti porque me caes bien", R.drawable.ic_notification,
                NotificationCompat.PRIORITY_HIGH, NotificationCompat.VISIBILITY_PUBLIC);
        this.androidEngine.onStop();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == SENSOR_TYPE) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            float lastAcceleration = currentAcceleration;

            currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = currentAcceleration - lastAcceleration;
            acceleration = acceleration * 0.9f + delta;

            if (acceleration > SHAKE_ACCELERATION) {
                this.gameManager.increaseCoins(1);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
