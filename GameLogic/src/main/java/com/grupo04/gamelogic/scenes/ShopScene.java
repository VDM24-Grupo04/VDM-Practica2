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
import com.grupo04.gamelogic.gameobjects.shopItems.ShopColor;

import java.util.HashMap;

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
    Color SELECTED_COLOR = new Color(0, 255, 0);
    int coinsImageSize = this.FONT_SIZE;
    private HashMap<String, ShopItem> items;

    public ShopScene(IEngine engine) {
        super(engine, 400, 600, new Color(255, 255, 255));

        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);

        items = new HashMap<String, ShopItem>();
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
        test();

        super.init();
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.setFont(this.headerCoinsFont);
        graphics.setColor(this.TEXT_COLOR);
        graphics.drawText( ((Integer)(gameManager.getCoins())).toString(), headerCoinsTextPos, false, true);
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
    public void shutdown() {
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
    private void addItem(ShopItem item, String id) {
        // Si el objeto no esta ya en la lista
        if (!items.containsKey(id)) {
            // Calcula su posicion dependiendo del numero de objetos que haya en la lista antes de anadirlo
            float x = (items.size() % ITEMS_PER_ROW) * (this.itemSize + this.ITEM_OFFSET) + this.HEADER_OFFSET +  this.itemSize / 2;
            float y = (items.size() / ITEMS_PER_ROW) * (this.itemSize + this.FONT_SIZE * 3) + this.HEADER_SIZE * 2.3f + this.itemSize / 2;

            // Cambia la posicion del objeto
            item.setPos(x, y);

            // Lo anade a la escena y a la lista de objetos
            addGameObject(item);
            items.put(id, item);
        }
    }

    private void test() {
        this.gameManager.setCoins(129);

        ShopColor shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "1");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "2");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "3");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "4");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "5");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "6");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "7");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "8");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "9");

        shopColor = new ShopColor(itemSize, itemSize, this.BUTTON_SOUND,
                50, pricesFont, this.TEXT_COLOR, coinImg, coinsImageSize, SELECTED_COLOR, new Color(255, 255, 0));
        addItem(shopColor, "10");
    }

}
