package com.grupo04.gamelogic.scenes;

import static com.grupo04.engine.utilities.JSONConverter.convertLinkedListToJSONArray;
import static com.grupo04.engine.utilities.JSONConverter.convertMatrixToJSONArray;

import com.grupo04.gamelogic.GameManager;
import com.grupo04.gamelogic.Scene;
import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.BubbleColors;
import com.grupo04.gamelogic.gameobjects.CurrentBubble;
import com.grupo04.gamelogic.gameobjects.Grid;
import com.grupo04.gamelogic.gameobjects.buttons.ImageButton;
import com.grupo04.gamelogic.gameobjects.buttons.ImageToggleButton;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.gameobjects.Walls;

import org.json.JSONObject;

import java.util.LinkedList;

public class GameScene extends Scene {
    private final int NUM_COLS = 10;
    private final int INIT_ROWS = 5;
    private final int HEADER_WIDTH = 50;
    private final int WALL_THICKNESS = 20;

    private final int BUBBLES_TO_EXPLODE = 3;
    private final int GREAT_SCORE = 10;
    private final int SMALL_SCORE = 5;

    private final String BUTTON_SOUND = "button.wav";
    private final int SIDE_BUTTONS_PADDING = 10;
    private final int SIDE_BUTTONS_SIZE = 40;

    private final String MENU_BUTTON_IMG = "close.png";

    private final String SHOW_GRID_BUTTON_UNCHECKED_IMG = "hex_empty.png";
    private final String SHOW_GRID_BUTTON_CHECKED_IMG = "hex_full.png";

    private final Color TEXT_COLOR = new Color(0, 0, 0);
    private final String SCORE_TEXT_FONT = "kimberley.ttf";
    private final float SCORE_TEXT_SIZE = 35;

    private final int levelNumber;
    private final Grid grid;
    private final CurrentBubble currentBubble;
    boolean checkEnded;

    public GameScene(IEngine engine, JSONObject json, int levelNumber, Color UIColor) {
        super(engine, 400, 600, UIColor);

        this.levelNumber = levelNumber;

        this.checkEnded = true;

        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);

        // Radio de las burbujas en el mapa
        float r = (((float) this.worldWidth - (WALL_THICKNESS * 2)) / NUM_COLS) / 2;
        int bubbleOffset = (int) (r * 0.3);
        int rows = ((int) ((this.worldHeight - HEADER_WIDTH - WALL_THICKNESS) / (r * 2)));

