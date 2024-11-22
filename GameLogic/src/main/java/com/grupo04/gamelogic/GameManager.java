package com.grupo04.gamelogic;

import static com.grupo04.engine.utilities.JSONConverter.convertLinkedListToJSONArray;
import static com.grupo04.engine.utilities.JSONConverter.convertMatrixToJSONArray;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.scenes.TitleScene;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class GameManager implements IScene {
    // Escenas
    private final IEngine engine;
    private final Stack<Scene> scenes;
    private final Stack<Scene> aliveScenes;

    // Todos los datos del juego
    private JSONObject mainJsonObject;
    private JSONObject adventureJsonObject;
    private JSONObject quickPlayJsonObject;
    private final String shopFileName;
    private JSONObject shopJsonObject;
    private JSONObject playerShopJsonObject;
    private final String fileName;

    private int coins;

    // Para el ultimo nivel jugado a medias, si se juega otro se omite
    private int lastLevel;
    private int[][] adventureGrid;
    private LinkedList<Integer> adventureBubbleColors;
    private int adventureScore;
    private int[][] quickPlayGrid;
    private int quickPlayBubbleColor;
    private int quickPlayScore;

    // Cosmeticos
    private Color bgColor;
    private IImage[] activeSkins;

    public GameManager(IEngine engine, String fileName, String shopFileName) {
        this.engine = engine;
        this.scenes = new Stack<>();
        this.aliveScenes = new Stack<>();
        this.fileName = fileName;
        this.shopFileName = shopFileName;

        // Inicializar las variables como los valores
        // sin que haya guardado ningun dato
        this.coins = 0;
        this.lastLevel = 1;
        this.adventureGrid = null;
        this.adventureBubbleColors = null;
        this.adventureScore = 0;
        this.quickPlayGrid = null;
        this.quickPlayBubbleColor = -1;
        this.quickPlayScore = 0;

        bgColor = null;
        activeSkins = new IImage[BubbleColors.getTotalColors()];
        Arrays.fill(activeSkins, null);
    }

    @Override
    public void init() {
        readInfo();
        applyShopProgress();
        pushScene(new TitleScene(this.engine));
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
                    // Cada vez que se vaya a quitar una escena de la pila
                    // se llama a su shutdown para asignar los valores a medias
                    // en caso de ser GameScene o ShopScene
                    scene.shutdown();
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


    public void readInfo() {
        InputStream file = this.engine.getFileInputStream(this.fileName, IEngine.FileType.PROGRESS_DATA);
        this.mainJsonObject = this.engine.readFile(file);

        InputStream shopFile = this.engine.getFileInputStream(this.shopFileName, IEngine.FileType.GAME_DATA);
        this.shopJsonObject = this.engine.readFile(shopFile);

        if (this.mainJsonObject != null) {
            this.adventureJsonObject = this.mainJsonObject.getJSONObject("adventure");
            this.quickPlayJsonObject = this.mainJsonObject.getJSONObject("quickPlay");

            try {
                // Leer todas las variables comunes del jsonObject y asignar
                this.coins = (int) this.mainJsonObject.get("coins");
                this.lastLevel = (int) this.mainJsonObject.get("lastLevel");
                // ...
                // Para el resto de variables como grid o bubble color se encargan
                // los objetos Grid y CurrentBubble

                // Obtiene la parte de la tienda del archivo de guardado
                this.playerShopJsonObject = this.mainJsonObject.getJSONObject("shop");
            }
            catch (Exception e) {
                System.err.println("Error while reading info");
            }
        }
        else {
            this.mainJsonObject = new JSONObject();
            this.adventureJsonObject = new JSONObject();
            this.quickPlayJsonObject = new JSONObject();
        }
    }

    public void writeInfo() {
        try {
            // Guardar todas las variables y escribir
            this.mainJsonObject.keySet().clear();
            // Variables comunes
            this.mainJsonObject.put("coins", this.coins);
            this.mainJsonObject.put("lastLevel", this.lastLevel);

            if (this.adventureGrid != null) {
                this.adventureJsonObject.put("grid", convertMatrixToJSONArray(this.adventureGrid));
            }
            if (this.adventureBubbleColors != null) {
                this.adventureJsonObject.put("colors", convertLinkedListToJSONArray(this.adventureBubbleColors));
            }
            this.adventureJsonObject.put("score", this.adventureScore);
            if (this.quickPlayGrid != null && this.quickPlayBubbleColor != -1) {
                this.quickPlayJsonObject.put("grid", convertMatrixToJSONArray(this.quickPlayGrid));
                this.quickPlayJsonObject.put("color", this.quickPlayBubbleColor);
            }
            this.quickPlayJsonObject.put("score", this.quickPlayScore);
            // ...

            this.mainJsonObject.put("adventure", this.adventureJsonObject);
            this.mainJsonObject.put("quickPlay", this.quickPlayJsonObject);

            FileOutputStream file = this.engine.getFileOutputStream(this.fileName);
            this.engine.writeFile(file, this.mainJsonObject);
        } catch (JSONException e) {
            System.err.println("Error while writing info to system: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        for (Scene scene : this.scenes) {
            // De normal debería haber solo una escena en la pila de escenas
            // pero si hay mas, se deberia considerar recorrerla de final a principio
            // y no asi porque cuando mas arriba en la pila, mas reciente es la escena
            scene.shutdown();
        }

        this.writeInfo();
    }

    public JSONObject getAdventureJSONObj() {
        return this.adventureJsonObject;
    }

    public JSONObject getQuickPlayJSONObj() {
        return this.quickPlayJsonObject;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getCoins() {
        return this.coins;
    }

    public void setLastLevel(int lastLevel) {
        this.lastLevel = lastLevel;
    }

    public int getLastLevel() {
        return this.lastLevel;
    }

    public void setAdventureGrid(int[][] grid) {
        this.adventureGrid = grid;
    }

    public void setAdventureBubbleColors(LinkedList<Integer> colors) {
        this.adventureBubbleColors = colors;
    }

    public void setAdventureScore(int score) {
        this.adventureScore = score;
    }

    public void setQuickPlayGrid(int[][] grid) {
        this.quickPlayGrid = grid;
    }

    public void setQuickPlayBubbleColor(int color) {
        this.quickPlayBubbleColor = color;
    }

    public void setQuickPlayScore(int score) {
        this.quickPlayScore = score;
    }
    // ...

    public JSONObject getShopJsonObject() {
        return this.shopJsonObject;
    }

    public JSONObject getSavedShopJsonObject() {
        return this.playerShopJsonObject;
    }

    public void setBgColor(Color c) {
        this.bgColor = c;
    }
    public Color getBgColor() {
        return this.bgColor;
    }
    public void setBallSkin(int i, IImage img) {
        activeSkins[i] = img;
    }
    public IImage getBallSkin(int i) {
        return activeSkins[i];
    }


    private void applyShopProgress() {
        // Si se ha leido el archivo de la tienda y hay progreso de la tienda guardado
        if (this.shopJsonObject != null && this.playerShopJsonObject != null) {
            JSONObject allItems = this.shopJsonObject.getJSONObject("items");

            // Recorre todos los objetos guardados
            Iterator<String> savedItems = this.playerShopJsonObject.keys();
            while (savedItems.hasNext()) {
                String key = savedItems.next();

                // Obtiene los atributos del objeto con la key actual y
                // si no son nulos y el objeto existe en la tienda
                JSONObject savedItem = this.playerShopJsonObject.getJSONObject(key);
                if (savedItem != null && allItems.has(key)) {
                    // Si el objeto leido tiene el atributo active y
                    // el objeto esta activo, se selecciona
                    if (savedItem.get("active") != null) {
                        if ((Boolean) savedItem.get("active")) {
                            JSONObject shopItem = allItems.getJSONObject(key);

                            if(shopItem.get("type") != null) {
                                if (shopItem.get("type") == "bgColor") {
                                    this.bgColor = new Color((int) shopItem.get("r"), (int) shopItem.get("g"), (int) shopItem.get("b"), (int) shopItem.get("a"));
                                }
                                else if (shopItem.get("type") == "bgColor") {
                                    setBallSkin((int)shopItem.get("colorId"), engine.getGraphics().newImage((String) shopItem.get("path")));
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}