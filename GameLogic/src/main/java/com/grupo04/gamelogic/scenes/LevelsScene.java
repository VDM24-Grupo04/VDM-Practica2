package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.gameobjects.LevelsListview;
import com.grupo04.gamelogic.gameobjects.buttons.ImageButton;
import com.grupo04.gamelogic.gameobjects.Text;

import org.json.JSONObject;

import java.io.InputStream;

public class LevelsScene extends Scene {
    private final Color BG_COLOR = new Color(255, 255, 255);

    private final int HEADER_SIZE = 40;
    private final int HEADER_OFFSET = 20;
    private final int HEADER_REAL_SIZE = HEADER_SIZE + HEADER_OFFSET;

    private final String FONT_NAME = "kimberley.ttf";

    private final String BUTTON_SOUND = "button.wav";
    private final String MENU_BUTTON_IMG = "close.png";
    private final int MENU_BUTTON_SIZE = HEADER_SIZE;

    private final Color TITLE_COLOR = new Color(0, 0, 0);
    private final int TITLE_SIZE = HEADER_SIZE;

    final int NUM_WORLDS = 3;
    final int LEVELS_PER_WORLD = 4;

    public LevelsScene(IEngine engine) {
        super(engine, 400, 600);

        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);
    }

    private void addLevelButton(LevelsListview listview, int world, int level) {
        int levelNumber = world * LEVELS_PER_WORLD + (level + 1);
        listview.addLevelButton(levelNumber, () -> {
            gameManager.changeToGameScene(world + 1, level + 1);
        });
    }

    @Override
    public void init() {
        IGraphics graphics = this.getEngine().getGraphics();
        graphics.setClearColor(new Color(0, 255, 0));

        Color[] worldColorsUnlocked = new Color[NUM_WORLDS];
        Color[] worldColorsPointoverUnlocked = new Color[NUM_WORLDS];
        Color[] worldColorsLocked = new Color[NUM_WORLDS];

        for (int i = 0; i < NUM_WORLDS; i++) {
            String worldStyleFileName = "levels/world" + (i + 1) + "/style.json";
            InputStream styleFile = this.engine.getFileInputStream(worldStyleFileName, IEngine.FileType.GAME_DATA);

            if (styleFile != null) {
                String styleStr = this.engine.readFile(styleFile);
                if (styleStr != null) {
                    JSONObject style = new JSONObject(styleStr);
                    JSONObject colorUnlocked = style.getJSONObject("colorUnlocked");
                    JSONObject colorLocked = style.getJSONObject("colorLocked");
                    JSONObject pointoverUnlocked = style.getJSONObject("pointoverUnlocked");

                    if (colorUnlocked != null && colorLocked != null) {
                        Color unlocked = new Color(colorUnlocked.getInt("r"), colorUnlocked.getInt("g"), colorUnlocked.getInt("b"));
                        Color pointover = new Color(pointoverUnlocked.getInt("r"), pointoverUnlocked.getInt("g"), pointoverUnlocked.getInt("b"));
                        Color locked = new Color(colorLocked.getInt("r"), colorLocked.getInt("g"), colorLocked.getInt("b"));

                        worldColorsUnlocked[i] = unlocked;
                        worldColorsPointoverUnlocked[i] = pointover;
                        worldColorsLocked[i] = locked;
                    }
                }
            }
        }

        float height = this.worldHeight - HEADER_REAL_SIZE;
        float y = HEADER_REAL_SIZE + height / 2f;
        float maskHeight = HEADER_REAL_SIZE;
        LevelsListview listview = new LevelsListview(new Vector(this.worldWidth / 2f, y),
                this.worldWidth, height, BG_COLOR,
                maskHeight, maskHeight, 3, 20);
        addGameObject(listview);

        for (int i = 0; i < NUM_WORLDS; ++i) {
            for (int j = 0; j < LEVELS_PER_WORLD; ++j) {
                this.addLevelButton(listview, i, j);
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
                "Adventure", FONT_NAME, TITLE_SIZE, false, false, TITLE_COLOR);
        addGameObject(title);

        super.init();
    }
}
