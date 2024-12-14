package com.grupo04.gamelogic;

import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;

import java.util.HashMap;
import java.util.Random;

public class BubbleColors {
    private static final Color[] colors = {
            new Color(245, 75, 100),    // Rojo
            new Color(105, 245, 85),    // Verde
            new Color(135, 150, 235),   // Azul
            new Color(245, 225, 85)     // Amarillo
    };
    private final Random randomNumbers = new Random();
    private final HashMap<Integer, Color> availableColors = new HashMap<>();

    // Obtiene un color aleatorio de entre los que hay actualmente en el mapa
    public int getRandomColor() {
        // Si esta vacio (no hay mas bolas) devuelve "sin color"
        if (this.availableColors.isEmpty()) {
            return -1;
        }
        // Coge las claves disponibles del hashmap
        Integer[] keys = this.availableColors.keySet().toArray(new Integer[0]);
        return keys[this.randomNumbers.nextInt(keys.length)];
    }

    public static int getTotalColors() { return colors.length; }
    // Genera un color aleatorio de entre todos los colores posibles
    public int generateRandomColor() {
        return randomNumbers.nextInt(colors.length);
    }

    public void reset() {
        this.availableColors.clear();
    }

    public static Color getColor(int i) {
        return colors[i];
    }

    public void removeColor(int i) {
        this.availableColors.remove(i);
    }

    public void addColor(int i) {
        if (i >= 0 && i < colors.length) {
            this.availableColors.put(i, colors[i]);
        }
    }

    public void drawBall(IGraphics graphics, GameManager gameManager, int color, Vector pos, int r) {
        // Se dibuja la bola
        if (color >= 0) {
            // Si la bola usa skin, se pinta la skin
            if (gameManager.getBallSkin(color) != null) {
                graphics.drawImage(gameManager.getBallSkin(color), pos, r * 2, r * 2);
            }
            // Si no, se pinta el circulo del color indicado
            else {
                graphics.setColor(colors[color]);
                graphics.fillCircle(pos, r);
            }
        }
    }
}
