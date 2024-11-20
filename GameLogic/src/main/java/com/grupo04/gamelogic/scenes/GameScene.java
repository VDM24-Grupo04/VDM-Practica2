package com.grupo04.gamelogic.scenes;

import com.grupo04.gamelogic.GameManager;
import com.grupo04.gamelogic.Scene;
import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.BubbleColors;
import com.grupo04.gamelogic.gameobjects.CurrentBubble;
import com.grupo04.gamelogic.gameobjects.Grid;
import com.grupo04.gamelogic.gameobjects.ImageButton;
import com.grupo04.gamelogic.gameobjects.ImageToggleButton;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.gameobjects.Walls;

import org.json.JSONObject;

public class GameScene extends Scene {
    private final int id;
    private final JSONObject jsonObject;
    private final Grid grid;
    private final CurrentBubble currentBubble;
    boolean checkEnded;

    public GameScene(IEngine engine, JSONObject jsonObject, int id) {
        super(engine, 400, 600, "background.jpg");

        this.jsonObject = jsonObject;
        this.id = id;

        int n_COLS = 10;
        int INIT_ROWS = 5;
        int HEADER_WIDTH = 50;
        int WALL_THICKNESS = 20;

        int BUBBLES_TO_EXPLODE = 3;
        int GREAT_SCORE = 10;
        int SMALL_SCORE = 5;

        String BUTTON_SOUND = "button.wav";
        int SIDE_BUTTONS_PADDING = 10;
        int SIDE_BUTTONS_SIZE = 40;

        String MENU_BUTTON_IMG = "close.png";

        String SHOW_GRID_BUTTON_UNCHECKED_IMG = "hex_empty.png";
        String SHOW_GRID_BUTTON_CHECKED_IMG = "hex_full.png";

        Color TEXT_COLOR = new Color(0, 0, 0);
        String SCORE_TEXT_FONT = "kimberley.ttf";
        float SCORE_TEXT_SIZE = 35;

        this.checkEnded = true;

        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);

        // Radio de las burbujas en el mapa
        float r = (((float) this.worldWidth - (WALL_THICKNESS * 2)) / n_COLS) / 2;
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
                        TitleScene scene = new TitleScene(this.engine);
                        scene.setFade(Fade.OUT, 0.25);
                        if (this.gameManager != null) {
                            this.gameManager.changeScene(scene);
                        }
                    });
                });
        addGameObject(menuButton);

        String text = "Score: 0";
        if (this.jsonObject != null && this.jsonObject.has("score")) {
            text = "Score: " + this.jsonObject.get("score");
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

        this.grid = new Grid(jsonObject, this.worldWidth, WALL_THICKNESS, HEADER_WIDTH, (int) r, bubbleOffset, rows, n_COLS,
                INIT_ROWS, BUBBLES_TO_EXPLODE, GREAT_SCORE, SMALL_SCORE, bubbleColors);
        addGameObject(grid, "grid");

        this.currentBubble = new CurrentBubble(jsonObject, this.worldWidth, WALL_THICKNESS, HEADER_WIDTH,
                (int) r, bubbleOffset, rows, bubbleColors);
        addGameObject(currentBubble);
    }

    @Override
    public void init() {
        super.init();

        // Se cambia el color de fondo si hay uno seleccionado
        Color bgColor = gameManager.getBgColor();
        if (bgColor != null) {
            super.bgImage = null;
            this.engine.getGraphics().setClearColor(bgColor);
        }

    }

    @Override
    public void dereference() {
        // Al salir de la escena reinicia el color del fondo para ponerlo en blanco
        this.engine.getGraphics().setClearColor(new Color(255, 255, 255));
        super.dereference();
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        if (this.checkEnded && this.grid.hasEnded()) {
            if (this.grid.hasWon()) {
                // Se hace un fade in y cuando acaba la animacion se cambia a la escena de victoria
                this.setFade(Scene.Fade.IN, 0.25);
                this.setFadeCallback(() -> {
                    GameManager gameManager = this.getGameManager();
                    if (gameManager != null) {
                        gameManager.changeScene(new VictoryScene(this.engine, this.grid.getScore(), this.jsonObject, this.id));
                    }
                });
            }
            else {
                // Se hace un fade in y cuando acaba la animacion se cambia a la escena de game over
                this.setFade(Scene.Fade.IN, 0.25);
                this.setFadeCallback(() -> {
                    GameManager gameManager = this.getGameManager();
                    if (gameManager != null) {
                        gameManager.changeScene(new GameOverScene(this.engine, this.jsonObject, this.id));
                    }
                });
            }

            this.checkEnded = false;
        }
    }

    @Override
    public void shutdown() {
        // Si es un nivel
        if (this.id != 0) {
            this.gameManager.setLastLevel(this.id);
            this.gameManager.setAdventureGrid(this.grid.getBubbles());
            this.gameManager.setAdventureBubbleColors(this.currentBubble.getAdventureModeColors());
            this.gameManager.setAdventureScore(this.grid.getScore());
        }
        // Si es modo Juego Rapido
        else {
            this.gameManager.setQuickPlayGrid(this.grid.getBubbles());
            this.gameManager.setQuickPlayBubbleColor(this.currentBubble.getColor());
            this.gameManager.setQuickPlayScore(this.grid.getScore());
        }
    }
}
