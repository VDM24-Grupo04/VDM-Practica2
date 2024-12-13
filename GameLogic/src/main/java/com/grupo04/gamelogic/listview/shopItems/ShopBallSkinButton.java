package com.grupo04.gamelogic.listview.shopItems;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.GameManager;
import com.grupo04.gamelogic.listview.ShopItemButton;

public class ShopBallSkinButton extends ShopItemButton {
    private IImage image;
    private final int colorId;

    public ShopBallSkinButton(int price, IFont priceFont, Color priceColor, IImage coinImage, int coinSize,
                              Color selectedColor, ISound buttonClickSound, GameManager gameManager,
                              IImage image, int colorId) {
        super(price, priceFont, priceColor, coinImage, coinSize, selectedColor, buttonClickSound, gameManager);

        this.image = image;
        this.colorId = colorId;

        // Al deseleccionar el objeto, se pone el color del fondo en el gameManager a null
        super.onDeselect = () -> {
            gameManager.setBallSkin(this.colorId, null);
        };
        super.onSelect = () -> {
            gameManager.setBallSkin(this.colorId, this.image);
        };
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.drawImage(image, super.pos, (int) (width * 0.8f), (int) (height * 0.8f));

        super.render(graphics);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.image = null;
    }
}
