package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.Scene;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.gameobjects.buttons.TextButton;
import com.grupo04.gamelogic.gameobjects.Text;

public class TitleScene extends Scene {
    private final String TEXT_FONT = "TheMeshroomRegular.ttf";
    private final Color TEXT_COLOR = new Color(0, 0, 0);
    private final float TEXT_SIZE = 55;
    private final float[] TEXT_INDENTING = new float[]{-15f, 15f};
    private final float TEXT_LINE_SPACING = -15f;

    private final String BUTTON_SOUND = "button.wav";
    private final float BUTTON_WIDTH = 205f;
    private final float BUTTON_HEIGHT = 52f;
    private final float BUTTON_ARC = 25f;
    private final Color BUTTON_BASE_COLOR = new Color(252, 228, 5);
    private final Color BUTTON_OVER_COLOR = new Color(226, 205, 5);
    private final String BUTTON_FONT = "kimberley.ttf";

    private final Color SHOP_BUTTON_BASE_COLOR = new Color(10, 10, 10);
    private final Color SHOP_BUTTON_OVER_COLOR = new Color(0, 0, 0);
    private final Color SHOP_FONT_COLOR = new Color(252, 228, 5);

    private final TextButton adventureButton;
    private final TextButton quickPlayButton;
    private final TextButton shopButton;

    public TitleScene(IEngine engine, Color UIColor) {
        super(engine, 400, 600, UIColor);

        // Texto de titulo
        Text title = new Text(new Vector(this.worldWidth / 2f, 2f * this.worldHeight / 7f), new String[]{"Puzzle", "Booble"},
                TEXT_FONT, TEXT_SIZE, false, false, TEXT_COLOR,
                TEXT_INDENTING, TEXT_LINE_SPACING);
        addGameObject(title);

        // Boton del modo aventura
        this.adventureButton = new TextButton(new Vector(this.worldWidth / 2f, 2.5f * this.worldHeight / 5f),
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Adventure", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        if (this.gameManager != null) {
                            // Le pasamos el jsonObject del modo de Aventura
                            this.gameManager.changeScene(new LevelsScene(this.engine, this.UIColor));
                        }
                    });
                });
        addGameObject(this.adventureButton);

        // Boton del modo juego rapido
        this.quickPlayButton = new TextButton(new Vector(this.worldWidth / 2f, 3f * this.worldHeight / 5f),
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, BUTTON_BASE_COLOR, BUTTON_OVER_COLOR,
                "Quick play", BUTTON_FONT, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        if (this.gameManager != null) {
                            this.gameManager.changeToQuickPlay();
                        }
                    });
                });
        addGameObject(this.quickPlayButton);

        // Boton de la tienda
        this.shopButton = new TextButton(new Vector(this.worldWidth / 2f, 3.8f * this.worldHeight / 5f),
                BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_ARC, SHOP_BUTTON_BASE_COLOR, SHOP_BUTTON_OVER_COLOR,
                "Shop", BUTTON_FONT, SHOP_FONT_COLOR, true, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia a la escena de juego
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        if (this.gameManager != null) {
                            this.gameManager.changeScene(new ShopScene(this.engine, this.UIColor));
                        }
                    });
                });
        addGameObject(this.shopButton);

        setUIColor(this.UIColor);
    }

    public TitleScene(IEngine engine) {
        this(engine, null);
    }

    // Establece segun la tematica el color de los botones
    @Override
    public void setUIColor(Color color) {
        super.setUIColor(color);

        if (this.adventureButton != null) {
            this.adventureButton.setBaseColor(this.UIColor, BUTTON_BASE_COLOR);
            this.adventureButton.setPointerOverColor(this.UIColor, BUTTON_OVER_COLOR);
        }
        if (this.quickPlayButton != null) {
            this.quickPlayButton.setBaseColor(this.UIColor, BUTTON_BASE_COLOR);
            this.quickPlayButton.setPointerOverColor(this.UIColor, BUTTON_OVER_COLOR);
        }
        if (this.shopButton != null) {
            this.shopButton.setFontColor(this.UIColor, SHOP_FONT_COLOR);
        }
    }
}
