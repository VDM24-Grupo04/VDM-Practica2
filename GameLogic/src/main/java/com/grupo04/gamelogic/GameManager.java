package com.grupo04.gamelogic;

import com.grupo04.engine.Sensor;
import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.IMobile;
import com.grupo04.engine.interfaces.ISensor;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.scenes.CheaterScene;
import com.grupo04.gamelogic.scenes.GameScene;
import com.grupo04.gamelogic.scenes.TitleScene;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GameManager extends SceneManager {
    private final Color DEFAULT_BG_COLOR = new Color(255, 255, 255);

    private final String SECRET = "VDM24-Grupo04-Practica2";
    private final int MIN_HASH_SIZE = 64;

    private final String ADVENTURE_KEY = "adventure";
    private final String QUICK_PLAY_KEY = "quickPlay";
    private final String COINS_KEY = "coins";
    private final String LEVEL_PROGRESS_KEY = "levelProgress";
    private final String SHOP_KEY = "shop";

    private final GameDataManager gameDataManager;
    private IMobile mobile;

    // Progresion del juego
    private final String progressFileName;
    private JSONObject progressJsonObject;

    // Cosmeticos
    private float BG_COLOR_MULTIPLY_FACTOR = 0.60f;
    private Color bgColor;
    private Color UIColor;
    private final IImage[] activeSkins;
    private final String DEFAULT_BG_IMAGE = "backgroundDefault.jpg";
    private String bgImage;

    // Recompensas por notificacion
    private final String REWARD_1 = "Reward1";
    private final String REWARD_1_TITLE = "¡Entra ahora para conseguir tu recompensa diaria!";
    private final String REWARD_1_MESSAGE = "1 moneda gratis para ti porque me caes bien";

    private final String notificationIconFileName;

    // Opcional: sensor de giroscopio
    private final float SHAKE_ACCELERATION = 1;
    private float acceleration;
    private float currentAcceleration;

    public GameManager(IEngine engine, String fileName, String shopFileName, String notificationIconFileName) {
        super(engine);

        this.gameDataManager = new GameDataManager(engine, shopFileName);

        // Progresion del juego
        this.progressFileName = fileName;
        this.progressJsonObject = null;

        // Inicializar cosmeticos
        this.bgColor = null;
        this.activeSkins = new IImage[BubbleColors.getTotalColors()];
        Arrays.fill(this.activeSkins, null);
        this.bgImage = DEFAULT_BG_IMAGE;

        this.notificationIconFileName = notificationIconFileName;
        this.acceleration = 0.0f;
        this.currentAcceleration = 0.0f;
    }

    public GameManager(IEngine engine, String fileName, String shopFileName) {
        this(engine, fileName, shopFileName, "");
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

            // Le pasamos buttonColor si queremos para cambiar la tematica
            pushScene(new TitleScene(this.engine, this.UIColor));
            this.mobile = this.engine.getMobile();
            if (this.mobile != null) {
                // Las recompensas deben aplicarse siempre y cuando sea correcta la
                // lectura del archivo de guardado
                if (this.mobile.isNotification(REWARD_1)) {
                    this.increaseCoins(1);
                }
                // ... (resto de recompensas)
            }
        } else {
            pushScene(new CheaterScene(this.engine, this.UIColor));
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

        if (this.mobile != null) {
            int icon = this.mobile.getAsset(this.notificationIconFileName);
            this.mobile.programNotification(3, TimeUnit.SECONDS,
                    REWARD_1, REWARD_1_TITLE, REWARD_1_MESSAGE, icon,
                    IMobile.NotificationPriority.HIGH, IMobile.NotificationVisibility.PUBLIC);
        }
    }

    @Override
    public void sensorChanged(Sensor sensor) {
        // Opcional: sensor de giroscopio
        if (sensor.getType() == ISensor.SensorType.GYROSCOPE) {
            float[] values = sensor.getValues();
            float x = values[0];
            float y = values[1];
            float z = values[2];
            float lastAcceleration = this.currentAcceleration;

            this.currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = this.currentAcceleration - lastAcceleration;
            this.acceleration = this.acceleration * 0.9f + delta;

            if (this.acceleration > SHAKE_ACCELERATION) {
                this.increaseCoins(1);
            }
        }
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

        return true;
    }

    public void writeInfo() {
        try {
            // Si es nulo es porque ha habido fallas en readInfo() debido a la comprobacion de los hashes
            if (this.progressJsonObject != null) {
                FileOutputStream progressFile = this.engine.getFileOutputStream(this.progressFileName);
                if (progressFile != null) {
                    String progressStr = this.progressJsonObject.toString();
                    this.engine.writeFile(progressFile, progressStr + this.engine.getHash(progressStr + SECRET));
                }
            } else {
                this.engine.eraseFile(this.progressFileName);
            }
        } catch (JSONException e) {
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
                    json = this.gameDataManager.getLevelJson(levelNumber);
                }
            }
            // Si no hay progreso guardado
            else {
                json = this.gameDataManager.getLevelJson(levelNumber);
            }
        }

        this.changeScene(new GameScene(this.engine, json, levelNumber, this.UIColor));
    }

    public void changeToQuickPlay() {
        changeToGameScene(0);
    }

    private void applyShopProgress() {
        List<String> shopItemsKeys = this.gameDataManager.getShopItemsKeys();
        HashMap<String, JSONObject> shopItemsByKey = this.gameDataManager.getShopItemsByKey();

        JSONObject playerShopJsonObject = this.getSavedShopJsonObject();
        this.setBgColor(null);

        // Si se han leido objetos en la tienda y hay progreso de la tienda guardado
        if (!shopItemsKeys.isEmpty() && playerShopJsonObject != null) {
            // Recorre todos los objetos guardados
            Iterator<String> savedItems = playerShopJsonObject.keys();
            while (savedItems.hasNext()) {
                // Obtiene el JsonObject del objeto con la key actual
                String key = savedItems.next();
                JSONObject item = playerShopJsonObject.getJSONObject(key);

                // Si el objeto no es nulo y existe en la tienda
                if (item != null && shopItemsByKey.containsKey(key)) {
                    // Si el objeto leido tiene el atributo active y esta activo,
                    try {
                        // Si el objeto esta comprado y activo, se selecciona
                        if (item.getBoolean("active")) {
                            // Obtiene el JsonObject con esa key
                            JSONObject shopItem = shopItemsByKey.get(key);

                            // Intenta crear el objeto con los atributos correspondientes segun su tipo
                            try {
                                if (Objects.equals(shopItem.get("type"), "themeColor")) {
                                    this.setUIColor(new Color(shopItem.getInt("r"), shopItem.getInt("g"), shopItem.getInt("b"), shopItem.getInt("a")));
                                } else if (Objects.equals(shopItem.get("type"), "ballSkin")) {
                                    setBallSkin(shopItem.getInt("colorId"), this.engine.getGraphics().newImage(shopItem.getString("path")));
                                } else if (Objects.equals(shopItem.get("type"), "bgImage")) {
                                    setBgImage(shopItem.getString("path"));
                                }
                            } catch (JSONException e) {
                                System.out.println("Error while trying to apply active item: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Item " + key + " doesn't have active property");
                    }
                }
            }
        }
    }

    public void setAdventureJsonObject(JSONObject adventureJsonObject) {
        this.progressJsonObject.put(ADVENTURE_KEY, adventureJsonObject);
    }
    public JSONObject getAdventureJSONObj() { return getProgressJsonObject(ADVENTURE_KEY); }

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

    // Metodos de informacion de los niveles (no de progreso)
    public int getTotalLevels() {
        return this.gameDataManager.getTotalLevels();
    }
    public int[] getWorlds() {
        return this.gameDataManager.getWorlds();
    }
    public Color[][] getLevelsStyle() {
        return this.gameDataManager.getLevelsStyle();
    }

    // Metodos del archivo de la tienda (no de progreso)
    public List<String> getShopItemsKeys() {
        return this.gameDataManager.getShopItemsKeys();
    }
    public HashMap<String, JSONObject> getShopItemsByKey() {
        return this.gameDataManager.getShopItemsByKey();
    }

    // Metodos del progreso de la tienda
    public JSONObject getSavedShopJsonObject() {
        return getProgressJsonObject(SHOP_KEY);
    }
    public void setSavedShopJsonObject(JSONObject obj) {
        this.progressJsonObject.put(SHOP_KEY, obj);
    }

    public void setBgColor(Color c) {
        Color col;
        if (c == null) {
            col = DEFAULT_BG_COLOR;
        } else {
            // Calcula el color de fondo aplicando un factor
            int newRed = Math.min(255, c.red + (int)(c.red * BG_COLOR_MULTIPLY_FACTOR));
            int newGreen = Math.min(255, c.green + (int)(c.green * BG_COLOR_MULTIPLY_FACTOR));
            int newBlue = Math.min(255, c.blue + (int)(c.blue * BG_COLOR_MULTIPLY_FACTOR));
            col = new Color(newRed, newGreen, newBlue, c.alpha);
        }
        this.engine.getGraphics().setClearColor(col);
        this.bgColor = col;
    }
    public Color getBgColor() {
        if (this.bgColor != null) {
            return this.bgColor;
        }
        return DEFAULT_BG_COLOR;
    }

    public void setBgImage(String path) {
        this.bgImage = path.isBlank() ? DEFAULT_BG_IMAGE : path;
    }
    public String getBgImage() {
        return this.bgImage;
    }

    public void setBallSkin(int i, IImage img) {
        this.activeSkins[i] = img;
    }
    public IImage getBallSkin(int i) { return this.activeSkins[i]; }

    // Para nuestras escenas del juego queremos modificar el color de fondo
    // y el color de los botones, textos, etc.
    public void setUIColor(Color c) {
        this.setBgColor(c);
        this.UIColor = c;
        for (Scene scene : this.scenes) {
            scene.setUIColor(this.UIColor);
        }
    }
}
