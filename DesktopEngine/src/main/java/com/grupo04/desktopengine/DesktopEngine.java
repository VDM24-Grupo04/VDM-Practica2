package com.grupo04.desktopengine;

import com.grupo04.engine.Engine;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        this.initModules(desktopGraphics, this.desktopAudio, desktopInput, null);

        // Al cerrar la ventana se realiza una salida adecuada del sistema
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                onStop();
                System.exit(0);
            }
        });

        // Se anade al JFrame un listener de eventos de teclado
        window.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            // Pulsar tecla
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                // Si se pulsa el escape
                if (keyEvent.getKeyCode() == 27) {
                    onStop();
                    System.exit(0);
                }
            }

            // Soltar tecla
            @Override
            public void keyReleased(KeyEvent keyEvent) {
            }
        });

        // Se anade al JFrame un listener de foco de la ventana
        window.addWindowFocusListener(new WindowFocusListener() {
            // Si se pierde el foco, se pausa el hilo que ejecuta el juego
            @Override
            public void windowLostFocus(WindowEvent e) {
                onPause();
            }

            // Si se recupera el foco, se reanuda el hilo que ejecuta el juego
            @Override
            public void windowGainedFocus(WindowEvent e) {
                onResume();
            }
        });
    }

    // Cierra todos los clips que estuvieran abiertos cuando se cierra el juego en Desktop
    @Override
    public void onStop() {
        super.onStop();

        if (this.desktopAudio != null) {
            this.desktopAudio.closeAllClips();
        }
    }

    @Override
    public FileInputStream getFileInputStream(String fileName, FileType type) {
        String path = "./assets/" + fileName;
        if (type == FileType.PROGRESS_DATA) {
            path = System.getProperty("user.home") + File.separator + "Documents" + File.separator + fileName;
        }

        // Comprobar si existe el archivo en Documentos del usuario
        File file = new File(path);
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

    @Override
    public String getHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not available: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error computing hash: " + e.getMessage());
        }
        return null;
    }

    private static String bytesToHex(byte[] hash) {
        // Se multiplica x2 porque cada byte representa dos caracteres hexadecimales
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            // Para asegurarse de que el byte se trate como un valor sin signo
            // se hace la operación 0xff & b
            String hex = Integer.toHexString(0xff & b);
            // Si es de un solo carácter (valores de 0 a 15), se añade un 0 antes
            // para asegurar que cada byte se represente siempre por dos caracteres
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
