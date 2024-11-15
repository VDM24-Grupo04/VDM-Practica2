package com.grupo04.androidengine;

import android.content.Context;
import android.content.res.AssetManager;
import android.view.SurfaceView;

import com.grupo04.engine.Engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AndroidEngine extends Engine {
    private final Context context;

    public AndroidEngine(SurfaceView window, AssetManager assetManager, Context context, int maxStreams) {
        super();

        this.context = context;

        AndroidGraphics androidGraphics = new AndroidGraphics(window, assetManager);
        AndroidAudio androidAudio = new AndroidAudio(assetManager, maxStreams);
        AndroidInput androidInput = new AndroidInput(window);
        this.initModules(androidGraphics, androidAudio, androidInput);
    }

    @Override
    public void onPause() {
        this.shutdown();
        super.onPause();
    }

    @Override
    public FileInputStream getFileInputStream(String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            return null;
        }

        // Si el archivo existe
        try {
            return this.context.openFileInput(fileName);
        } catch (IOException e) {
            System.err.println("Error while getting FileInputStream from: " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public FileOutputStream getFileOutputStream(String fileName) {
        try {
            return this.context.openFileOutput(fileName, Context.MODE_PRIVATE);
        } catch (IOException e) {
            System.err.println("Error while getting FileOutputStream from: " + fileName + ": " + e.getMessage());
            return null;
        }
    }
}
