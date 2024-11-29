package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.listview.VerticalListview;
import com.grupo04.gamelogic.gameobjects.buttons.ImageButton;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.listview.LevelButton;

public class LevelsScene extends Scene {
    private final Color BG_COLOR = new Color(255, 255, 255);

    private final int HEADER_SIZE = 40;
    private final int HEADER_OFFSET = 20;
    private final int HEADER_REAL_SIZE = HEADER_SIZE + HEADER_OFFSET;

    private final String FONT_NAME = "kimberley.ttf";

    private final String BUTTON_SOUND = "button.wav";
    private final Color FONT_COLOR = new Color(0, 0, 0);

    private final String MENU_BUTTON_IMG = "close.png";
    private final int MENU_BUTTON_SIZE = HEADER_SIZE;

    private final float LEVEL_BUTTON_ARC = 80f;
    private final float LEVEL_BUTTON_BORDER_WIDTH = 4.0f;
    private final Color LEVEL_BUTTON_BORDER_COLOR = new Color(0, 0, 0);
    private final String LEVEL_BUTTON_FONT_NAME = "kimberley.ttf";
    private final String LEVEL_BUTTON_IMAGE = "padlockIcon.png";

    private final int TITLE_SIZE = HEADER_SIZE;

    public LevelsScene(IEngine engine) {
        super(engine, 400, 600);

        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);
    }

    private void addLevelButton(VerticalListview listview, int world, int level, int levelProgress, Color[] cols) {
        int levelsPerWorld = this.gameManager.getLevelsPerWorld();
        int levelNumber = world * levelsPerWorld + (level + 1);
        boolean locked = levelNumber > levelProgress;
        LevelButton levelButton = new LevelButton(levelNumber, locked, cols,
                LEVEL_BUTTON_BORDER_COLOR, LEVEL_BUTTON_ARC, LEVEL_BUTTON_BORDER_WIDTH,
                LEVEL_BUTTON_FONT_NAME, false, FONT_COLOR,
                LEVEL_BUTTON_IMAGE,
                BUTTON_SOUND, () -> {
            gameManager.changeToGameScene(levelNumber);
        });
        listview.addButton(levelButton);
    }

    @Override
    public void init() {
        IGraphics graphics = this.getEngine().getGraphics();
        graphics.setClearColor(BG_COLOR);

        Color[][] styles = this.gameManager.getLevelsStyle();

        float height = this.worldHeight - HEADER_REAL_SIZE;
        float y = HEADER_REAL_SIZE + height / 2f;
        float maskHeight = HEADER_REAL_SIZE * 2f;
        VerticalListview listview = new VerticalListview(new Vector(this.worldWidth / 2f, y),
                this.worldWidth, height, BG_COLOR,
                maskHeight, maskHeight, 3, 20);
        addGameObject(listview);

        int levelProgress = this.gameManager.getLevelProgress();

        int nWorlds = gameManager.getNWorlds();
        int levelsPerWorld = gameManager.getLevelsPerWorld();

        for (int i = 0; i < nWorlds; ++i) {
            for (int j = 0; j < levelsPerWorld; ++j) {
                this.addLevelButton(listview, i, j, levelProgress, styles[i]);
            }
        }

        // UI de la parte superior
        ImageButton menuButton = new ImageButton(
                new Vector(HEADER_OFFSET + MENU_BUTTON_SIZE / 2f, HEADER_OFFSET + MENU_BUTTON_SIZE / 2f),
                MENU_BUTTON_SIZE, MENU_BUTTON_SIZE, MENU_BUTTON_IMG, BUTTON_SOUND,
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
                new Vector(this.worldWidth / 2f, HEADER_OFFSET + TITLE_SIZE / 2.0f),
                "Adventure", FONT_NAME, TITLE_SIZE, false, false, FONT_COLOR);
        addGameObject(title);

        super.init();
    }
}
