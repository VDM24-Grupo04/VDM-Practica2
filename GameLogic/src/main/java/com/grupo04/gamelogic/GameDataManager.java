package com.grupo04.gamelogic;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GameDataManager {
    IEngine engine;

    // Niveles dirigidos por datos
    private int totalLevels;
    private int[] worlds;

    private Color[][] styleColors;
    private final int COLORS_PER_WORLD = 4;
    private final Color DEFAULT_UNLOCKED_COLOR = new Color(255, 255, 255);  // Blanco
    private final Color DEFAULT_PASSED_COLOR = new Color(128, 128, 128); // Gris medio
    private final Color DEFAULT_LOCKED_COLOR = new Color(100, 100, 100);    // Gris oscuro
    private final Color DEFAULT_POINTER_OVER_COLOR = new Color(180, 180, 180); // Gris claro

    // Tienda dirigida por datos
    private final String shopFileName;
    private final List<String> shopItemsKeys;
    private final HashMap<String, JSONObject> shopItemsByKey;

    public GameDataManager(IEngine engine, String shopFileName) {
        this.engine = engine;

        // Niveles dirigidos por datos
        this.totalLevels = 0;
        readLevelsData();
        styleColors = readLevelsStyle();

        // Tienda
        this.shopFileName = shopFileName;
        this.shopItemsKeys = new ArrayList<>();
        this.shopItemsByKey = new HashMap<>();

        // Se intenta leer la tienda
        InputStream shopFile = this.engine.getFileInputStream(this.shopFileName, IEngine.FileType.GAME_DATA);
        String shopStr = this.engine.readFile(shopFile);
        if (shopStr != null) {
            try {
                readShop(new JSONObject(shopStr));
            } catch (Exception e) {
                System.out.println("Invalid shop file");
            }
        }
    }


    // Mundos y niveles
    private void readLevelsData() {
        // Busca el directorio "levels" en los assets y consigue los nombres de los directorios
        String[] levelsDirectories = this.engine.listDirectories("levels", IEngine.FileType.GAME_DATA);
        if (levelsDirectories == null || levelsDirectories.length == 0) {
            this.worlds = new int[0];
        }
        else {
            // Nos aseguramos que el recorrido de directorios "worldX"
            // esté ordenado por el índice (p.e.: world1 < world2 < world23)
            Arrays.sort(levelsDirectories, (f1, f2) -> extractNumber(f1) - extractNumber((f2)));

            // Para cada directorio en "levels", si contiene "world" + indice
            // de forma incremental empezando por 1
            List<String> worldsFiles = new ArrayList<>();
            int worldIndex = 1;
            for (String dirName : levelsDirectories) {
                if (dirName.contains("world" + worldIndex)) {
                    worldsFiles.add(dirName);
                    ++worldIndex;
                }
            }

            // Inicia los mundos al tamaño con el número de directorios
            // con "worldX" de forma incremental (world1, world2, etc.)
            this.worlds = new int[worldsFiles.size()];
            Arrays.fill(this.worlds, 0);

            // Para cada directorio de "worlds", si contiene "level" + indice
            // + ".json" de forma incremental
            for (int i = 0; i < worldsFiles.size(); ++i) {
                String[] levels = this.engine.listFiles("levels/" + worldsFiles.get(i), IEngine.FileType.GAME_DATA);
                if (levels != null) {
                    int levelIndex = 1;
                    for (String levelName : levels) {
                        if (levelName.contains("level" + levelIndex + ".json")) {
                            ++this.worlds[i];
                            ++this.totalLevels;
                            ++levelIndex;
                        }
                    }
                }
            }
        }
    }

    private int extractNumber(String filename) {
        // Reemplaza todos los números por vacío y lo devuelve
        return Integer.parseInt(filename.replaceAll("\\D+", ""));
    }

    private Color[][] readLevelsStyle() {
        Color[][] colors = new Color[this.worlds.length][COLORS_PER_WORLD];

        for (int i = 0; i < this.worlds.length; i++) {
            String worldStyleFileName = "levels/world" + (i + 1) + "/style.json";
            InputStream styleFile = this.engine.getFileInputStream(worldStyleFileName, IEngine.FileType.GAME_DATA);

            Color unlockedColor = DEFAULT_UNLOCKED_COLOR;
            Color passedColor = DEFAULT_PASSED_COLOR;
            Color lockedColor = DEFAULT_LOCKED_COLOR;
            Color pointerOverColor = DEFAULT_POINTER_OVER_COLOR;

            if (styleFile != null) {
                String styleStr = this.engine.readFile(styleFile);
                if (styleStr != null) {
                    try {
                        JSONObject style = new JSONObject(styleStr);
                        unlockedColor = getColor(style, "unlocked", DEFAULT_UNLOCKED_COLOR);
                        passedColor = getColor(style, "passed", DEFAULT_UNLOCKED_COLOR);
                        lockedColor = getColor(style, "locked", DEFAULT_LOCKED_COLOR);
                        pointerOverColor = getColor(style, "pointerOver", DEFAULT_POINTER_OVER_COLOR);

                    } catch (Exception e) {
                        System.err.println("Error parsing style for world " + (i + 1) + ": " + e.getMessage());
                    }
                }
            }

            // Siempre hay COLORS_PER_WORLD colores
            colors[i][0] = unlockedColor;
            colors[i][1] = passedColor;
            colors[i][2] = pointerOverColor;
            colors[i][3] = lockedColor;
        }
        return colors;
    }

    private Color getColor(JSONObject style, String key, Color defaultColor) {
        try {
            if (style.has(key) && style.getJSONObject(key) != null) {
                JSONObject colorJson = style.getJSONObject(key);
                int r = colorJson.optInt("r", defaultColor.red);
                int g = colorJson.optInt("g", defaultColor.green);
                int b = colorJson.optInt("b", defaultColor.blue);
                return new Color(r, g, b);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving color " + key + ": " + e.getMessage());
        }
        return defaultColor;
    }

    private Pair<Integer, Integer> getLevelWorld(int levelNumber) {
        boolean found = false;
        Pair<Integer, Integer> levelWorld = new Pair<>(0, 0);

        int k = 1;
        int i = 0;
        while (i < this.worlds.length && !found) {
            int j = 0;
            while (j < this.worlds[i] && !found) {
                if (levelNumber == k) {
                    levelWorld.setFirst(i + 1);
                    levelWorld.setSecond(j + 1);
                    found = true;
                }
                ++j;
                ++k;
            }
            ++i;
        }

        return levelWorld;
    }


    // Tienda
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
                } catch (Exception e) {
                    System.out.println("Invalid item (doesn't have an id)");
                }
            }
        }
    }

    // Getters
    public JSONObject getLevelJson(int levelNumber) {
        Pair<Integer, Integer> levelWorld = this.getLevelWorld(levelNumber);

        // Carga el archivo json del nivel
        String levelFileName = "levels/world" + levelWorld.getFirst() + "/level" + levelWorld.getSecond() + ".json";
        InputStream levelFile = this.engine.getFileInputStream(levelFileName, IEngine.FileType.GAME_DATA);

        if (levelFile != null) {
            String levelStr = this.engine.readFile(levelFile);
            if (levelStr != null) {
                try {
                    JSONObject obj = new JSONObject(levelStr);

                    // Comprobar si existen grid y colors
                    if (!obj.has("grid")) {
                        System.out.println("Grid not found or empty. Generating quickplay grid");
                        return null;
                    }
                    if (!obj.has("colors")) {
                        System.out.println("Bubble colors not found. Generating quickplay grid");
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

    public int getTotalLevels() { return this.totalLevels; }

    public int[] getWorlds() { return this.worlds; }

    public Color[][] getLevelsStyle() { return this.styleColors; }

    public List<String> getShopItemsKeys() { return this.shopItemsKeys; };

    public HashMap<String, JSONObject> getShopItemsByKey() { return this.shopItemsByKey;};
}
