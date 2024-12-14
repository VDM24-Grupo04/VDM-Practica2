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
    private final int ELEMENT_OFFSET = 15;
    private final float IMG_SCALE = 0.5f;
    private final int RADIUS = 15;

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

        // El resto de la funcion de seleccion se establece desde fuera con el setOnSelect(),
        // ya que al seleccionar un objeto ademas hay que deseleccionar el resto
        super.baseOnSelect = () -> {
            // Selecciona el objeto y cambia el color del fondo
            super.setSelected(true);
            gameManager.setBallSkin(this.colorId, this.image);
        };
    }

    @Override
    public void render(IGraphics graphics) {
        super.pos.x += ELEMENT_OFFSET;
        super.pos.y += ELEMENT_OFFSET;
        graphics.drawImage(this.image, super.pos, (int) (this.width * IMG_SCALE), (int) (this.height * IMG_SCALE));

        super.pos.x -= ELEMENT_OFFSET * 2;
        super.pos.y -= ELEMENT_OFFSET * 2;
        graphics.setColor(BubbleColors.getColor(this.colorId));
        graphics.fillCircle(super.pos, RADIUS);

        super.pos.x += ELEMENT_OFFSET;
        super.pos.y += ELEMENT_OFFSET;
        super.render(graphics);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.image = null;
    }
}
