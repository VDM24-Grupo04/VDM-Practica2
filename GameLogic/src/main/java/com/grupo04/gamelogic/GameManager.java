package com.grupo04.gamelogic;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.gamelogic.scenes.GameOverScene;
import com.grupo04.gamelogic.scenes.GameScene;
import com.grupo04.gamelogic.scenes.TitleScene;
import com.grupo04.gamelogic.scenes.VictoryScene;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Stack;

public class GameManager implements IScene {
    // Escenas
    private final IEngine engine;
    private final Stack<Scene> scenes;
    private final Stack<Scene> aliveScenes;

    // Todos los datos del juego
//    public enum SCENES { TITLE_SCENE, VICTORY_SCENE, GAMEOVER_SCENE, GAME_SCENE }; // DUDOSO
    private final String fileName;

    private int lastScene;
    private int lastScore; // Usaremos lastScore para el numero tanto en GameScene como en VictoryScene
//    private int coins;
//    private int numUnlockedLevels;
//    private int lastPlayingLevel; // Para el ultimo nivel jugado a medias, si se juega otro se omite
//    ...

    public GameManager(IEngine engine, String fileName) {
        this.engine = engine;
        this.scenes = new Stack<>();
        this.aliveScenes = new Stack<>();
        this.fileName = fileName;

        this.lastScene = -1;
        this.lastScore = 0;
        // ...

        // Si eso moverlo a un init()
        readInfo();

        // Inicializar la primera escena a partir del "lastScene"
        // No tiene mucho sentido asignar un id a no ser que sea un enum... ???
        switch (this.lastScene) {
            default:        // Para que inicie en PC
            case -1:
                TitleScene titleScene = new TitleScene(this.engine);
                titleScene.setId(-1);
                pushScene(titleScene);
                break;
            case -2:
                GameScene gameScene = new GameScene(this.engine);
                gameScene.setId(-2);
                gameScene.setScore(this.lastScore);
                pushScene(gameScene);
                break;
            case -3:
                VictoryScene victoryScene = new VictoryScene(this.engine, this.lastScore);
                victoryScene.setId(-3);
                pushScene(victoryScene);
                break;
            case -4:
                GameOverScene gameOverScene = new GameOverScene(this.engine);
                gameOverScene.setId(-4);
                pushScene(gameOverScene);
                break;
            // case ...: pushScene(new ShopScene(...)); break;
            // case ...: pushScene(new LevelsScene(...)); break;
            // durante un nivel

        }
    }

    public void popScene() {
        if (!this.scenes.empty()) {
            this.scenes.peek().setAlive(false);
        }
    }

    public void pushScene(Scene newScene) {
        this.scenes.push(newScene);
        newScene.setGameManager(this);
        newScene.init();
    }

    public void changeScene(Scene newScene) {
        if (!this.scenes.empty()) {
            // Si la escena que se quiere insertar no es la misma que la activa...
            if (this.scenes.peek() != newScene) {
                this.scenes.peek().setAlive(false);
                // Se inserta la nueva escena
                this.scenes.push(newScene);
                newScene.setGameManager(this);
                newScene.init();
            }
        }
    }

    @Override
    public void handleInput(List<ITouchEvent> touchEvents) {
        if (!this.scenes.empty()) {
            this.scenes.peek().handleInput(touchEvents);
        }
    }

    @Override
    public void update(double deltaTime) {
        if (!this.scenes.empty()) {
            this.scenes.peek().update(deltaTime);
        }
    }

    @Override
    public void fixedUpdate(double fixedDeltaTime) {
        if (!this.scenes.empty()) {
            this.scenes.peek().fixedUpdate(fixedDeltaTime);
        }
    }

    @Override
    public void render(IGraphics graphics) {
        if (!this.scenes.empty()) {
            this.scenes.peek().render(graphics);
        }
    }

    @Override
    public void refresh() {
        if (!this.scenes.empty()) {
            this.scenes.peek().refresh();

            boolean hasDeadScenes = false;

            while (!this.scenes.empty()) {
                Scene scene = this.scenes.peek();
                if (!scene.isAlive()) {
                    scene.dereference();
                    hasDeadScenes = true;
                } else {
                    this.aliveScenes.push(scene);
                }
                this.scenes.pop();
            }

            while (!this.aliveScenes.empty()) {
                Scene scene = this.aliveScenes.peek();
                this.scenes.push(scene);
                this.aliveScenes.pop();
            }

            // Si se ha eliminado una escena, quiere decir que se vuelve a la anterior y, por lo tanto,
            // hay que actualizar el tam del mundo
            if (!this.scenes.empty() && hasDeadScenes) {
                this.aliveScenes.clear();
                Scene currentScene = this.scenes.peek();
                this.engine.setWorldSize(currentScene.getWorldWidth(), currentScene.getWorldHeight());
            }
        }
    }

    private void assignReadInfo(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                this.lastScene = (int)jsonObject.get("lastScene");
                this.lastScore = (int)jsonObject.get("lastScore");
                // ...
            } catch (Exception e) {
                System.err.println("Error while reading info");
            }
        }
    }

    public void readInfo() {
        FileInputStream file = this.engine.getFileInputStream(this.fileName);
        JSONObject jsonObject = this.engine.readFile(file);
        assignReadInfo(jsonObject);
    }

    // Habria tambien que asignar la informacion cada vez que se modificase
    // el numero de monedas, la ultima escena a medias, etc.
    // Para el numero de monedas cada vez que se incrementase o se comprase algo en la tienda
    // Para la ultima escena a medias, si se ha salido cuando estaba en un nivel o al salir del juego
    // ...
    private void assignWriteInfo() {
        Scene scene = this.scenes.peek();
        this.lastScene = scene.getId();
        switch (this.lastScene) {
            case -2:
                GameScene gameScene = (GameScene) scene;
                this.lastScore = gameScene.getScore();
                break;
            case -3:
                VictoryScene victoryScene = (VictoryScene) scene;
                this.lastScore = victoryScene.getScore();
                break;
//                case ...:
//                    ShopScene shopScene = (ShopScene) scene;
//                    this.coins = shopScene.getCoins();
//                break;
//                case ...:
//                    LevelsScene levelsScene = (LevelsScene) scene;
//                    this.numUnlockedLevels = scene.getNumUnlockedLevels();
//                    ...
//                    break;
            // default: break; // durante un nivel
        }
    }

    public void writeInfo() {
        try {
            assignWriteInfo();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lastScene", this.lastScene);
            jsonObject.put("lastScore", this.lastScore);
            // ...

            FileOutputStream file = this.engine.getFileOutputStream(this.fileName);
            this.engine.writeFile(file, jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
