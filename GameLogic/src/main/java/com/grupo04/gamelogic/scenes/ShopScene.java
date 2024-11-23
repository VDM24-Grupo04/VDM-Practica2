package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.gameobjects.ImageButton;
import com.grupo04.gamelogic.gameobjects.ShopItem;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.gameobjects.shopItems.ShopBallSkin;
import com.grupo04.gamelogic.gameobjects.shopItems.ShopBgColor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ShopScene extends Scene {
    private final String BUTTON_SOUND = "button.wav";
    private final String FONT_NAME = "kimberley.ttf";
    private final int HEADER_SIZE = 40, HEADER_OFFSET = 20, FONT_SIZE = 20;
    private Color TEXT_COLOR = new Color(0, 0, 0);

    IImage coinImg;
    private IFont headerCoinsFont;
    private Vector headerCoinsTextPos;
    private Vector headerCoinsImgPos;
    private final float COINS_IMAGE_SIZE = HEADER_SIZE * 0.6f;

    private IFont pricesFont;
    private float itemSize;
    private final int ITEMS_PER_ROW = 4, ITEM_OFFSET = 10;
    private Color SELECTED_COLOR = new Color(0, 255, 0);
    private int coinsImageSize = this.FONT_SIZE;

    private HashMap<String, ShopItem> items;
    private List<ShopBgColor> colors;

    public ShopScene(IEngine engine) {
        super(engine, 400, 600, new Color(255, 255, 255));

        items = new HashMap<>();
        colors = new ArrayList<>();
    }

    @Override
    public void init() {
        IGraphics graphics = getEngine().getGraphics();

        // Se anaden los objetos del header
        createHeader(graphics);

        // Se crea la fuente
        this.pricesFont = graphics.newFont(this.FONT_NAME, this.FONT_SIZE, true, false);

        // Se calcula el tamano de los objetos dependiendo del numero de objetos por fila
        float freeSpace = this.worldWidth - this.HEADER_OFFSET * 2 - this.ITEM_OFFSET * (this.ITEMS_PER_ROW - 1);
        this.itemSize = freeSpace / this.ITEMS_PER_ROW;

        // Anadir los objetos
        readItems();

        super.init();

        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);

        // TEST
        this.gameManager.setCoins(300);
//        addBgColor("test1", 244, 20, 2, 255);
//        addBgColor("test2", 123, 67, 222, 255);
//        addBgColor("test3", 31, 64, 64, 255);
//        addBgColor("test4", 235, 122, 0, 255);
//        addBgColor("test5", 123, 67, 222, 255);
//        addBgColor("test6", 244, 20, 2, 255);
//        addBgColor("test7", 235, 122, 0, 255);
//        addBgColor("test8", 31, 64, 64, 255);
//
//        addBallSkin("img0","emotiguy0.png", 0);
//        addBallSkin("img1","emotiguy1.png", 1);
//        addBallSkin("img2","emotiguy2.png", 2);
//        addBallSkin("img3","emotiguy3.png", 3);
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.setFont(this.headerCoinsFont);
        graphics.setColor(this.TEXT_COLOR);
        graphics.drawText(((Integer) (gameManager.getCoins())).toString(), headerCoinsTextPos, false, true);
        graphics.drawImage(this.coinImg, this.headerCoinsImgPos, (int) this.COINS_IMAGE_SIZE, (int) this.COINS_IMAGE_SIZE);

        super.render(graphics);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.TEXT_COLOR = null;
        this.coinImg = null;
        this.headerCoinsFont = null;
        this.headerCoinsTextPos = null;
        this.headerCoinsImgPos = null;

        this.pricesFont = null;
        SELECTED_COLOR = null;
        items.clear();
    }

    @Override
    public void saveJson() {
        // o llamarlo cada vez que se gastan monedas
//        this.gameManager.setCoins(this.coins);
    }


    // Crea los elementos del header
    private void createHeader(IGraphics graphics) {
        // Se anade el boton de volver al menu inicial
        String MENU_BUTTON_IMG = "close.png";

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
                "Tienda", this.FONT_NAME, this.HEADER_SIZE, false, false, this.TEXT_COLOR);
        addGameObject(title);

        // Se crea la imagen de las monedas
        this.coinImg = graphics.newImage("coin.png");

        // Se crean las posiciones de los elementos del header
        this.headerCoinsTextPos = new Vector(this.worldWidth * 0.84f + COINS_IMAGE_SIZE / 2.0f, this.HEADER_OFFSET + this.HEADER_SIZE / 2.0f);
        this.headerCoinsFont = graphics.newFont(this.FONT_NAME, COINS_IMAGE_SIZE, false, false);
        this.headerCoinsImgPos = new Vector(this.headerCoinsTextPos.x - this.COINS_IMAGE_SIZE, this.headerCoinsTextPos.y);
    }


    // Anade un objeto a la lista
    private void addItem(String key, ShopItem item) {
        // Si el objeto no esta ya en la lista
        if (!items.containsKey(key)) {
            // Calcula su posicion dependiendo del numero de objetos que haya en la lista antes de anadirlo
            float x = (items.size() % ITEMS_PER_ROW) * (this.itemSize + this.ITEM_OFFSET) + this.HEADER_OFFSET + this.itemSize / 2;
            float y = (items.size() / ITEMS_PER_ROW) * (this.itemSize + this.FONT_SIZE * 3) + this.HEADER_SIZE * 2.3f + this.itemSize / 2;

            // Cambia la posicion del objeto
            item.setPos(x, y);

            // Lo anade a la escena y a la lista de objetos
            addGameObject(item);
            items.put(key, item);
        }
    }

    private void addBgColor(String key, int r, int g, int b, int a) {
        if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255 && a >= 0 && a <= 255) {
            Color col = new Color(r, g, b, a);
            ShopBgColor color = new ShopBgColor(itemSize, itemSize, this.BUTTON_SOUND,
                    50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, col);

            // Anade la funcion para que al seleccionar el objeto se deseleccionen el resto
            color.setOnSelect(() -> {
                for (ShopBgColor c : colors) {
                    c.setSelected(false);
                }
            });

            // Anade el objeto al mapa de objetos y el color a la lista de colores
            addItem(key, color);
            colors.add(color);
        }
        else {
            System.out.println("Color out of valid range");
        }
    }

    private void addBallSkin(String key, String imgPath, int id) {
        IImage img = getEngine().getGraphics().newImage(imgPath);
        ShopBallSkin skin = new ShopBallSkin(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, img, id);
        addItem(key, skin);
    }

    private void readItems() {
        JSONObject allItems = gameManager.getShopJsonObject();
        JSONObject savedItems = gameManager.getSavedShopJsonObject();

        // Si se ha leido el archivo de la tienda y hay progreso de la tienda guardado
        if (allItems != null) {
            // Obtiene el array de objetos
            JSONArray objects = (JSONArray) allItems.get("items");

            // Si el array de objetos es valido
            if (objects != null) {
                // Recorre todos los objetos y los anade a la tienda
                for (int i = 0; i < objects.length(); i++) {
                    JSONObject obj = objects.getJSONObject(i);
                    if (Objects.equals((String) obj.get("type"), "bgColor")) {
                        addBgColor((String) obj.get("id"), (int) obj.get("r"), (int) obj.get("g"), (int) obj.get("b"), (int) obj.get("a"));
                    } else if (Objects.equals((String) obj.get("type"), "ballSkin")) {
                        addBallSkin((String) obj.get("id"), (String) obj.get("path"), (int) obj.get("colorId"));
                    }
                }
            }
        }

        // Si hay progreso guardado en la tienda
        if (savedItems != null) {
            // Recorre todos los objetos guardados
            Iterator<String> keys = savedItems.keys();
            while (keys.hasNext()) {
                String key = keys.next();

                // Obtiene los atributos del objeto con la key actual y
                // si no son nulos y el objeto existe en la tienda
                JSONObject obj = savedItems.getJSONObject(key);
                if (obj != null && this.items.containsKey(key)) {
                    // Obtiene el objeto con esa id de la tienda
                    ShopItem item = this.items.get(key);

                    // Si el objeto leido tiene el atributo bought, se
                    // pone el valor bought del objeto por el leido
                    if (obj.get("bought") != null) {
                        item.setBought((Boolean) obj.get("bought"));
                    }

                    // Si el objeto leido tiene el atributo active y
                    // el objeto esta activo, se selecciona
                    if (obj.get("active") != null) {
                        if ((Boolean) obj.get("active")) {
                            item.select();
                        }
                    }
                }
            }
        }
    }
}
