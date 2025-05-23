package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IMobile;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.Scene;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.gamelogic.gameobjects.TextWithIcon;
import com.grupo04.gamelogic.gameobjects.buttons.TextButton;
import com.grupo04.gamelogic.gameobjects.Text;

public class VictoryScene extends Scene {
    private final Color BLACK_TEXT_COLOR = new Color(0, 0, 0);
    private final Color YELLOW_TEXT_COLOR = new Color(252, 228, 5);
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

    private final Color YELLOW_BUTTON_BASE_COLOR = new Color(252, 228, 5);
    private final Color YELLOW_BUTTON_OVER_COLOR = new Color(226, 205, 5);
    private final Color BLACK_BUTTON_BASE_COLOR = new Color(10, 10, 10);
    private final Color BLACK_BUTTON_OVER_COLOR = new Color(0, 0, 0);

    private final int NUM_COINS_EARNED = 10;

    private final Color UIColor;
    private TextButton x2Button;
    private TextButton shareButton;
    private TextButton menuButton;
    private TextButton tryAgainButton;
    private TextWithIcon coins;
    private final int levelNumber;
    private final boolean firstTime;

    public VictoryScene(IEngine engine, int score, int levelNumber, boolean firstTime, Color UIColor) {
        super(engine, 400, 600);

        this.levelNumber = levelNumber;
        this.firstTime = firstTime;
        this.UIColor = UIColor;

        Text title = new Text(new Vector(this.worldWidth / 2f, this.worldHeight / 8f), "Victory!",
                TITLE_FONT, TITLE_SIZE, false, false, BLACK_TEXT_COLOR);
        addGameObject(title);

        Text scoreText = new Text(new Vector(this.worldWidth / 2f, 2f * this.worldHeight / 7f), "Score: " + score,
                SCORE_TEXT_FONT, SCORE_TEXT_SIZE, false, false, BLACK_TEXT_COLOR);
        addGameObject(scoreText);

        Vector coinsPos = new Vector(this.worldWidth / 2f, this.worldHeight / 2f);
        this.x2Button = null;
        this.coins = null;

        // Solo para android
        IMobile mobile = engine.getMobile();
        if (mobile != null) {
            coinsPos = new Vector(this.worldWidth / 2f - BUTTON_WIDTH / 3f, 4f * this.worldHeight / 9f);

            if (this.firstTime) {
                Vector x2ButtonPos = new Vector(this.worldWidth / 2f + BUTTON_WIDTH / 4f, coinsPos.y);
                this.x2Button = new TextButton(x2ButtonPos,
                        BUTTON_WIDTH / 2f, BUTTON_HEIGHT, BUTTON_ARC, YELLOW_BUTTON_BASE_COLOR, YELLOW_BUTTON_OVER_COLOR,
                        "x2", BUTTON_FONT, BLACK_TEXT_COLOR, false, AD_IMAGE_PATH, BUTTON_SOUND,
                        () -> mobile.showRewardedAd(this::onReward));
                addGameObject(this.x2Button);
            }

            Vector shareButtonPos = new Vector(this.worldWidth / 2f, coinsPos.y + BUTTON_HEIGHT + BUTTON_OFFSET_Y);
            this.shareButton = new TextButton(shareButtonPos,
                    BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, YELLOW_BUTTON_BASE_COLOR, YELLOW_BUTTON_OVER_COLOR,
                    "Share", BUTTON_FONT, BLACK_TEXT_COLOR, false, SHARE_IMAGE_PATH, BUTTON_SOUND,
                    () -> {
                        IMobile.ShareParams params = new IMobile.ShareParams();
                        params.fullScreen = true;
                        params.shareTitle = "Compartir imagen";

                        // Si no es un nivel y es modo QuickPlay
                        if (this.levelNumber <= 0) {
                            params.extraText = "¡Ha completado el nivel aleatorio del modo QuickPlay!";
                        } else {
                            params.extraText = "¡Ha completado el nivel " + this.levelNumber + "!";
                        }

                        mobile.shareAction(IMobile.ShareActionType.IMAGE, params);
                    });
            addGameObject(this.shareButton);
        }

        if (this.firstTime) {
            String text = "+" + NUM_COINS_EARNED;
            this.coins = new TextWithIcon(coinsPos, COINS_SIZE, COINS_SPACING,
                    text, COINS_TEXT_FONT, BLACK_TEXT_COLOR, false,
                    COINS_IMAGE_PATH);
            addGameObject(this.coins);
        }

        setFade(Fade.OUT, 0.25);
    }

