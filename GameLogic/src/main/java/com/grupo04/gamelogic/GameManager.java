package com.grupo04.gamelogic;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.scenes.GameScene;
import com.grupo04.gamelogic.scenes.TitleScene;
import com.grupo04.gamelogic.scenes.VictoryScene;

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

public class GameManager extends SceneManager {
    private final String ADVENTURE_KEY = "adventure";
    private final String QUICK_PLAY_KEY = "quickPlay";
    private final String COINS_KEY = "coins";
    private final String SHOP_KEY = "shop";

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
        super(engine);
        this.progressFileName = fileName;
        this.progressJsonObject = new JSONObject();

        this.shopFileName = shopFileName;

        // Inicializar cosmeticos
        shopItemsKeys = new ArrayList<>();
        shopItemsByKey = new HashMap<>();
        bgColor = null;
        activeSkins = new IImage[BubbleColors.getTotalColors()];
        Arrays.fill(activeSkins, null);
    }

    @Override
    protected void initNewScene(Scene newScene) {
        newScene.setGameManager(this);
        super.initNewScene(newScene);
    }

    private JSONObject getProgressJsonObject(String key) {
        if (this.progressJsonObject.has(key)) {
            JSONObject json = this.progressJsonObject.getJSONObject(key);
            // Se hace de esta forma y no con .isEmpty() porque este metodo
            // produce un error en Android
            Iterator<String> aux = json.keys();
            if (!aux.hasNext()) {
                return null;
            } else {
                return json;
            }
        }
        return null;
    }

    private <T> void tryToCreateProgressProperty(String key, T defaultValue) {
        if (!this.progressJsonObject.has(key)) {
            this.progressJsonObject.put(key, defaultValue);
        }
    }

    private void readShop(JSONObject shopJson) {
        // Obtiene el array de objetos
        JSONArray objects = (JSONArray) shopJson.get("items");
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

    @Override
    public void init() {
        readInfo();
        applyShopProgress();
        pushScene(new TitleScene(this.engine));
    }

    public void changeToGameScene(int levelNumber) {
        JSONObject json = null;
        // Progreso del modo de juego rapido
        if (levelNumber == 0) {
            json = this.getQuickPlayJSONObj();
            this.setQuickPlayJsonObject(new JSONObject());
        } else {
            json = this.getAdventureJSONObj();
            if (json != null) {
                int levelNumJson = json.getInt("levelNumber");
                if (levelNumber == levelNumJson) {
                    this.setAdventureJsonObject(new JSONObject());
                } else {
                    // TODO: Setear json con el nivel entero
                }
            } else {
                // TODO: Setear json con el nivel entero
            }
        }
        this.changeScene(new GameScene(this.engine, json, levelNumber));
    }

    public void readInfo() {
        InputStream progressFile = this.engine.getFileInputStream(this.progressFileName, IEngine.FileType.PROGRESS_DATA);
        JSONObject json = this.engine.readFile(progressFile);
        if (json != null) {
            this.progressJsonObject = json;
        }
        this.tryToCreateProgressProperty(COINS_KEY, 0);
        this.tryToCreateProgressProperty(SHOP_KEY, new JSONObject());
        this.tryToCreateProgressProperty(QUICK_PLAY_KEY, new JSONObject());
        this.tryToCreateProgressProperty(ADVENTURE_KEY, new JSONObject());

        InputStream shopFile = this.engine.getFileInputStream(this.shopFileName, IEngine.FileType.GAME_DATA);
        JSONObject shopJson = this.engine.readFile(shopFile);
        if (shopJson != null) {
            readShop(shopJson);
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
        super.shutdown();
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
                                } else if (Objects.equals((String) shopItem.get("type"), "ballSkin")) {
                                    setBallSkin((int) shopItem.get("colorId"), engine.getGraphics().newImage((String) shopItem.get("path")));
                                }
                            } catch (JSONException e) {
                                System.out.println("Error while trying to apply active item: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    public void setAdventureJsonObject(JSONObject adventureJsonObject) {
        this.progressJsonObject.put("adventure", adventureJsonObject);
    }

    public JSONObject getAdventureJSONObj() {
        return getProgressJsonObject("adventure");
    }

    public void setQuickPlayJsonObject(JSONObject quickPlayJsonObject) {
        this.progressJsonObject.put("quickPlay", quickPlayJsonObject);
    }

    public JSONObject getQuickPlayJSONObj() {
        return getProgressJsonObject("quickPlay");
    }

    public void increaseCoins(int coins) {
        int currCoins = getCoins();
        currCoins += coins;
        this.progressJsonObject.put(COINS_KEY, currCoins);
    }

    public void decreaseCoins(int coins) {
        int currCoins = getCoins();
        currCoins -= coins;
        currCoins = Math.max(currCoins, 0);
        this.progressJsonObject.put(COINS_KEY, currCoins);
    }

    public int getCoins() {
        if (this.progressJsonObject.has(COINS_KEY)) {
            return this.progressJsonObject.getInt(COINS_KEY);
        }
        return 0;
    }

    // Metodos tienda
    public List<String> getShopItemsKeys() {
        return this.shopItemsKeys;
    }

    public HashMap<String, JSONObject> getShopItemsByKey() {
        return this.shopItemsByKey;
    }

    public JSONObject getSavedShopJsonObject() {
        return getProgressJsonObject("shop");
    }

    public void setSavedShopJsonObject(JSONObject obj) {
        this.progressJsonObject.put("shop", obj);
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
}