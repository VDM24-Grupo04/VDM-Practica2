package com.grupo04.androidengine;

import com.grupo04.androidengine.ec.Scene;
import com.grupo04.androidengine.audio.IAudio;
import com.grupo04.androidengine.graphics.IGraphics;

public interface IEngine {
    void popScene();
    void pushScene(Scene newScene);
    void changeScene(Scene newScene);
    void setWorldSize(int worldWidth, int worldHeight);
    IGraphics getGraphics();
    IAudio getAudio();
}