    public VictoryScene(IEngine engine, int score, int levelNumber, boolean firstTime) {
        this(engine, score, levelNumber, firstTime, null);
    }

    @Override
    public void init() {
        // Se reproduce una vez cargado el sonido
        ISound winSound = this.engine.getAudio().newSound("win.wav", true);

        int totalLevels = this.gameManager.getTotalLevels();
        String playButtonText = "Play again";
        if (this.levelNumber > 0 && this.levelNumber < totalLevels) {
            playButtonText = "Next";
        }

        Vector tryAgainButtonPos = new Vector(this.worldWidth / 2f, 4.5f * this.worldHeight / 6f);
        this.tryAgainButton = new TextButton(tryAgainButtonPos,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BLACK_BUTTON_BASE_COLOR, BLACK_BUTTON_OVER_COLOR,
                playButtonText, BUTTON_FONT, YELLOW_TEXT_COLOR, false, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        this.engine.getAudio().stopSound(winSound);
                        if (this.gameManager != null) {
                            int nextLevelNumber = this.levelNumber;
                            if (this.levelNumber > 0 && this.levelNumber < totalLevels) {
                                nextLevelNumber += 1;
                            }
                            this.gameManager.changeToGameScene(nextLevelNumber);
                        }
                    });
                });
        addGameObject(this.tryAgainButton);

        Vector menuButtonPos = new Vector(tryAgainButtonPos);
        menuButtonPos.y += BUTTON_HEIGHT + BUTTON_OFFSET_Y;
        this.menuButton = new TextButton(menuButtonPos,
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BLACK_BUTTON_BASE_COLOR, BLACK_BUTTON_OVER_COLOR,
                "Menu", BUTTON_FONT, YELLOW_TEXT_COLOR, false, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia al menu principal
                    // con animacion de fade out
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        Scene scene;
                        if (this.levelNumber <= 0) {
                            scene = new TitleScene(this.engine, this.UIColor);
                        } else {
                            scene = new LevelsScene(this.engine, this.UIColor);
                        }
                        scene.setFade(Fade.OUT, 0.25);
                        this.engine.getAudio().stopSound(winSound);
                        if (this.gameManager != null) {
                            this.gameManager.changeScene(scene);
                        }
                    });
                });
        addGameObject(this.menuButton);

        if (this.gameManager != null && this.firstTime) {
            this.gameManager.increaseCoins(NUM_COINS_EARNED);
        }

        setUIColor(this.UIColor);

        super.init();
    }

    private void onReward() {
        if (this.x2Button != null) {
            this.x2Button.setAlive(false);
        }
        this.coins.setText("+" + NUM_COINS_EARNED * 2);
        this.coins.setPos(this.worldWidth / 2f);
        if (this.gameManager != null) {
            this.gameManager.increaseCoins(NUM_COINS_EARNED);
        }
    }

    public void setUIColor(Color color) {
        super.setUIColor(color);

        if (this.x2Button != null) {
            this.x2Button.setBaseColor(this.UIColor, YELLOW_BUTTON_BASE_COLOR);
            this.x2Button.setPointerOverColor(this.UIColor, YELLOW_BUTTON_OVER_COLOR);
        }
        if (this.shareButton != null) {
            this.shareButton.setBaseColor(this.UIColor, YELLOW_BUTTON_BASE_COLOR);
            this.shareButton.setPointerOverColor(this.UIColor, YELLOW_BUTTON_OVER_COLOR);
        }
        if (this.tryAgainButton != null) {
            this.tryAgainButton.setFontColor(this.UIColor, YELLOW_BUTTON_BASE_COLOR);
        }
        if (this.menuButton != null) {
            this.menuButton.setFontColor(this.UIColor, YELLOW_BUTTON_BASE_COLOR);
        }
    }
}
