package com.grupo04.engine.interfaces;

public interface IAudio {
    ISound newSound(String soundName, int priority, float leftVolume, float rightVolume, int loop, float rate, boolean playOnLoad);
    ISound newSound(String soundName, int priority, boolean playOnLoad);
    ISound newSound(String soundName, boolean playOnLoad);
    ISound newSound(String soundName);
    void playSound(ISound sound);
    void stopSound(ISound sound);
    void pauseSound(ISound sound);
    void resumeSound(ISound sound);
}
