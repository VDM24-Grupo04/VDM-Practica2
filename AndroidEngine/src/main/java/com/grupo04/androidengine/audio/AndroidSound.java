package com.grupo04.androidengine.audio;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.SoundPool;

import java.io.IOException;

public class AndroidSound implements ISound {
    protected String soundName;
    protected int priority;
    protected float leftVolume;
    protected float rightVolume;
    protected int loop;    // 0 = no loop, -1 = loop infinito
    protected float rate;  // 1.0 = normal

    private int soundId;      // Devuelto por el load() (0 si falla)
    private int streamId;     // Devuelto por play() (0 si falla)
    private boolean isLoaded; // Para reproducir cuando cargue la escena
    private final SoundPool soundPool;

    AndroidSound(AssetManager assetManager, SoundPool soundPool, String fileName, int priority,
                 float leftVolume, float rightVolume, int loop, float rate) {
        this.soundName = fileName;
        this.priority = priority;
        this.leftVolume = leftVolume;
        this.rightVolume = rightVolume;
        this.loop = loop;
        this.rate = rate;

        this.soundPool = soundPool;
        this.isLoaded = false;
        this.streamId = 0;

        try {
            AssetFileDescriptor audioFile = assetManager.openFd("sounds/" + fileName);
            this.soundId = this.soundPool.load(audioFile, this.priority);
        } catch (IOException e) {
            System.err.printf("Couldn't load sound (\"%s\")%n", fileName);
        }
    }

    protected void play() {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized.");
            return;
        }

        this.streamId = this.soundPool.play(this.soundId, this.leftVolume, this.rightVolume, this.priority, this.loop, this.rate);
    }

    protected void stop() {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized.");
            return;
        }

        if (this.streamId == 0) {
            System.out.println("Sound " + this.soundName + " did not stop because it has not been played.");
            return;
        }

        this.soundPool.stop(this.streamId);
    }

    protected void pause() {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized.");
            return;
        }

        if (this.streamId == 0) {
            System.out.println("Sound " + this.soundName + " did not pause because it has not been played.");
            return;
        }

        this.soundPool.pause(this.streamId);
    }

    protected void resume() {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized.");
            return;
        }

        if (this.streamId == 0) {
            System.out.println("Sound " + this.soundName + " did not resume because it has not been played.");
            return;
        }

        this.soundPool.resume(this.streamId);
    }

    @Override
    public String getSoundName() {
        return this.soundName;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public float getLeftVolume() {
        return this.leftVolume;
    }

    @Override
    public float getRightVolume() {
        return this.rightVolume;
    }

    @Override
    public int getLoop() {
        return this.loop;
    }

    @Override
    public float getRate() {
        return this.rate;
    }

    @Override
    public void setPriority(int priority) {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized");
            return;
        }

        if (this.streamId == 0) {
            System.out.println("Sound " + this.soundName + " could not set priority because it has not been played.");
            return;
        }

        this.priority = priority;
        this.soundPool.setPriority(this.streamId, this.priority);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized");
            return;
        }

        this.leftVolume = leftVolume;
        this.rightVolume = rightVolume;
        this.soundPool.setVolume(this.streamId, this.leftVolume, this.rightVolume);
    }

    @Override
    public void setLoop(int loop) {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized.");
            return;
        }

        if (this.streamId == 0) {
            System.out.println("Sound " + this.soundName + " could not set loop because it has not been played.");
            return;
        }

        if (this.loop != loop) {
            this.loop = loop;
            this.soundPool.setLoop(this.streamId, this.loop);
            play();
        }
    }

    public int getSoundId() {
        return this.soundId;
    }

    public boolean getLoaded() {
        return this.isLoaded;
    }

    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }
}
