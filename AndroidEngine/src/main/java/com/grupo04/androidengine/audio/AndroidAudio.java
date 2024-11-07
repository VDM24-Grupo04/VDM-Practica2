package com.grupo04.androidengine.audio;

import android.content.res.AssetManager;
import android.media.SoundPool;

public class AndroidAudio implements IAudio {
    private final SoundPool soundPool;
    private final AssetManager assetManager;

    public AndroidAudio(AssetManager assetManager, int maxStreams) {
        this.assetManager = assetManager;
        this.soundPool = new SoundPool.Builder().setMaxStreams(maxStreams).build();
    }

    @Override
    public ISound newSound(String fileName, int priority, float leftVolume, float rightVolume, int loop, float rate, boolean playOnLoad) {
        if (!fileName.isBlank() && !fileName.isEmpty()) {
            AndroidSound sound = new AndroidSound(this.assetManager, this.soundPool, fileName, priority, leftVolume, rightVolume, loop, rate);
            if (playOnLoad) {
                // Añade un listener cuando cargue el audio para reproducirlo a primera instancia
                this.soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
                    if (sound.getSoundId() == sampleId && status == 0) {
                        if (!sound.getLoaded()) {
                            sound.play();
                            sound.setLoaded(true);
                        }
                    }
                });
            }
            return sound;
        }
        return null;
    }

    public ISound newSound(String soundName, int priority, boolean playOnLoad) {
        return newSound(soundName, priority, 1.0f, 1.0f, 0, 1.0f, playOnLoad);
    }

    public ISound newSound(String soundName, boolean playOnLoad) {
        return newSound(soundName, 0, playOnLoad);
    }

    public ISound newSound(String soundName) {
        return newSound(soundName, false);
    }

    @Override
    public void playSound(ISound sound) {
        AndroidSound s = (AndroidSound) sound;
        s.play();
    }

    @Override
    public void stopSound(ISound sound) {
        AndroidSound s = (AndroidSound) sound;
        s.stop();
    }

    @Override
    public void pauseSound(ISound sound) {
        AndroidSound s = (AndroidSound) sound;
        s.pause();
    }

    @Override
    public void resumeSound(ISound sound) {
        AndroidSound s = (AndroidSound) sound;
        s.resume();
    }
}
