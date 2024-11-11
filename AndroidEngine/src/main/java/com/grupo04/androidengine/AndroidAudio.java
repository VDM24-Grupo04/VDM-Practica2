package com.grupo04.androidengine;

import android.content.res.AssetManager;
import android.media.SoundPool;

import com.grupo04.engine.Audio;
import com.grupo04.engine.interfaces.ISound;

public class AndroidAudio extends Audio {
    private final SoundPool soundPool;
    private final AssetManager assetManager;

    public AndroidAudio(AssetManager assetManager, int maxStreams) {
        this.assetManager = assetManager;
        this.soundPool = new SoundPool.Builder().setMaxStreams(maxStreams).build();
    }

    @Override
    public AndroidSound newSound(String fileName, int priority, float leftVolume, float rightVolume, int loop, float rate, boolean playOnLoad) {
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
