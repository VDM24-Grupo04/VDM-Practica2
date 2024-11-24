package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.gameobjects.buttons.ImageButton;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.gameobjects.buttons.TextButton;

import org.json.JSONObject;

public class LevelsScene extends Scene {
    private final String FONT_NAME = "kimberley.ttf";
    private final Color TEXT_COLOR = new Color(0, 0, 0);

    private final int HEADER_SIZE = 40;
    private final int HEADER_OFFSET = 20;

    private final String BUTTON_SOUND = "button.wav";
    private final float BUTTON_WIDTH = 205f;
    private final float BUTTON_HEIGHT = 55f;
    private final float BUTTON_ARC = 25f;
    private final Color BUTTON_BASE_COLOR = new Color(237, 12, 46);
    private final Color BUTTON_OVER_COLOR = new Color(203, 10, 38);
    private final String BUTTON_FONT = "kimberley.ttf";
    private final String MENU_BUTTON_IMG = "close.png";


    // private float itemSize;        // Se anade el boton de volver al menu inicial
    // private final int ITEMS_PER_ROW = 4, ITEM_OFFSET = 10;

    public LevelsScene(IEngine engine) {
        super(engine, 400, 600, new Color(255, 255, 255));

        // UI de la parte superior
        ImageButton menuButton = new ImageButton(
                new Vector(this.HEADER_SIZE / 2f + this.HEADER_OFFSET, this.HEADER_OFFSET + this.HEADER_SIZE / 2.0f),
                this.HEADER_SIZE, this.HEADER_SIZE, MENU_BUTTON_IMG, this.BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia al menu principal
                    // con animacion de fade out
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        TitleScene scene = new TitleScene(this.engine);
                        scene.setFade(Fade.OUT, 0.25);
                        if (this.gameManager != null) {
                            this.gameManager.changeScene(scene);
                        }
                    });
                });
        addGameObject(menuButton);

        // Se anade el texto del titulo
        Text title = new Text(
                new Vector(this.worldWidth / 2f, this.HEADER_OFFSET + this.HEADER_SIZE / 2.0f),
                "Aventura", this.FONT_NAME, this.HEADER_SIZE, false, false, this.TEXT_COLOR);
        addGameObject(title);

        Vector testButtonVector = new Vector(this.worldWidth / 2f, 4f * this.worldHeight / 6f);
        TextButton testButton = new TextButton(testButtonVector,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Try again", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        if (this.gameManager != null) {
                            // Cambiar al numero del nivel correspondiente
                            this.gameManager.changeToGameScene(1);
                        }
                    });
                });
        addGameObject(testButton);

        // Se calcula el tamano de los objetos dependiendo del numero de objetos por fila
        // float freeSpace = this.worldWidth - this.HEADER_OFFSET * 2 - this.ITEM_OFFSET * (this.ITEMS_PER_ROW - 1);
        // this.itemSize = freeSpace / this.ITEMS_PER_ROW;

        // Anadir los niveles

        // Al iniciar la escena se hace un fade out

        setFade(Fade.OUT, 0.25);
    }
}
