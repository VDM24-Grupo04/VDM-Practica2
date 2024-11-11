package com.grupo04.engine;

import com.grupo04.engine.interfaces.ISound;

public abstract class Sound implements ISound {
    protected String soundName;
    protected int priority;
    protected float leftVolume;
    protected float rightVolume;
    protected int loop;    // 0 = no loop, -1 = loop infinito
    protected float rate;  // 1.0 = normal

    public Sound(String fileName, int priority, float leftVolume, float rightVolume, int loop, float rate) {
        this.soundName = fileName;
        this.priority = priority;
        this.leftVolume = leftVolume;
        this.rightVolume = rightVolume;
        this.loop = loop;
        this.rate = rate;
    }

    protected abstract void play();
    protected abstract void stop();
    protected abstract void pause();
    protected abstract void resume();

    @Override
    public String getSoundName() { return this.soundName; }
    @Override
    public int getPriority() { return this.priority; }
    @Override
    public float getLeftVolume() { return this.leftVolume; }
    @Override
    public float getRightVolume() { return this.rightVolume; }
    @Override
    public int getLoop() { return this.loop; }
    @Override
    public float getRate() { return this.rate; }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        this.leftVolume = leftVolume;
        this.rightVolume = rightVolume;
    }

    @Override
    public void setLoop(int loop) { this.loop = loop; }
}
