package com.grupo04.gamelogic.scenes;

import com.grupo04.androidengine.IEngine;
import com.grupo04.androidengine.utilities.Color;
import com.grupo04.androidengine.ec.Scene;
import com.grupo04.androidengine.utilities.Vector;
import com.grupo04.androidengine.audio.ISound;
import com.grupo04.gamelogic.gameobjects.TextButton;
import com.grupo04.gamelogic.gameobjects.Text;

public class VictoryScene extends Scene {
    final Color TEXT_COLOR = new Color(0, 0, 0);

    final String TITLE_FONT = "TheMeshroomRegular.ttf";
    final float TITLE_SIZE = 62;

    final String SCORE_TEXT_FONT = "kimberley.ttf";
    final float SCORE_TEXT_SIZE = 40;

    final String BUTTON_SOUND = "button.wav";
    final float BUTTON_WIDTH = 205f;
    final float BUTTON_HEIGHT = 55f;
    final float BUTTON_ARC = 25f;
    final Color BUTTON_BASE_COLOR = new Color(44, 166, 28);
    final Color BUTTON_OVER_COLOR = new Color(34, 138, 24);
    final String BUTTON_FONT = "kimberley.ttf";
    final float BUTTON_OFFSET_Y = 25f;

    ISound winSound;

    public VictoryScene(IEngine engine, int score) {
        super(engine, 400, 600, new Color(255, 255, 255));

        Text title = new Text(new Vector(this.worldWidth / 2f, this.worldHeight / 6f), "Victory!",
                TITLE_FONT, TITLE_SIZE, false, false, TEXT_COLOR);
        addGameObject(title);

        Text scoreText = new Text(new Vector(this.worldWidth / 2f, 3f * this.worldHeight / 7f), Integer.toString(score),
                SCORE_TEXT_FONT, SCORE_TEXT_SIZE, false, false, TEXT_COLOR);
        addGameObject(scoreText);

        Vector tryAgainButtonPos = new Vector(this.worldWidth / 2f, 4f * this.worldHeight / 6f);
        TextButton tryAgainButton = new TextButton(tryAgainButtonPos,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Play again", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        this.engine.getAudio().stopSound(this.winSound);
                        this.engine.changeScene(new GameScene(this.engine));
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
                        this.engine.getAudio().stopSound(this.winSound);
                        this.engine.changeScene(scene);
                    });
                });
        addGameObject(menuButton);

        setFade(Fade.OUT, 0.25);

        // Se reproduce una vez cargado el sonido
        this.winSound = engine.getAudio().newSound("win.wav", true);
    }
}
