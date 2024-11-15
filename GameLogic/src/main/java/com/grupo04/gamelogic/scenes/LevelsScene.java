package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.gameobjects.ImageButton;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.gameobjects.TextButton;

import org.json.JSONObject;

public class LevelsScene extends Scene {
    public LevelsScene(IEngine engine, JSONObject jsonObject, int lastLevel) {
        super(engine, 400, 600, new Color(255, 255, 255));

        String TEXT_FONT = "TheMeshroomRegular.ttf";
        Color TEXT_COLOR = new Color(0, 0, 0);
        float TEXT_SIZE = 70;
        float[] TEXT_INDENTING = new float[]{3f, -10f};
        float TEXT_LINE_SPACING = -30f;

        String BUTTON_SOUND = "button.wav";
        float BUTTON_WIDTH = 205f;
        float BUTTON_HEIGHT = 55f;
        float BUTTON_ARC = 25f;
        Color BUTTON_BASE_COLOR = new Color(237, 12, 46);
        Color BUTTON_OVER_COLOR = new Color(203, 10, 38);
        String BUTTON_FONT = "kimberley.ttf";

        int SIDE_BUTTONS_PADDING = 10;
        int SIDE_BUTTONS_SIZE = 40;

        String MENU_BUTTON_IMG = "close.png";

        int HEADER_WIDTH = 50;

        Text title = new Text(new Vector(this.worldWidth / 2f, this.worldHeight / 4f), new String[]{"Aventura"},
                TEXT_FONT, TEXT_SIZE, false, false, TEXT_COLOR,
                TEXT_INDENTING, TEXT_LINE_SPACING);
        addGameObject(title);

        // UI de la parte superior
        ImageButton menuButton = new ImageButton(new Vector(SIDE_BUTTONS_SIZE / 2f + SIDE_BUTTONS_PADDING, HEADER_WIDTH / 2f),
                SIDE_BUTTONS_SIZE, SIDE_BUTTONS_SIZE, MENU_BUTTON_IMG, BUTTON_SOUND,
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

        // Botones de niveles...
        // Hacer algo con el indice del ultimo nivel (lastLevel)
        // if i == lastLevel... se le pasa el jsonObject porque contiene la info del ultimo nivel a medias
        // else se le pasa null donde jsonObject
        Vector prueba1ButtonPos = new Vector(this.worldWidth / 2f, 4f * this.worldHeight / 6f);
        TextButton prueba1Button = new TextButton(prueba1ButtonPos,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Try again", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        if (this.gameManager != null) {
                            // Cambiar al numero del nivel correspondiente
                            this.gameManager.changeScene(new GameScene(this.engine, jsonObject, 1));
                            //this.gameManager.changeScene(new GameScene(this.engine, null, i));
                        }
                    });
                });
        addGameObject(prueba1Button);

        setFade(Fade.OUT, 0.0);
    }
}
