package com.grupo04.gamelogic;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.IMobile;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Pair;
import com.grupo04.gamelogic.scenes.CheaterScene;
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

public class GameManager extends SceneManager {
    private final Color DEFAULT_BG_COLOR = new Color(255, 255, 255);

    private final int NUM_WORLDS = 3;
    private final int LEVELS_PER_WORLD = 4;

    private final String SECRET = "VDM24-Grupo04-Practica2";
    private final int MIN_HASH_SIZE = 64;

    private final String ADVENTURE_KEY = "adventure";
    private final String QUICK_PLAY_KEY = "quickPlay";
    private final String COINS_KEY = "coins";
    private final String LEVEL_PROGRESS_KEY = "levelProgress";
    private final String SHOP_KEY = "shop";

    // Progresion del juego
    private final String progressFileName;
    private JSONObject progressJsonObject;

    // Tienda dirigida por datos
    private final String shopFileName;
    private final List<String> shopItemsKeys;
    private final HashMap<String, JSONObject> shopItemsByKey;

    // Cosmeticos
    private Color bgColor;
    private final IImage[] activeSkins;

    public GameManager(IEngine engine, String fileName, String shopFileName) {
        super(engine);
        this.progressFileName = fileName;
        this.progressJsonObject = null;

        this.shopFileName = shopFileName;

        // Inicializar cosmeticos
        this.shopItemsKeys = new ArrayList<>();
        this.shopItemsByKey = new HashMap<>();
        this.bgColor = null;
        this.activeSkins = new IImage[BubbleColors.getTotalColors()];
        Arrays.fill(this.activeSkins, null);
    }

    @Override
    protected void initNewScene(Scene newScene) {
        newScene.setGameManager(this);
        super.initNewScene(newScene);
    }

