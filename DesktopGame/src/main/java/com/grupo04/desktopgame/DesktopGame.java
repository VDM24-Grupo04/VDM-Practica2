package com.grupo04.desktopgame;

import com.grupo04.desktopengine.DesktopEngine;
import com.grupo04.gamelogic.GameManager;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class DesktopGame {
    public static void main(String[] args) {
        // Creacion de la ventana desktop
        JFrame window = new JFrame("Puzzle Bobble");
        ImageIcon icon = new ImageIcon("./assets/PuzzleBubbleIcon.png");
        window.setIconImage(icon.getImage());
        window.setSize(400, 600);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setIgnoreRepaint(true);

        // Pantalla completa
        //window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Sin bordes
        //window.setUndecorated(true);

        window.setVisible(true);

        // Creacion del motor
        DesktopEngine desktopEngine = new DesktopEngine(window, 5);

        // Creacion de la escena
        String fileName = "game.json";
        String shopFileName = "shop.json";
        GameManager gameManager = new GameManager(desktopEngine, fileName, shopFileName);
        desktopEngine.setScene(gameManager);

        desktopEngine.onResume();


        // Al cerrar la ventana se realiza una salida adecuada del sistema
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                desktopEngine.shutdown();
                System.exit(0);
            }
        });

        // Se anade al JFrame un listener de eventos de teclado
        window.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) { }

            // Pulsar tecla
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                // Si se pulsa el escape
                if (keyEvent.getKeyCode() == 27) {
                    desktopEngine.shutdown();
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
                desktopEngine.onPause();
            }
            // Si se recupera el foco, se reanuda el hilo que ejecuta el juego
            @Override
            public void windowGainedFocus(WindowEvent e) {
                desktopEngine.onResume();
            }
        });
    }
}
