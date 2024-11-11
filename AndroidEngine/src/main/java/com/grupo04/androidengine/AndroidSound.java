package com.grupo04.androidengine;

import com.grupo04.engine.Sound;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.SoundPool;

import java.io.IOException;

public class AndroidSound extends Sound {
    private int soundId;      // Devuelto por el load() (0 si falla)
    private int streamId;     // Devuelto por play() (0 si falla)
    private boolean isLoaded; // Para reproducir cuando cargue la escena
    private final SoundPool soundPool;

    AndroidSound(AssetManager assetManager, SoundPool soundPool, String fileName, int priority,
                 float leftVolume, float rightVolume, int loop, float rate) {
        super(fileName, priority, leftVolume, rightVolume, loop, rate);

        this.soundPool  = soundPool;
        this.isLoaded   = false;
        this.streamId   = 0;

        try {
            AssetFileDescriptor audioFile = assetManager.openFd("sounds/" + fileName);
            this.soundId = this.soundPool.load(audioFile, this.priority);
        } catch (IOException e) {
            System.err.printf("Couldn't load sound (\"%s\")%n", fileName);
        }
    }

    @Override
    protected void play() {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized.");
            return;
        }

        this.streamId = this.soundPool.play(this.soundId, this.leftVolume, this.rightVolume, this.priority, this.loop, this.rate);
    }

    @Override
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

    @Override
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

    @Override
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
    public void setPriority(int priority) {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized");
            return;
        }

        if (this.streamId == 0) {
            System.out.println("Sound " + this.soundName + " could not set priority because it has not been played.");
            return;
        }

        super.setPriority(priority);
        this.soundPool.setPriority(this.streamId, this.priority);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (this.soundPool == null) {
            System.err.println("SoundPool not initialized");
            return;
        }

        super.setVolume(leftVolume, rightVolume);
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
            super.setLoop(loop);
            this.soundPool.setLoop(this.streamId, this.loop);
            play();
        }
    }

    public int getSoundId() { return this.soundId; }

    public boolean getLoaded() { return this.isLoaded; }

    public void setLoaded(boolean isLoaded) { this.isLoaded = isLoaded; }
}
