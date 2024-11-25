package com.grupo04.gamelogic;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.scenes.GameScene;
import com.grupo04.gamelogic.scenes.TitleScene;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class GameManager implements IScene {
    // Escenas
    private final IEngine engine;
    private final Stack<Scene> scenes;
    private final Stack<Scene> aliveScenes;

    // Progresion del juego
    private final String progressFileName;
    private JSONObject progressJsonObject;

    // Tienda dirigida por datos
    private final String shopFileName;
    private List<String> shopItemsKeys;
    private HashMap<String, JSONObject> shopItemsByKey;

    // Cosmeticos
    private Color bgColor;
    private IImage[] activeSkins;

    public GameManager(IEngine engine, String fileName, String shopFileName) {
        this.engine = engine;
        this.scenes = new Stack<>();
        this.aliveScenes = new Stack<>();

        this.progressFileName = fileName;
        this.progressJsonObject = new JSONObject();
        this.progressJsonObject.put("coins", 0);
        this.progressJsonObject.put("shop", new JSONObject());
        this.progressJsonObject.put("quickPlay", new JSONObject());
        this.progressJsonObject.put("adventure", new JSONObject());

        this.shopFileName = shopFileName;

        // Inicializar cosmeticos
        shopItemsKeys = new ArrayList<>();
        shopItemsByKey = new HashMap<>();
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

    public void changeToGameScene(int levelNumber) {
        JSONObject json = null;
        // Progreso del modo de juego rapido
        if (levelNumber == 0) {
            json = this.getQuickPlayJSONObj();
            this.setQuickPlayJsonObject(new JSONObject());
        } else {
            json = this.getAdventureJSONObj();
            if(json != null) {
            int levelNumJson = json.getInt("levelNumber");
            this.setAdventureJsonObject(new JSONObject());
                if (levelNumber != levelNumJson) {
                    // TODO: Setear json con el nivel entero
                }
            }
            else {
                // TODO: Setear json con el nivel entero
            }
        }
        this.changeScene(new GameScene(this.engine, json, levelNumber));
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
                    // scene.shutdown();
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
        InputStream progressFile = this.engine.getFileInputStream(this.progressFileName, IEngine.FileType.PROGRESS_DATA);
        JSONObject json = this.engine.readFile(progressFile);
        if (json != null) {
            this.progressJsonObject = json;
        }

        InputStream shopFile = this.engine.getFileInputStream(this.shopFileName, IEngine.FileType.GAME_DATA);
        JSONObject shopJsonObj = this.engine.readFile(shopFile);
        if (shopJsonObj != null) {
            // Obtiene el array de objetos
            JSONArray objects = (JSONArray) shopJsonObj.get("items");
            // Si el array de objetos es valido
            if (objects != null) {
                // Recorre todos los objetos y los anade a la tienda
                for (int i = 0; i < objects.length(); i++) {
                    JSONObject obj = objects.getJSONObject(i);
                    String id = (String) obj.get("id");
                    this.shopItemsByKey.put(id, obj);
                    this.shopItemsKeys.add(id);
                }
            }
        }
    }

    public void writeInfo() {
        try {
            FileOutputStream progressFile = this.engine.getFileOutputStream(this.progressFileName);
            this.engine.writeFile(progressFile, this.progressJsonObject);

        } catch (JSONException e) {
            System.err.println("Error while writing info to system: " + e.getMessage());
        }
    }

    @Override
    public void shutdown() {
        for (Scene scene : this.scenes) {
            // De normal debería haber solo una escena en la pila de escenas
            // pero si hay mas, se deberia considerar recorrerla de principio a fin
            scene.saveJson();
        }

        this.writeInfo();
    }

    private void applyShopProgress() {
        JSONObject playerShopJsonObject = this.getSavedShopJsonObject();

        // Si se han leido objetos en la tienda y hay progreso de la tienda guardado
        if (!this.shopItemsKeys.isEmpty() && playerShopJsonObject != null) {
            // Recorre todos los objetos guardados
            Iterator<String> savedItems = playerShopJsonObject.keys();
            while (savedItems.hasNext()) {
                // Obtiene el JsonObject del objeto con la key actual
                String key = savedItems.next();
                JSONObject item = playerShopJsonObject.getJSONObject(key);

                // Si el objeto no es nulo y existe en la tienda
                if (item != null && shopItemsByKey.containsKey(key)) {
                    // Si el objeto leido tiene el atributo active y esta activo,
                    if (item.get("active") != null) {
                        if ((Boolean) item.get("active")) {
                            // Obtiene el JsonObject con esa key
                            JSONObject shopItem = shopItemsByKey.get(key);

                            // Intenta crear el objeto con los atributos correspondientes segun su tipo
                            try {
                                if (Objects.equals((String) shopItem.get("type"), "bgColor")) {
                                    this.bgColor = new Color((int) shopItem.get("r"), (int) shopItem.get("g"), (int) shopItem.get("b"), (int) shopItem.get("a"));
                                }
                                else if (Objects.equals((String) shopItem.get("type"), "ballSkin")) {
                                    setBallSkin((int) shopItem.get("colorId"), engine.getGraphics().newImage((String) shopItem.get("path")));
                                }
                            }
                            catch (JSONException e) {
                                System.out.println("Error while trying to apply active item: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    private JSONObject getProgressJsonObject(String key) {
        if (this.progressJsonObject.has(key)) {
            JSONObject json = this.progressJsonObject.getJSONObject(key);
            Iterator<String> aux = json.keys();
            if (!aux.hasNext()) {
                return null;
            } else {
                return json;
            }
        }
        return null;
    }

    public void setAdventureJsonObject(JSONObject adventureJsonObject) { this.progressJsonObject.put("adventure", adventureJsonObject); }
    public JSONObject getAdventureJSONObj() { return getProgressJsonObject("adventure"); }

    public void setQuickPlayJsonObject(JSONObject quickPlayJsonObject) { this.progressJsonObject.put("quickPlay", quickPlayJsonObject); }
    public JSONObject getQuickPlayJSONObj() { return getProgressJsonObject("quickPlay"); }

    public void setCoins(int coins) { this.progressJsonObject.put("coins", coins); }
    public int getCoins() { return this.progressJsonObject.getInt("coins"); }

    public List<String> getShopItemsKeys() { return this.shopItemsKeys; }
    public HashMap<String, JSONObject> getShopItemsByKey() { return this.shopItemsByKey; }
    public JSONObject getSavedShopJsonObject() { return getProgressJsonObject("shop"); }
    public void setSavedShopJsonObject(JSONObject obj) { this.progressJsonObject.put("shop", obj); }

    public void setBgColor(Color c) { this.bgColor = c; }
    public Color getBgColor() { return this.bgColor; }

    public void setBallSkin(int i, IImage img) { activeSkins[i] = img; }
    public IImage getBallSkin(int i) { return activeSkins[i]; }
}