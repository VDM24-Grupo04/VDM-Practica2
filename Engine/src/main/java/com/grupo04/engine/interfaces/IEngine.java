package com.grupo04.engine.interfaces;

public interface IEngine {
    void setWorldSize(int worldWidth, int worldHeight);
    void setScene(IScene scene);
    IGraphics getGraphics();
    IAudio getAudio();
}
