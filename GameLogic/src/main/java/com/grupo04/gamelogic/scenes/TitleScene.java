package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.Scene;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.gameobjects.TextButton;
import com.grupo04.gamelogic.gameobjects.Text;

import org.json.JSONObject;

public class TitleScene extends Scene {
    public TitleScene(IEngine engine) {
        super(engine, 400, 600);

        String TEXT_FONT = "TheMeshroomRegular.ttf";
        Color TEXT_COLOR = new Color(0, 0, 0);
        float TEXT_SIZE = 55;
        float[] TEXT_INDENTING = new float[]{-15f, 15f};
        float TEXT_LINE_SPACING = -15f;

        String BUTTON_SOUND = "button.wav";
        float BUTTON_WIDTH = 205f;
        float BUTTON_HEIGHT = 55f;
        float BUTTON_ARC = 25f;
        Color BUTTON_BASE_COLOR = new Color(252, 228, 5);
        Color BUTTON_OVER_COLOR = new Color(226, 205, 5);
        String BUTTON_FONT = "kimberley.ttf";

        Text title = new Text(new Vector(this.worldWidth / 2f, 2f * this.worldHeight / 7f), new String[]{"Puzzle", "Booble"},
                TEXT_FONT, TEXT_SIZE, false, false, TEXT_COLOR,
                TEXT_INDENTING, TEXT_LINE_SPACING);
        addGameObject(title);

        TextButton adventure = new TextButton(new Vector(this.worldWidth / 2f, 2.5f * this.worldHeight / 5f),
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Aventura", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        if (this.gameManager != null) {
                            // Le pasamos el jsonObject del modo de Aventura
                            this.gameManager.changeScene(new LevelsScene(this.engine, this.gameManager.getAdventureJSONObj(), this.gameManager.getLastLevel()));
                        }
                    });
                });
        addGameObject(adventure);

        TextButton quickPlay = new TextButton(new Vector(this.worldWidth / 2f, 3.2f * this.worldHeight / 5f),
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Juego rápido", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        if (this.gameManager != null) {
                            // Le pasamos el jsonObject del modo de Juego Rapido
                            // con id = 0
                            this.gameManager.changeScene(new GameScene(this.engine, this.gameManager.getQuickPlayJSONObj(), 0));
                        }
                    });
                });
        addGameObject(quickPlay);

        Color SHOP_BUTTON_BASE_COLOR = new Color(62, 62, 62);
        Color SHOP_BUTTON_OVER_COLOR = new Color(0, 0, 0);
        Color SHOP_FONT_COLOR = new Color(252, 228, 5);
        TextButton shop = new TextButton(new Vector(this.worldWidth / 2f, 3.9f * this.worldHeight / 5f),
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, SHOP_BUTTON_BASE_COLOR, SHOP_BUTTON_OVER_COLOR,
                "Tienda", BUTTON_FONT, SHOP_FONT_COLOR, true, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        if (this.gameManager != null) {
                             this.gameManager.changeScene(new ShopScene(this.engine));
                        }
                    });
                });
        addGameObject(shop);
    }
}
