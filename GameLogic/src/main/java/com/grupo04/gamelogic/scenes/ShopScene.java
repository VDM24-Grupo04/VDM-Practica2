package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.BubbleColors;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.gameobjects.buttons.ImageButton;
import com.grupo04.gamelogic.gameobjects.Text;
import com.grupo04.gamelogic.listview.ShopItemButton;
import com.grupo04.gamelogic.listview.VerticalListview;
import com.grupo04.gamelogic.listview.shopItems.ShopBallSkinButton;
import com.grupo04.gamelogic.listview.shopItems.ShopBgColorButton;
import com.grupo04.gamelogic.listview.shopItems.ShopBgImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ShopScene extends Scene {
    private final String FONT_NAME = "kimberley.ttf";
    private final int HEADER_SIZE = 40, HEADER_OFFSET = 20, FONT_SIZE = 20;
    private final int HEADER_REAL_SIZE = HEADER_SIZE + HEADER_OFFSET;

    private final String BUTTON_SOUND = "button.wav";
    private ISound buttonSound;

    private final Color TEXT_COLOR = new Color(0, 0, 0);
    private final float COINS_IMAGE_SIZE = HEADER_SIZE * 0.6f;
    private final Color SELECTED_COLOR = new Color(0, 255, 0);
    private final int ITEMS_PER_ROW = 4, ITEM_OFFSET = 10;

    private IImage coinImg;
    private IFont headerCoinsFont;
    private Vector headerCoinsTextPos;
    private Vector headerCoinsImgPos;

    private IFont pricesFont;
    private float itemSize;
    private final int coinsImageSize = FONT_SIZE;

    VerticalListview listview;

    private final HashMap<String, ShopItemButton> items;
    private final List<ShopBgColorButton> themeColors;
    private final ArrayList<ShopBallSkinButton>[] ballSkins;
    private final List<ShopBgImageButton> bgImages;

    public ShopScene(IEngine engine, Color UIColor) {
        super(engine, 400, 600, UIColor);

        this.items = new HashMap<>();
        this.themeColors = new ArrayList<>();
        this.ballSkins = new ArrayList[BubbleColors.getTotalColors()];
        for(int i = 0; i < this.ballSkins.length; i++) {
            this.ballSkins[i] = new ArrayList<>();
        }
        this.bgImages = new ArrayList<>();
    }

    public ShopScene(IEngine engine) {
        this(engine, null);
    }

    @Override
    public void init() {
        IGraphics graphics = getEngine().getGraphics();

        float height = this.worldHeight - HEADER_REAL_SIZE;
        float y = HEADER_REAL_SIZE + height / 1.95f;
        this.listview = new VerticalListview(new Vector(this.worldWidth / 2f, y),
                this.worldWidth, height, ITEMS_PER_ROW, ITEM_OFFSET, this.itemSize + this.FONT_SIZE * 3);
        addGameObject(this.listview);

        // Se anaden los objetos del header
        createHeader(graphics);

        // Se crea la fuente y el audio de los botones
        this.pricesFont = graphics.newFont(FONT_NAME, FONT_SIZE, true, false);
        this.buttonSound = getEngine().getAudio().newSound(BUTTON_SOUND);

        // Se calcula el tamano de los objetos dependiendo del numero de objetos por fila
        float freeSpace = this.worldWidth - HEADER_OFFSET * 2 - ITEM_OFFSET * (ITEMS_PER_ROW - 1);
        this.itemSize = freeSpace / ITEMS_PER_ROW;

        // Anadir los objetos
        readItems();

        super.init();

        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);
    }

    @Override
    public void render(IGraphics graphics) {
        super.render(graphics);

        graphics.setFont(this.headerCoinsFont);
        graphics.setColor(TEXT_COLOR);
        graphics.drawText(((Integer) (this.gameManager.getCoins())).toString(), this.headerCoinsTextPos, false, true);
        graphics.drawImage(this.coinImg, this.headerCoinsImgPos, (int) COINS_IMAGE_SIZE, (int) COINS_IMAGE_SIZE);
    }

    @Override
    public void dereference() {
        saveJson();

        super.dereference();

        this.coinImg = null;
        this.headerCoinsFont = null;
        this.headerCoinsTextPos = null;
        this.headerCoinsImgPos = null;

        this.pricesFont = null;
        this.buttonSound = null;
        this.items.clear();
    }

    @Override
    public void saveJson() {
        JSONObject savedItems = this.gameManager.getSavedShopJsonObject();
        if (savedItems == null) {
            savedItems = new JSONObject();
        }
        // Recorre todas las keys de los objetos de la tienda
        for (String key : this.items.keySet()) {
            ShopItemButton item = this.items.get(key);

            // Se crea un JsonObject en el que guardar los atributos del objeto
            JSONObject savedItem = new JSONObject();

            // Si el objeto esta comprado, se guarda que esta comprado y si esta activado
            // (Solo se guardan objetos comprados)
            if (item.getBought()) {
                savedItem.put("bought", true);
                savedItem.put("active", item.getSelected());
                savedItems.put(key, savedItem);
            }
        }

        this.gameManager.setSavedShopJsonObject(savedItems);
    }

    // Crea los elementos del header
    private void createHeader(IGraphics graphics) {
        // Se anade el boton de volver al menu inicial
        String MENU_BUTTON_IMG = "close.png";

        // UI de la parte superior
        ImageButton menuButton = new ImageButton(
                new Vector(HEADER_SIZE / 2f + HEADER_OFFSET, HEADER_OFFSET + HEADER_SIZE / 2.0f),
                HEADER_SIZE, HEADER_SIZE, MENU_BUTTON_IMG, BUTTON_SOUND,
                () -> {
                    // Al pulsar el boton se hace un fade in y cuando
                    // acaba la animacion se cambia al menu principal
                    // con animacion de fade out
                    this.setFade(Fade.IN, 0.25);
                    this.setFadeCallback(() -> {
                        TitleScene scene = new TitleScene(this.engine, this.UIColor);
                        scene.setFade(Fade.OUT, 0.25);
                        if (this.gameManager != null) {
                            this.gameManager.changeScene(scene);
                        }
                    });
                });
        addGameObject(menuButton);

        // Se anade el texto del titulo
        Text title = new Text(
                new Vector(this.worldWidth / 2f, HEADER_OFFSET + HEADER_SIZE / 2.0f),
                "Shop", FONT_NAME, HEADER_SIZE, false, false, TEXT_COLOR);
        addGameObject(title);

        // Se crea la imagen de las monedas
        this.coinImg = graphics.newImage("coin.png");

        // Se crean las posiciones de los elementos del header
        this.headerCoinsTextPos = new Vector(this.worldWidth * 0.84f + COINS_IMAGE_SIZE / 2.0f, HEADER_OFFSET + HEADER_SIZE / 2.0f);
        this.headerCoinsFont = graphics.newFont(FONT_NAME, COINS_IMAGE_SIZE, false, false);
        this.headerCoinsImgPos = new Vector(this.headerCoinsTextPos.x - COINS_IMAGE_SIZE, this.headerCoinsTextPos.y);
    }

    // Lee los objetos del json de la tienda
    private void readItems() {
        List<String> itemsKeys = this.gameManager.getShopItemsKeys();
        HashMap<String, JSONObject> itemsByKey = this.gameManager.getShopItemsByKey();
        JSONObject savedItems = this.gameManager.getSavedShopJsonObject();

        // Recorre (en orden) todas las keys de los objetos leidos y los anade a la tienda segun su tipo
        for (String key : itemsKeys) {
            JSONObject obj = itemsByKey.get(key);
            try {
                ShopItemButton item = null;
                if (Objects.equals(obj.getString("type"), "bgColor")) {
                    item = createBgColor(obj.getInt("price"), obj.getInt("r"), obj.getInt("g"), obj.getInt("b"), obj.getInt("a"));
                }
                else if (Objects.equals(obj.getString("type"), "ballSkin")) {
                    item = createBallSkin(obj.getInt("price"), obj.getString("path"), obj.getInt("colorId"));
                }
                else if (Objects.equals(obj.getString("type"), "bgImage")) {
                    item = createBgImage(obj.getInt("price"), obj.getString("path"));
                }

                if (item != null) {
                    // Anade un objeto a la lista si su id no esta ya
                    // Si el objeto no esta ya en la lista
                    if (!this.items.containsKey(key)) {
                        this.listview.addButton(item);
                        this.items.put(key, item);
                    }
                }
            }
            catch (JSONException e) {
                System.out.println("Error while trying to add item to shop: " + e.getMessage());
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
                    ShopItemButton item = this.items.get(key);

                    try {
                        // Se pone el valor bought del objeto por el leido
                        item.setBought(obj.getBoolean("bought"));
                    }
                    catch (Exception e) {
                        System.out.println("Item " + key + " doesn't have bought property");
                        item.setBought(false);
                    }

                    try {
                        // Si el objeto esta activo, se selecciona
                        if (obj.getBoolean("active")) {
                            item.select();
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Item " + key + " doesn't have active property");
                    }

                }
            }
        }
    }

    // Crea un elemento de tipo color de fondo
    private ShopItemButton createBgColor(int price, int r, int g, int b, int a) {
        if (Color.isValidColor(r, g, b, a)) {
            Color col = new Color(r, g, b, a);

            ShopBgColorButton color = new ShopBgColorButton(price, this.pricesFont, TEXT_COLOR, this.coinImg, this.coinsImageSize,
                    SELECTED_COLOR, this.buttonSound, this.gameManager, col);

            // Anade la funcion para que al seleccionar el objeto se deseleccionen el resto
            color.setOnSelectExtra(() -> {
                for (ShopBgColorButton c : this.themeColors) {
                    c.setSelected(false);
                }
            });
            // Anade el color a los colores de fondo disponibles
            this.themeColors.add(color);

            return color;
        }
        else {
            System.out.println("Color out of valid range");
        }
        return null;
    }

    // Crea un elemento de tipo skin de bola
    private ShopItemButton createBallSkin(int price, String imgPath, int id) {
        if (id < BubbleColors.getTotalColors()) {
            IImage img = getEngine().getGraphics().newImage(imgPath);
            if (img.isValid()) {
                ShopBallSkinButton skin = new ShopBallSkinButton(price, this.pricesFont, TEXT_COLOR, this.coinImg, this.coinsImageSize,
                        SELECTED_COLOR, this.buttonSound, this.gameManager, img, id);

                // Anade la funcion para que al seleccionar el objeto se deseleccionen el resto
                skin.setOnSelectExtra(() -> {
                    for (ShopBallSkinButton s : this.ballSkins[id]) {
                        s.setSelected(false);
                    }
                });
                // Anade la skin a las skins de ese color disponibles
                this.ballSkins[id].add(skin);

                return skin;
            }
            else {
                System.out.println("Invalid image path");
            }
        }
        else {
            System.out.println("Ball id doesn't match with the available balls");
        }
        return null;
    }

    private ShopItemButton createBgImage(int price, String imgPath) {
        IImage image = this.engine.getGraphics().newImage(imgPath);

        if (image.isValid()) {
            ShopBgImageButton img = new ShopBgImageButton(price, this.pricesFont, TEXT_COLOR, this.coinImg, this.coinsImageSize,
                    SELECTED_COLOR, this.buttonSound, this.gameManager, imgPath, image);

            // Anade la funcion para que al seleccionar el objeto se deseleccionen el resto
            img.setOnSelectExtra(() -> {
                for (ShopBgImageButton i : this.bgImages) {
                    i.setSelected(false);
                }
            });
            // Anade la imagen a las imagenes de fondo disponibles
            this.bgImages.add(img);

            return img;
        }
        else {
            System.out.println("Invalid image path");
        }
        return null;
    }
}
