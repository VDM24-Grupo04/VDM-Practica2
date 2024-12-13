package com.grupo04.gamelogic.listview.shopItems;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.BubbleColors;
import com.grupo04.gamelogic.GameManager;
import com.grupo04.gamelogic.listview.ShopItemButton;

public class ShopBallSkinButton extends ShopItemButton {
    private IImage image;
    private final int colorId;

    private final int elementOffset = 15;
    private final float IMG_SCALE = 0.5f;
    private final int radius = 15;

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
        super.pos.x += elementOffset;
        super.pos.y += elementOffset;
        graphics.drawImage(image, super.pos, (int) (width * this.IMG_SCALE), (int) (height * this.IMG_SCALE));

        super.pos.x -= elementOffset * 2;
        super.pos.y -= elementOffset * 2;
        graphics.setColor(BubbleColors.getColor(this.colorId));
        graphics.fillCircle(super.pos, this.radius);

        super.pos.x += elementOffset;
        super.pos.y += elementOffset;
        super.render(graphics);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.image = null;
    }
}
