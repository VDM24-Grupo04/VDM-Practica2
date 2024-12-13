package com.grupo04.desktopgame;

import com.grupo04.desktopengine.DesktopEngine;
import com.grupo04.gamelogic.GameManager;

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
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Sin bordes
         window.setUndecorated(true);

        window.setVisible(true);

        // Creacion del motor
        DesktopEngine desktopEngine = new DesktopEngine(window, 5);

        // Creacion de la escena
        String fileName = "game.json";
        String shopFileName = "shop.json";
        GameManager gameManager = new GameManager(desktopEngine, fileName, shopFileName);
        desktopEngine.setScene(gameManager);

        desktopEngine.onResume();
    }
}
