package com.grupo04.desktopengine;

import com.grupo04.engine.Engine;
import com.grupo04.engine.utilities.Callback;

import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;

public class DesktopEngine extends Engine {
    private DesktopAudio desktopAudio;

    public DesktopEngine(JFrame window, int maxStreams) {
        super();

        // Intentamos crear el buffer strategy con 2 buffers
        int attempts = 100;
        while (attempts-- > 0) {
            try {
                window.createBufferStrategy(2);
                break;
            } catch (Exception e) {
                System.out.println("Error creating double buffer: " + e.getMessage());
            }
        }
        if (attempts == 0) {
            System.err.println("BufferStrategy could not be created");
            return;
        } else {
            System.out.println("BufferStrategy after " + (100 - attempts) + " attempts");
        }

        BufferStrategy bufferStrategy = window.getBufferStrategy();
        Graphics2D graphics2D = (Graphics2D) bufferStrategy.getDrawGraphics();

        DesktopGraphics desktopGraphics = new DesktopGraphics(window, graphics2D, bufferStrategy);
        this.desktopAudio = new DesktopAudio(maxStreams);
        DesktopInput desktopInput = new DesktopInput(window, this);
        this.initModules(desktopGraphics, this.desktopAudio, desktopInput);
    }

    // Cierra todos los clips que estuvieran abiertos cuando se cierra el juego en Desktop
    @Override
    public void shutdown() {
        super.shutdown();

        if (this.desktopAudio != null) {
            this.desktopAudio.closeAllClips();
        }
    }

    @Override
    public FileInputStream getFileInputStream(String fileName) {
        // Comprobar si existe el archivo en Documentos del usuario
        File file = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + fileName);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (IOException e) {
                System.err.println("Error while getting FileInputStream from: " + file.getPath() + ": " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public FileOutputStream getFileOutputStream(String fileName) {
        File file = new File(System.getProperty("user.home") + File.separator + "Documents" + File.separator + fileName);
        try {
            return new FileOutputStream(file);
        } catch (IOException e) {
            System.err.println("Error while getting FileOutputStream from: " + file.getPath() + ": " + e.getMessage());
        }
        return null;
    }
}