    @Override
    public void init() {
        if (readInfo()) {
            applyShopProgress();
            pushScene(new TitleScene(this.engine));
            IMobile mobile = this.engine.getMobile();
            if (mobile != null) {
                // Las recompensas deben aplicarse siempre y cuando sea correcta la
                // lectura del archivo de guardado
                if (mobile.isNotification("Reward1")) {
                    this.increaseCoins(1);
                }
                // ... (resto de recompensas)
            }
        }
        else {
            pushScene(new CheaterScene(this.engine));
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

    public boolean readInfo() {
        InputStream progressFile = this.engine.getFileInputStream(this.progressFileName, IEngine.FileType.PROGRESS_DATA);
        String progressStrAndHash = this.engine.readFile(progressFile);
        if (progressStrAndHash != null) {
            // Debe tener al menos 64 caracteres para el hash...
            if (progressStrAndHash.length() >= MIN_HASH_SIZE) {
                int progressLength = progressStrAndHash.length() - MIN_HASH_SIZE;
                // Substring con el hash
                String hashStr = progressStrAndHash.substring(progressLength);
                // Substring con el progreso
                String progressStr = progressStrAndHash.substring(0, progressLength);
                // Si no cuadran los hashes (teniendo en cuenta el SECRET), entonces es
                // porque modificó el archivo game.json
                if (!Objects.equals(this.engine.getHash(progressStr + SECRET), hashStr)) {
                    System.err.println("Hashes do not match!");
                    return false;
                }
                this.progressJsonObject = new JSONObject(progressStr);
            }
            else {
                System.err.println("Something went wrong. Progress and hash string is not bigger or equal to 256.");
                return false;
            }
        }
        else {
            this.progressJsonObject = new JSONObject();
        }

        this.tryToCreateProgressProperty(LEVEL_PROGRESS_KEY, 1);
        this.tryToCreateProgressProperty(COINS_KEY, 0);
        this.tryToCreateProgressProperty(SHOP_KEY, new JSONObject());
        this.tryToCreateProgressProperty(QUICK_PLAY_KEY, new JSONObject());
        this.tryToCreateProgressProperty(ADVENTURE_KEY, new JSONObject());

        // Se intenta leer la tienda
        InputStream shopFile = this.engine.getFileInputStream(this.shopFileName, IEngine.FileType.GAME_DATA);
        String shopStr = this.engine.readFile(shopFile);
        if (shopStr != null) {
            try {
                readShop(new JSONObject(shopStr));
            }
            catch (Exception e) {
                System.out.println("Invalid shop file");
            }
        }
        return true;
    }

    public void writeInfo() {
        try {
            // Si es nulo es porque ha habido fallas en readInfo() debido a la comprobacion de los hashes
            if (this.progressJsonObject != null) {
                FileOutputStream progressFile = this.engine.getFileOutputStream(this.progressFileName);
                String progressStr = this.progressJsonObject.toString();
                this.engine.writeFile(progressFile, progressStr + this.engine.getHash(progressStr + SECRET));
            }
            else {
                this.engine.eraseFile(this.progressFileName);
            }
        }
        catch (JSONException e) {
            System.err.println("Error while writing info to system: " + e.getMessage());
        }
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
        JSONArray objects = shopJson.getJSONArray("items");

        // Si el array de objetos es valido
        if (objects != null) {
            // Recorre todos los objetos y los anade a la tienda
            for (int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                try {
                    String id = obj.getString("id");
                    this.shopItemsByKey.put(id, obj);
                    this.shopItemsKeys.add(id);
                }
                catch (Exception e) {
                    System.out.println("Invalid item (doesn't have an id)");
                }
            }
        }
    }

    public void changeToGameScene(int levelNumber) {
        JSONObject json;
        // Progreso del modo de juego rapido
        if (levelNumber == 0) {
            json = this.getQuickPlayJSONObj();
            this.setQuickPlayJsonObject(new JSONObject());
        }
        // Progreso del modo aventura
        else {
            json = this.getAdventureJSONObj();

            // Si hay progreso guardado
            if (json != null) {
                int levelNumJson = json.getInt("levelNumber");
                // Si el ultimo nivel guardado es el que se va a jugar
                if (levelNumber == levelNumJson) {
                    this.setAdventureJsonObject(new JSONObject());
                }
                // Si el ultimo nivel guardado es distinto al que se va a jugar
                else {
                    json = this.getLevelJson(levelNumber);
                }
            }
            // Si no hay progreso guardado
            else {
                json = this.getLevelJson(levelNumber);
            }
        }

        this.changeScene(new GameScene(this.engine, json, levelNumber));
    }

    public void changeToQuickPlay() {
        changeToGameScene(0);
    }

    private JSONObject getLevelJson(int levelNumber) {
        Pair<Integer, Integer> levelWorld = this.getLevelWorld(levelNumber);

        // Carga el archivo json del nivel
        String levelFileName = "levels/world" + levelWorld.getFirst() + "/level" + levelWorld.getSecond() + ".json";
        InputStream levelFile = this.engine.getFileInputStream(levelFileName, IEngine.FileType.GAME_DATA);

        if (levelFile != null) {
            String levelStr = this.engine.readFile(levelFile);
            if (levelStr != null) {
                try {
                    JSONObject obj = new JSONObject(levelStr);

                    try {
                        obj.get("grid");
                    }
                    catch (Exception e) {
                        System.out.println("Grid not found. Generating qickplay grid");
                        return null;
                    }

                    try {
                        obj.get("colors");
                    }
                    catch (Exception e) {
                        System.out.println("Bubble colors not found. Generating qickplay grid");
                        return null;
                    }
                    return obj;
                }
                catch (Exception e) {
                    System.out.println("Invalid level. Generating quickplay grid");
                    return null;
                }
            }
        }
        else {
            System.out.println("World " + levelWorld.getFirst() + " level " + levelNumber + " not found. Generating quickplay grid");
        }
        return null;
    }

    public Color[][] getLevelsStyle() {
        Color[][] colors = new Color[NUM_WORLDS][3];

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

                        colors[i][0] = unlocked;
                        colors[i][1] = pointover;
                        colors[i][2] = locked;
                    }
                }
            }
        }
        return colors;
    }

    private void applyShopProgress() {
        JSONObject playerShopJsonObject = this.getSavedShopJsonObject();
        this.setBgColor(null);

        // Si se han leido objetos en la tienda y hay progreso de la tienda guardado
        if (!this.shopItemsKeys.isEmpty() && playerShopJsonObject != null) {
            // Recorre todos los objetos guardados
            Iterator<String> savedItems = playerShopJsonObject.keys();
            while (savedItems.hasNext()) {
                // Obtiene el JsonObject del objeto con la key actual
                String key = savedItems.next();
                JSONObject item = playerShopJsonObject.getJSONObject(key);

                // Si el objeto no es nulo y existe en la tienda
                if (item != null && this.shopItemsByKey.containsKey(key)) {
                    // Si el objeto leido tiene el atributo active y esta activo,
                    try {
                        // Si el objeto esta comprado y activo, se selecciona
                        if (item.getBoolean("active")) {
                            // Obtiene el JsonObject con esa key
                            JSONObject shopItem = this.shopItemsByKey.get(key);

                            // Intenta crear el objeto con los atributos correspondientes segun su tipo
                            try {
                                if (Objects.equals(shopItem.get("type"), "bgColor")) {
                                    this.setBgColor(new Color(shopItem.getInt("r"), shopItem.getInt("g"), shopItem.getInt("b"), shopItem.getInt("a")));
                                }
                                else if (Objects.equals(shopItem.get("type"), "ballSkin")) {
                                    setBallSkin(shopItem.getInt("colorId"), this.engine.getGraphics().newImage(shopItem.getString("path")));
                                }
                            }
                            catch (JSONException e) {
                                System.out.println("Error while trying to apply active item: " + e.getMessage());
                            }
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Item " + key + " doesn't have active property");
                    }
                }
            }
        }
    }

    public void setAdventureJsonObject(JSONObject adventureJsonObject) {
        this.progressJsonObject.put(ADVENTURE_KEY, adventureJsonObject);
    }
    public JSONObject getAdventureJSONObj() {
        return getProgressJsonObject(ADVENTURE_KEY);
    }

    public void setQuickPlayJsonObject(JSONObject quickPlayJsonObject) {
        this.progressJsonObject.put(QUICK_PLAY_KEY, quickPlayJsonObject);
    }
    public JSONObject getQuickPlayJSONObj() {
        return getProgressJsonObject(QUICK_PLAY_KEY);
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

    public void setLevelProgress(int levelNumber) {
        int currLevelProgress = this.getLevelProgress();
        if (levelNumber > currLevelProgress) {
            this.progressJsonObject.put(LEVEL_PROGRESS_KEY, levelNumber);
        }
    }
    public int getLevelProgress() {
        if (this.progressJsonObject.has(LEVEL_PROGRESS_KEY)) {
            return this.progressJsonObject.getInt(LEVEL_PROGRESS_KEY);
        }
        return 1;
    }

    public Pair<Integer, Integer> getLevelWorld(int levelNumber) {
        int world = (levelNumber - 1) / LEVELS_PER_WORLD + 1;
        int level = (levelNumber - 1) % LEVELS_PER_WORLD + 1;
        return new Pair<>(world, level);
    }

    public int getNWorlds() {
        return NUM_WORLDS;
    }
    public int getLevelsPerWorld() {
        return LEVELS_PER_WORLD;
    }

    // Metodos tienda
    public List<String> getShopItemsKeys() {
        return this.shopItemsKeys;
    }
    public HashMap<String, JSONObject> getShopItemsByKey() {
        return this.shopItemsByKey;
    }

    public JSONObject getSavedShopJsonObject() {
        return getProgressJsonObject(SHOP_KEY);
    }
    public void setSavedShopJsonObject(JSONObject obj) {
        this.progressJsonObject.put(SHOP_KEY, obj);
    }

    public void setBgColor(Color c) {
        Color col = c;
        if (c == null) {
            col = DEFAULT_BG_COLOR;
        }
        engine.getGraphics().setClearColor(col);
        this.bgColor = c;
    }
    public Color getBgColor(boolean ignoreDefault) {
        if (this.bgColor != null) {
            return this.bgColor;
        }
        else {
            if (ignoreDefault) {
                return null;
            }
        }
        return DEFAULT_BG_COLOR;
    }

    public void setBallSkin(int i, IImage img) {
        activeSkins[i] = img;
    }
    public IImage getBallSkin(int i) {
        return activeSkins[i];
    }
}