        // UI de la parte superior
        ImageButton menuButton = new ImageButton(new Vector(SIDE_BUTTONS_SIZE / 2f + SIDE_BUTTONS_PADDING, HEADER_WIDTH / 2f),
                SIDE_BUTTONS_SIZE, SIDE_BUTTONS_SIZE, MENU_BUTTON_IMG, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia al menu principal
                    // con animacion de fade out
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        Scene scene = null;
                        if (this.levelNumber == 0) {
                            scene = new TitleScene(this.engine, this.UIColor);
                        } else {
                            scene = new LevelsScene(this.engine, this.UIColor);
                        }
                        scene.setFade(Fade.OUT, 0.25);
                        if (this.gameManager != null) {
                            saveJson();
                            this.gameManager.changeScene(scene);
                        }
                    });
                });
        addGameObject(menuButton);

        String text = "Score: 0";
        if (json != null && json.has("score")) {
            text = "Score: " + json.get("score");
        }
        Text scoreText = new Text(new Vector(this.worldWidth / 2f, HEADER_WIDTH / 2f), text,
                SCORE_TEXT_FONT, SCORE_TEXT_SIZE, false, false, TEXT_COLOR);
        addGameObject(scoreText, "scoreText");

        ImageToggleButton showGridButton = new ImageToggleButton(
                new Vector(this.worldWidth - SIDE_BUTTONS_SIZE / 2f - SIDE_BUTTONS_PADDING, HEADER_WIDTH / 2f),
                SIDE_BUTTONS_SIZE, SIDE_BUTTONS_SIZE,
                SHOW_GRID_BUTTON_UNCHECKED_IMG, SHOW_GRID_BUTTON_CHECKED_IMG, BUTTON_SOUND);
        addGameObject(showGridButton, "showGridButton");

        // Posibles colores de las bolas
        BubbleColors bubbleColors = new BubbleColors();

        // Elementos de juego
        Walls walls = new Walls(WALL_THICKNESS, HEADER_WIDTH, this.worldWidth, this.worldHeight);
        addGameObject(walls);

        this.grid = new Grid(json, this.worldWidth, WALL_THICKNESS, HEADER_WIDTH, (int) r, bubbleOffset, rows, NUM_COLS,
                INIT_ROWS, BUBBLES_TO_EXPLODE, GREAT_SCORE, SMALL_SCORE, bubbleColors);
        addGameObject(this.grid, "grid");

        this.currentBubble = new CurrentBubble(json, this.worldWidth, WALL_THICKNESS, HEADER_WIDTH,
                (int) r, bubbleOffset, rows, bubbleColors);
        addGameObject(this.currentBubble);
    }

    public GameScene(IEngine engine, JSONObject json, int levelNumber) {
        this(engine, json, levelNumber, null);
    }

    @Override
    public void init() {
        super.bgImage = this.engine.getGraphics().newImage(this.gameManager.getBgImage());
        super.init();
    }

    @Override
    public void dereference() {
        super.dereference();
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        if (this.grid.hasWon()) {
            this.currentBubble.setColor(-2);
        }

        if (this.checkEnded && (this.grid.hasEnded() || this.currentBubble.allBallsUsed())) {
            if (this.grid.hasWon()) {
                // Se hace un fade in y cuando acaba la animacion se cambia a la escena de victoria
                this.setFade(Scene.Fade.IN, 0.25);
                this.setFadeCallback(() -> {
                    GameManager gameManager = this.getGameManager();
                    if (gameManager != null) {
                        boolean firstTime = true;
                        if (this.levelNumber > 0) {
                            int levelProgress = gameManager.getLevelProgress();
                            firstTime = levelProgress == this.levelNumber;
                            gameManager.setLevelProgress(this.levelNumber + 1);
                        }
                        gameManager.changeScene(new VictoryScene(this.engine, this.grid.getScore(), this.levelNumber, firstTime, this.UIColor));
                    }
                });
            } else {
                // Se hace un fade in y cuando acaba la animacion se cambia a la escena de game over
                this.setFade(Scene.Fade.IN, 0.25);
                this.setFadeCallback(() -> {
                    GameManager gameManager = this.getGameManager();
                    if (gameManager != null) {
                        gameManager.changeScene(new GameOverScene(this.engine, this.levelNumber, this.UIColor));
                    }
                });
            }

            this.checkEnded = false;
        }
    }

    @Override
    public void saveJson() {
        JSONObject jsonObject = new JSONObject();

        // Si es el modo de juego rapido
        if (this.levelNumber == 0) {
            jsonObject.put("grid", convertMatrixToJSONArray(this.grid.getBubbles()));
            jsonObject.put("color", this.currentBubble.getColor());
            jsonObject.put("score", this.grid.getScore());
            this.gameManager.setQuickPlayJsonObject(jsonObject);
        }
        // Si es un nivel del modo aventura
        else {
            jsonObject.put("levelNumber", this.levelNumber);
            jsonObject.put("grid", convertMatrixToJSONArray(this.grid.getBubbles()));
            LinkedList<Integer> listAux = this.currentBubble.getAdventureModeColors();
            int lastColor = this.currentBubble.getColor();
            if (lastColor != -1) {
                listAux.push(lastColor);
            }
            jsonObject.put("colors", convertLinkedListToJSONArray(listAux));
            jsonObject.put("score", this.grid.getScore());
            this.gameManager.setAdventureJsonObject(jsonObject);
        }
    }
}
