package com.grupo04.engine;

import com.grupo04.engine.interfaces.IAudio;
import com.grupo04.engine.interfaces.ISound;

public abstract class Audio implements IAudio {
    public abstract ISound newSound(String soundName, int priority, float leftVolume, float rightVolume, int loop, float rate, boolean playOnLoad);
    public ISound newSound(String soundName, int priority, boolean playOnLoad) { return newSound(soundName, priority, 1.0f, 1.0f, 0, 1.0f, playOnLoad); }
    public ISound newSound(String soundName, boolean playOnLoad) { return newSound(soundName, 0, playOnLoad); }
    public ISound newSound(String soundName) { return newSound(soundName, false); }
    public abstract void playSound(ISound sound);
    public abstract void stopSound(ISound sound);
    public abstract void pauseSound(ISound sound);
    public abstract void resumeSound(ISound sound);
}
