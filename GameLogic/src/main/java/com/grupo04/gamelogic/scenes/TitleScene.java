package com.grupo04.gamelogic.scenes;

import com.grupo04.androidengine.IEngine;
import com.grupo04.androidengine.utilities.Color;
import com.grupo04.androidengine.ec.Scene;
import com.grupo04.androidengine.utilities.Vector;
import com.grupo04.gamelogic.gameobjects.TextButton;
import com.grupo04.gamelogic.gameobjects.Text;

public class TitleScene extends Scene {
    final String TEXT_FONT = "TheMeshroomRegular.ttf";
    final Color TEXT_COLOR = new Color(0, 0, 0);
    final float TEXT_SIZE = 55;
    final float[] TEXT_INDENTING = new float[]{-15f, 15f};
    final float TEXT_LINE_SPACING = -15f;

    final String BUTTON_SOUND = "button.wav";
    final float BUTTON_WIDTH = 205f;
    final float BUTTON_HEIGHT = 55f;
    final float BUTTON_ARC = 25f;
    final Color BUTTON_BASE_COLOR = new Color(252, 228, 5);
    final Color BUTTON_OVER_COLOR = new Color(226, 205, 5);
    final String BUTTON_FONT = "kimberley.ttf";

    public TitleScene(IEngine engine) {
        super(engine, 400, 600, new Color(255, 255, 255));

        Text title = new Text(new Vector(this.worldWidth / 2f, 2f * this.worldHeight / 7f), new String[]{"Puzzle", "Booble"},
                TEXT_FONT, TEXT_SIZE, false, false, TEXT_COLOR,
                TEXT_INDENTING, TEXT_LINE_SPACING);
        addGameObject(title);

        TextButton playButton = new TextButton(new Vector(this.worldWidth / 2f, 3f * this.worldHeight / 5f),
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Play", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        this.engine.changeScene(new GameScene(this.engine));
                    });
                });
        addGameObject(playButton);
    }
}
