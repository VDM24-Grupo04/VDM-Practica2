package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IMobile;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.GameObject;
import com.grupo04.gamelogic.Scene;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.gamelogic.gameobjects.TextWithIcon;
import com.grupo04.gamelogic.gameobjects.buttons.TextButton;
import com.grupo04.gamelogic.gameobjects.Text;

import org.json.JSONObject;

public class VictoryScene extends Scene {
    private final Color TEXT_COLOR = new Color(0, 0, 0);

    private final String TITLE_FONT = "TheMeshroomRegular.ttf";
    private final float TITLE_SIZE = 65;

    private final String SCORE_TEXT_FONT = "kimberley.ttf";
    private final float SCORE_TEXT_SIZE = 38;

    private final String COINS_IMAGE_PATH = "coin.png";
    private final String COINS_TEXT_FONT = "kimberley.ttf";
    private final int COINS_SIZE = 34;
    private final int COINS_SPACING = 3;

    private final String AD_IMAGE_PATH = "adIcon.png";
    private final String SHARE_IMAGE_PATH = "shareIcon.png";

    private final String BUTTON_SOUND = "button.wav";
    private final float BUTTON_WIDTH = 230f;
    private final float BUTTON_HEIGHT = 53f;
    private final float BUTTON_ARC = 25f;
    private final String BUTTON_FONT = "kimberley.ttf";
    private final float BUTTON_OFFSET_Y = 15f;

    private final Color GREEN_BUTTON_BASE_COLOR = new Color(44, 166, 28);
    private final Color GREEN_BUTTON_OVER_COLOR = new Color(34, 138, 24);
    private final Color YELLOW_BUTTON_BASE_COLOR = new Color(252, 228, 5);
    private final Color YELLOW_BUTTON_OVER_COLOR = new Color(226, 205, 5);

    private final int N_COINS_EARNED = 10;

    private TextButton x2Button;
    private TextWithIcon coins;

    public VictoryScene(IEngine engine, int score, int worldNumber, int levelNumber) {
        super(engine, 400, 600, new Color(255, 255, 255));

        Text title = new Text(new Vector(this.worldWidth / 2f, this.worldHeight / 8f), "Victory!",
                TITLE_FONT, TITLE_SIZE, false, false, TEXT_COLOR);
        addGameObject(title);

        Text scoreText = new Text(new Vector(this.worldWidth / 2f, 2f * this.worldHeight / 7f), "Score: " + score,
                SCORE_TEXT_FONT, SCORE_TEXT_SIZE, false, false, TEXT_COLOR);
        addGameObject(scoreText);

        Vector coinsPos = new Vector(this.worldWidth / 2f, this.worldHeight / 2f);
        this.x2Button = null;
        this.coins = null;

        // Solo para android
        IMobile mobile = engine.getMobile();
        if (mobile != null) {
            coinsPos = new Vector(this.worldWidth / 2f - BUTTON_WIDTH / 3f, 4f * this.worldHeight / 9f);

            Vector x2ButtonPos = new Vector(this.worldWidth / 2f + BUTTON_WIDTH / 4f, coinsPos.y);
            this.x2Button = new TextButton(x2ButtonPos,
                    BUTTON_WIDTH / 2f, BUTTON_HEIGHT, BUTTON_ARC, GREEN_BUTTON_BASE_COLOR, GREEN_BUTTON_OVER_COLOR,
                    "x2", BUTTON_FONT, TEXT_COLOR, false, AD_IMAGE_PATH, BUTTON_SOUND,
                    () -> {
                        mobile.showRewardedAd(this::onReward);
                    });
            addGameObject(this.x2Button);

            Vector shareButtonPos = new Vector(this.worldWidth / 2f, coinsPos.y + BUTTON_HEIGHT + BUTTON_OFFSET_Y);
            TextButton shareButton = new TextButton(shareButtonPos,
                    BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, GREEN_BUTTON_BASE_COLOR, GREEN_BUTTON_OVER_COLOR,
                    "Compartir", BUTTON_FONT, TEXT_COLOR, false, SHARE_IMAGE_PATH, BUTTON_SOUND,
                    () -> {
                        IEngine.ShareParams params = new IEngine.ShareParams();
                        params.fullScreen = true;

                        // Si no es un nivel y es modo QuickPlay
                        if (levelNumber <= 0) {
                            params.extraText = "¡Ha completado el nivel aleatorio del modo QuickPlay!";
                        } else {
                            params.extraText = "¡Ha completado el nivel " + levelNumber + "!";
                        }

                        this.engine.shareAction(IEngine.ShareActionType.IMAGE, params);
                    });
            addGameObject(shareButton);
        }

        String text = "+" + N_COINS_EARNED;
        this.coins = new TextWithIcon(coinsPos, COINS_SIZE, COINS_SPACING,
                text, COINS_TEXT_FONT, TEXT_COLOR, false,
                COINS_IMAGE_PATH);
        addGameObject(this.coins);

        // Se reproduce una vez cargado el sonido
        ISound winSound = engine.getAudio().newSound("win.wav", true);

        Vector tryAgainButtonPos = new Vector(this.worldWidth / 2f, 4.5f * this.worldHeight / 6f);
        TextButton tryAgainButton = new TextButton(tryAgainButtonPos,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, YELLOW_BUTTON_BASE_COLOR, YELLOW_BUTTON_OVER_COLOR,
                "Play again", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        this.engine.getAudio().stopSound(winSound);
                        if (this.gameManager != null) {
                            this.gameManager.changeToGameScene(worldNumber, levelNumber);
                        }
                    });
                });
        addGameObject(tryAgainButton);

        Vector menuButtonPos = new Vector(tryAgainButtonPos);
        menuButtonPos.y += BUTTON_HEIGHT + BUTTON_OFFSET_Y;
        TextButton menuButton = new TextButton(menuButtonPos,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, YELLOW_BUTTON_BASE_COLOR, YELLOW_BUTTON_OVER_COLOR,
                "Menu", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia al menu principal
                    // con animacion de fade out
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        TitleScene scene = new TitleScene(this.engine);
                        scene.setFade(Fade.OUT, 0.25);
                        this.engine.getAudio().stopSound(winSound);
                        if (this.gameManager != null) {
                            this.gameManager.changeScene(scene);
                        }
                    });
                });
        addGameObject(menuButton);

        setFade(Fade.OUT, 0.25);
    }

    @Override
    public void init() {
        if (this.gameManager != null) {
            this.gameManager.increaseCoins(N_COINS_EARNED);
        }

        super.init();
    }

    private void onReward() {
        this.x2Button.setAlive(false);
        this.coins.setText("+" + N_COINS_EARNED * 2);
        this.coins.setPos(worldWidth / 2f);
        if (this.gameManager != null) {
            this.gameManager.increaseCoins(N_COINS_EARNED);
        }
    }
}
