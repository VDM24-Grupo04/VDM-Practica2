package com.grupo04.engine.interfaces;

public interface ISound {
    String getSoundName();
    int getPriority();
    float getLeftVolume();
    float getRightVolume();
    int getLoop();
    float getRate();
    void setPriority(int priority);
    void setVolume(float leftVolume, float rightVolume);
    void setLoop(int loop);
}
