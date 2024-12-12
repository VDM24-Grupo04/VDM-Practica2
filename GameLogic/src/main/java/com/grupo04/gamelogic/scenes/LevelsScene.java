package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.listview.VerticalListview;
import com.grupo04.gamelogic.gameobjects.buttons.ImageButton;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.listview.LevelButton;

public class LevelsScene extends Scene {
    private final int HEADER_SIZE = 40;
    private final int HEADER_OFFSET = 20;
    private final int HEADER_REAL_SIZE = HEADER_SIZE + HEADER_OFFSET;

    private final int LEVELS_PER_ROW = 3;

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

    @Override
    public void init() {
        int totalLevels = gameManager.getTotalLevels();
        int nRows = (int) Math.ceil((double) totalLevels / LEVELS_PER_ROW);

        float height = this.worldHeight - HEADER_REAL_SIZE;
        float y = HEADER_REAL_SIZE + height / 1.95f;
        float maskHeight = HEADER_REAL_SIZE * nRows * 2f;

        VerticalListview listview = new VerticalListview(new Vector(this.worldWidth / 2f, y),
                this.worldWidth, height, maskHeight, maskHeight, LEVELS_PER_ROW, 20, 20);
        addGameObject(listview);

        int levelProgress = this.gameManager.getLevelProgress();

        int[] worlds = gameManager.getWorlds();

        Color[][] levelsStyle = this.gameManager.getLevelsStyle();
        float fontSize = listview.getItemSize() / 1.8f;
        IFont levelFont = getEngine().getGraphics().newFont(LEVEL_BUTTON_FONT_NAME, fontSize, false, false);
        IImage levelImage = getEngine().getGraphics().newImage(LEVEL_BUTTON_IMAGE);
        ISound levelSound = getEngine().getAudio().newSound(BUTTON_SOUND);

        int k = 1;
        for (int i = 0; i < worlds.length; ++i) {
            for (int j = 0; j < worlds[i]; ++j) {
                this.addLevelButton(listview, k, levelProgress, levelFont, levelImage,
                        levelSound, levelsStyle[i]);
                ++k;
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

    private void addLevelButton(VerticalListview listview, int levelNumber, int levelProgress,
                                IFont font, IImage image, ISound onClickSound, Color[] style) {
        boolean locked = levelNumber > levelProgress;

        LevelButton levelButton = new LevelButton(levelNumber, locked, style,
                LEVEL_BUTTON_BORDER_COLOR, LEVEL_BUTTON_ARC, LEVEL_BUTTON_BORDER_WIDTH,
                FONT_COLOR, font, image, onClickSound, () -> {
            gameManager.changeToGameScene(levelNumber);
        });

        listview.addButton(levelButton);
    }

}
