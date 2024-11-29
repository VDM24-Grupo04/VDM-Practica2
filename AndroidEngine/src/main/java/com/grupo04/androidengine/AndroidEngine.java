package com.grupo04.androidengine;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.SurfaceView;
import android.view.View;

import androidx.core.content.FileProvider;

import com.google.android.gms.ads.AdView;
import com.grupo04.engine.Engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AndroidEngine extends Engine {
    private final Activity mainActivity;
    private final SurfaceView window;
    private final AndroidMobile androidMobile;

    public AndroidEngine(Activity mainActivity, SurfaceView window, AssetManager assetManager, AdView adView, int maxStreams) {
        super();

        this.mainActivity = mainActivity;
        this.window = window;

        AndroidGraphics androidGraphics = new AndroidGraphics(this.window, assetManager);
        AndroidAudio androidAudio = new AndroidAudio(assetManager, maxStreams);
        AndroidInput androidInput = new AndroidInput(this.window);
        this.androidMobile = new AndroidMobile(this.mainActivity, this.window, adView);

        System.loadLibrary("HashLibrary");

        this.initModules(androidGraphics, androidAudio, androidInput, this.androidMobile);
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

    private native String hash(String data);

    @Override
    public String getHash(String data) {
        return hash(data);
    }

    @Override
    public void shareAction(ShareActionType type, ShareParams params) {
        switch (type) {
            case IMAGE:
                if (params.fullScreen) {
                    this.androidMobile.shareImageAction(params.extraText, 0,0, this.window.getWidth(), this.window.getHeight());
                } else {
                    this.androidMobile.shareImageAction(params.extraText, params.x, params.y, params.w, params.h);
                }
                break;
            case TEXT: this.androidMobile.shareTextAction(params.extraText); break;
            // ...
        }
    }
}
