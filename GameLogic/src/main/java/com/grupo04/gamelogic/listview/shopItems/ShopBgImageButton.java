package com.grupo04.gamelogic.listview.shopItems;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.GameManager;
import com.grupo04.gamelogic.listview.ShopItemButton;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;

public class ShopBgImageButton extends ShopItemButton {
    private final float IMG_SCALE = 0.8f;

    private String path;
    private IImage image;
    private final Callback baseOnSelect;

    public ShopBgImageButton(int price, IFont priceFont, Color priceColor, IImage coinImage, int coinSize,
                             Color selectedColor, ISound buttonClickSound, GameManager gameManager, String path) {
        super(price, priceFont, priceColor, coinImage, coinSize, selectedColor, buttonClickSound, gameManager);

        this.path = path;

        // Al deseleccionar el objeto, se pone la imagen del fondo en el gameManager a null
        super.onDeselect = () -> {
            gameManager.setBgImage("");
        };

        // El resto de la funcion de seleccion se establece desde fuera con el setOnSelect(),
        // ya que al seleccionar un objeto ademas hay que deseleccionar el resto
        this.baseOnSelect = () -> {
            // Selecciona el objeto y cambia el color del fondo
            super.setSelected(true);
            gameManager.setBgImage(this.path);
        };
    }

    @Override
    public void init(IEngine engine, Vector relativePos, Vector listviewPos, float width, float height) {
        super.init(engine, relativePos, listviewPos, width, height);

        this.image = engine.getGraphics().newImage(this.path);
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.drawImage(image, super.pos, (int) (width * this.IMG_SCALE), (int) (height * this.IMG_SCALE));
        super.render(graphics);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.image = null;
    }

    // Hace que la funcion onSelect sea una llamada a la funcion indicada
    // y una llamada a la funcion onSelect base (en vez de sobreescribirla)
    public void setOnSelect(Callback extraFunc) {
        super.onSelect = () -> {
            extraFunc.call();
            baseOnSelect.call();
        };
    }
}
