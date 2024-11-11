package com.grupo04.desktopengine;

import com.grupo04.engine.Sound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class DesktopSound extends Sound {
    private File audioFile;
    private long currentFrame; // Para el resume()
    private final List<DesktopAudio.ClipEntry> clips;
    private final PriorityQueue<DesktopAudio.ClipEntry> playingPool;
    private final List<DesktopAudio.ClipEntry> updatedEntries;
    private final List<DesktopAudio.ClipEntry> otherEntries;

    DesktopSound(PriorityQueue<DesktopAudio.ClipEntry> playingPool, String fileName, int priority, float leftVolume, float rightVolume, int loop, float rate) {
        super(fileName, priority, leftVolume, rightVolume, loop, rate);

        this.currentFrame = -1;
        this.clips = new ArrayList<>();
        this.playingPool = playingPool;
        this.updatedEntries = new ArrayList<>();
        this.otherEntries = new ArrayList<>();

        try {
            this.audioFile = new File("./assets/sounds/" + fileName);
        } catch (Exception e) {
            System.err.println("Could not load audio file (\"" + fileName + "\"): " + e.getMessage());
        }
    }

    public void addClip(DesktopAudio.ClipEntry clipEntry) { this.clips.add(clipEntry); }

    @Override
    protected void play() {
        try {
            this.currentFrame = -1;
            for (int i = 0; i < this.clips.size(); ++i) {
                DesktopAudio.ClipEntry clipEntry = this.clips.get(i);
                Clip clip = clipEntry.getClip();
                // Si no está abierto, abrimos
                if (!clip.isOpen()) {
                    clip.open(AudioSystem.getAudioInputStream(this.audioFile));
                }
                setClipVolume(clip);
                clip.loop(this.loop);
                clip.start();

                // Si el clip terminó de reproducirse, se para y
                // se elimina de la lista de clips
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.stop();
                        this.clips.remove(clipEntry);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Failed to play the clip: " + e.getMessage());
        }
    }

    @Override
    protected void stop() {
        try {
            if (this.clips.isEmpty()) {
                System.out.println("Sound " + this.soundName + " did not stop because it was not played.");
                return;
            }
            for (int i = 0; i < this.clips.size(); ++i) {
                DesktopAudio.ClipEntry clipEntry = this.clips.get(i);
                Clip clip = clipEntry.getClip();
                if (clip.isRunning()) {
                    clip.stop();
                }
            }
            this.clips.clear();
        } catch (Exception e) {
            System.err.println("Failed to stop the clip: " + e.getMessage());
        }
    }

    @Override
    protected void pause() {
        try {
            if (this.clips.isEmpty()) {
                System.out.println("Sound " + this.soundName + " did not stop because it has not been played.");
                return;
            }
            DesktopAudio.ClipEntry clipEntry = this.clips.get(this.clips.size() - 1);
            Clip lastClip = clipEntry.getClip();
            if (lastClip.isRunning()) {
                this.currentFrame = lastClip.getMicrosecondPosition();
            }
            stop();
        } catch (Exception e) {
            System.err.println("Failed to pause the clip: " + e.getMessage());
        }
    }

    @Override
    protected void resume() {
        try {
            for (int i = 0; i < this.clips.size(); ++i) {
                // Resumimos el sonido desde donde lo dejamos
                // en el pause
                DesktopAudio.ClipEntry clipEntry = this.clips.get(i);
                Clip clip = clipEntry.getClip();
                clip.setMicrosecondPosition(this.currentFrame);
            }
            play();
        } catch (Exception e) {
            System.err.println("Failed to resume the clip: " + e.getMessage());
        }
    }

    @Override
    public void setPriority(int priority) {
        if (this.clips.isEmpty()) {
            System.out.println("Sound " + this.soundName + " could not set priority because it has not been played.");
            return;
        }

        super.setPriority(priority);

        // Para actualizar la prioridad de los clips del mismo sonido.
        // Si coincide que es el mismo clip de mi lista de clips,
        // actualiza la prioridad
        // Si no, no hace nada
        // Al final, se reinserta todos los clips con la prioridad
        // modificada para el mismo sonido
        while (!this.playingPool.isEmpty()) {
            DesktopAudio.ClipEntry clipEntry = this.playingPool.poll();
            if (this.clips.contains(clipEntry)) {
                clipEntry.setPriority(priority);
                this.updatedEntries.add(clipEntry);
            } else {
                this.otherEntries.add(clipEntry);
            }
        }
        this.playingPool.addAll(this.updatedEntries);
        this.playingPool.addAll(this.otherEntries);
        this.updatedEntries.clear();
        this.otherEntries.clear();
    }

    private boolean setClipVolume(Clip clip) {
        try {
            // Ajuste de balance
            FloatControl balanceControl = (FloatControl) clip.getControl(FloatControl.Type.BALANCE);
            float balance = this.rightVolume - this.leftVolume;
            balanceControl.setValue(balance);
            return true;
        } catch (Exception e) {
            System.err.println("Error setting volume: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (this.clips.isEmpty()) {
            System.out.println("Sound " + this.soundName + " could not set volume because it has not been played.");
            return;
        }

        super.setVolume(leftVolume, rightVolume);
        for (int i = 0; i < this.clips.size(); ++i) {
            if (!setClipVolume(this.clips.get(i).getClip())) {
                return;
            }
        }
    }

    @Override
    public void setLoop(int loop) {
        if (this.clips.isEmpty()) {
            System.out.println("Sound " + this.soundName + " could not set loop because it has not been played.");
            return;
        }

        if (this.loop != loop) {
            super.setLoop(loop);
            for (int i = 0; i < this.clips.size(); ++i) {
                this.clips.get(i).getClip().loop(this.loop);
            }
        }
    }
}
