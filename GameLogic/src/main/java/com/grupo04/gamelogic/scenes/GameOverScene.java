package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.Scene;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.gamelogic.gameobjects.buttons.TextButton;
import com.grupo04.gamelogic.gameobjects.Text;

public class GameOverScene extends Scene {
    private final String TEXT_FONT = "TheMeshroomRegular.ttf";
    private final Color TEXT_COLOR = new Color(0, 0, 0);
    private final float TEXT_SIZE = 70;
    private final float[] TEXT_INDENTING = new float[]{3f, -10f};
    private final float TEXT_LINE_SPACING = -30f;

    private final String BUTTON_SOUND = "button.wav";
    private final float BUTTON_WIDTH = 205f;
    private final float BUTTON_HEIGHT = 55f;
    private final float BUTTON_ARC = 25f;
    private final Color BUTTON_BASE_COLOR = new Color(237, 12, 46);
    private final Color BUTTON_OVER_COLOR = new Color(203, 10, 38);
    private final String BUTTON_FONT = "kimberley.ttf";
    private final float BUTTON_OFFSET_Y = 25f;

    public GameOverScene(IEngine engine, int levelNumber) {
        super(engine, 400, 600);

        Text title = new Text(new Vector(this.worldWidth / 2f, this.worldHeight / 4f), new String[]{"Game", "Over!"},
                TEXT_FONT, TEXT_SIZE, false, false, TEXT_COLOR,
                TEXT_INDENTING, TEXT_LINE_SPACING);
        addGameObject(title);

        // Se reproduce una vez cargado el sonido
        ISound loseSound = engine.getAudio().newSound("lose.wav", true);

        Vector tryAgainButtonPos = new Vector(this.worldWidth / 2f, 4f * this.worldHeight / 6f);
        TextButton tryAgainButton = new TextButton(tryAgainButtonPos,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Try again", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        this.engine.getAudio().stopSound(loseSound);
                        if (this.gameManager != null) {
                            this.gameManager.changeToGameScene(levelNumber);
                        }
                    });
                });
        addGameObject(tryAgainButton);

        Vector menuButtonPos = new Vector(tryAgainButtonPos);
        menuButtonPos.y += BUTTON_HEIGHT + BUTTON_OFFSET_Y;
        TextButton menuButton = new TextButton(menuButtonPos,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Menu", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia al menu principal
                    // con animacion de fade out
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        TitleScene scene = new TitleScene(this.engine);
                        scene.setFade(Fade.OUT, 0.25);
                        this.engine.getAudio().stopSound(loseSound);
                        if (this.gameManager != null) {
                            this.gameManager.changeScene(scene);
                        }
                    });
                });
        addGameObject(menuButton);

        setFade(Fade.OUT, 0.0);
    }
}
