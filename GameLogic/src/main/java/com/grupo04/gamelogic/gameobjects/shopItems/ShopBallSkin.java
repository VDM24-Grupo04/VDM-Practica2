package com.grupo04.gamelogic.gameobjects.shopItems;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.gameobjects.ShopItem;

public class ShopBallSkin extends ShopItem {
    private IImage image;
    private int colorId;

    public ShopBallSkin(float width, float height, String onClickSoundPath,
                       int price, IFont priceFont, Color priceColor,
                       IImage coinImage, int coinSize, Color selectedColor, IImage image, int colorId)
    {
        super(width, height, onClickSoundPath, price, priceFont, priceColor, coinImage, coinSize, selectedColor);

        this.image = image;
        this.colorId = colorId;

        // Al deseleccionar el objeto, se pone el color del fondo en el gameManager a null
        super.onDeselect = () -> {
            scene.getGameManager().setBallSkin(this.colorId, null);
        };
        super.onSelect = () -> {
            scene.getGameManager().setBallSkin(this.colorId, this.image);
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
