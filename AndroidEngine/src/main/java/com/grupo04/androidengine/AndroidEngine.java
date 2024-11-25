package com.grupo04.androidengine;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.view.SurfaceView;

import com.google.android.gms.ads.AdView;
import com.grupo04.engine.Engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AndroidEngine extends Engine {
    private final Activity mainActivity;

    public AndroidEngine(Activity mainActiviy, SurfaceView window, AssetManager assetManager, AdView adView, int maxStreams) {
        super();

        this.mainActivity = mainActiviy;

        AndroidGraphics androidGraphics = new AndroidGraphics(window, assetManager);
        AndroidAudio androidAudio = new AndroidAudio(assetManager, maxStreams);
        AndroidInput androidInput = new AndroidInput(window);
        AndroidMobile androidMobile = new AndroidMobile(this.mainActivity, adView);
        this.initModules(androidGraphics, androidAudio, androidInput, androidMobile);
    }

    @Override
    public InputStream getFileInputStream(String fileName, FileType type) {
        if (type == FileType.PROGRESS_DATA) {
            File file = new File(this.mainActivity.getFilesDir(), fileName);
            if (!file.exists()) {
                System.out.println("File not found: " + fileName);
                return null;
            }

            // Si el archivo existe
            try {
                return this.mainActivity.openFileInput(fileName);
            } catch (IOException e) {
                System.err.println("Error while getting FileInputStream from: " + fileName + ": " + e.getMessage());
                return null;
            }
        } else {
            try {
                return this.mainActivity.getAssets().open(fileName);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    public FileOutputStream getFileOutputStream(String fileName) {
        try {
            return this.mainActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
        } catch (IOException e) {
            System.err.println("Error while getting FileOutputStream from: " + fileName + ": " + e.getMessage());
            return null;
        }
    }

}
