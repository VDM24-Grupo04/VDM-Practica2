package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.gameobjects.buttons.ImageButton;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.gameobjects.buttons.LevelButton;

import org.json.JSONObject;

import java.io.InputStream;

public class LevelsScene extends Scene {
    private final String FONT_NAME = "kimberley.ttf";
    private final Color TEXT_COLOR = new Color(0, 0, 0);

    private final int HEADER_SIZE = 40;
    private final int HEADER_OFFSET = 20;

    private final String BUTTON_SOUND = "button.wav";
    private final float BUTTON_ARC = 45f;
    private final String MENU_BUTTON_IMG = "close.png";

    private float itemSize;        // Se anade el boton de volver al menu inicial
    private final int ITEMS_PER_ROW = 4, ITEM_OFFSET = 10;

    final int NUM_LEVELS = 15;
    final int LEVELS_PER_WORLD = 5;
    final int NUM_WORLDS = NUM_LEVELS / LEVELS_PER_WORLD;

    Color[] worldColorsUnlocked;
    Color[] worldColorsLocked;
    private int levelsCount = 0;

    public LevelsScene(IEngine engine) {
        super(engine, 400, 600, new Color(255, 255, 255));

        // UI de la parte superior
        ImageButton menuButton = new ImageButton(
                new Vector(this.HEADER_SIZE / 2f + this.HEADER_OFFSET, this.HEADER_OFFSET + this.HEADER_SIZE / 2.0f),
                this.HEADER_SIZE, this.HEADER_SIZE, MENU_BUTTON_IMG, this.BUTTON_SOUND,
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
                new Vector(this.worldWidth / 2f, this.HEADER_OFFSET + this.HEADER_SIZE / 2.0f),
                "Adventure", this.FONT_NAME, this.HEADER_SIZE, false, false, this.TEXT_COLOR);
        addGameObject(title);

        // Se calcula el tamano de los objetos dependiendo del numero de objetos por fila
        float freeSpace = this.worldWidth - this.HEADER_OFFSET * 2 - this.ITEM_OFFSET * (this.ITEMS_PER_ROW - 1);
        this.itemSize = freeSpace / this.ITEMS_PER_ROW;


        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);
    }

    @Override
    public void init() {
        Color[] worldColorsUnlocked = new Color[NUM_WORLDS];
        Color[] worldColorsLocked = new Color[NUM_WORLDS];

        for (int i = 0; i < NUM_WORLDS; i++) {
            String worldStyleFileName = "/levels/world" + ((Integer) (i + 1)).toString() + "style.json";
            InputStream styleFile = this.engine.getFileInputStream(worldStyleFileName, IEngine.FileType.PROGRESS_DATA);

            if (styleFile != null) {
                String styleStr = this.engine.readFile(styleFile);
                if (styleStr != null) {
                    JSONObject style = new JSONObject(styleStr);
                    if (style != null) {
                        JSONObject colorUnlocked = style.getJSONObject("colorUnlocked");
                        JSONObject colorLocked = style.getJSONObject("colorLocked");

                        if (colorUnlocked != null && colorLocked != null) {
                            Color unlocked = new Color((int)colorUnlocked.get("r"), (int)colorUnlocked.get("g"), (int)colorUnlocked.get("b"), (int)colorUnlocked.get("a"));
                            Color locked = new Color((int)colorLocked.get("r"), (int)colorLocked.get("g"), (int)colorLocked.get("b"), (int)colorLocked.get("a"));

                            worldColorsUnlocked[i] = unlocked;
                            worldColorsLocked[i] = locked;
                        }
                    }
                }
            }
        }

        // Anadir los niveles
        Color BUTTON_BASE_COLOR = new Color(237, 12, 46);
        Color BUTTON_OVER_COLOR = new Color(203, 10, 38);
        for (int i = 0; i < this.NUM_LEVELS; i++) {
            addLevel(BUTTON_BASE_COLOR, BUTTON_OVER_COLOR);
        }

        super.init();
    }

    // Anade un objeto a la lista
    private void addLevel(Color baseColor, Color pointerOverColor) {
        // Calcula su posicion dependiendo del numero de objetos que haya en la lista antes de anadirlo
        float x = (this.levelsCount % this.ITEMS_PER_ROW) * (this.itemSize + this.ITEM_OFFSET) + this.HEADER_OFFSET + this.itemSize / 2;
        float y = (this.levelsCount / this.ITEMS_PER_ROW) * (this.itemSize + this.ITEM_OFFSET) + this.HEADER_SIZE * 2.3f + this.itemSize / 2;

        // Anade un objeto a la lista
        int world = this.levelsCount / this.LEVELS_PER_WORLD;
        int level = this.levelsCount % this.LEVELS_PER_WORLD;

        // Crea el boton del nivel
        LevelButton lvl = new LevelButton(new Vector(x, y), this.itemSize, this.itemSize, this.BUTTON_ARC,
                baseColor, pointerOverColor, ((Integer)(this.levelsCount + 1)).toString(), this.FONT_NAME, this.BUTTON_SOUND);

        lvl.setOnClick(() -> {
            this.gameManager.changeToGameScene(world + 1, level + 1);
        });
        this.levelsCount++;
        addGameObject(lvl);
    }

}
