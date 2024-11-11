package com.grupo04.androidengine;

import android.content.res.AssetManager;
import android.view.SurfaceView;

import com.grupo04.engine.Engine;

public class AndroidEngine extends Engine {
    public AndroidEngine(SurfaceView window, AssetManager assetManager, int maxStreams) {
        super();

        AndroidGraphics androidGraphics = new AndroidGraphics(window, assetManager);
        AndroidAudio androidAudio = new AndroidAudio(assetManager, maxStreams);
        AndroidInput androidInput = new AndroidInput(window);
        this.initModules(androidGraphics, androidAudio, androidInput);
    }
}
