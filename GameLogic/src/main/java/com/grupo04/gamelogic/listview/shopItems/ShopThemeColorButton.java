package com.grupo04.gamelogic.listview.shopItems;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.GameManager;
import com.grupo04.gamelogic.listview.ShopItemButton;

public class ShopThemeColorButton extends ShopItemButton {
    private Color color;

    public ShopThemeColorButton(int price, IFont priceFont, Color priceColor, IImage coinImage, int coinSize,
                                Color selectedColor, ISound buttonClickSound, GameManager gameManager, Color color) {
        super(price, priceFont, priceColor, coinImage, coinSize, selectedColor, buttonClickSound, gameManager);

        this.color = color;

        // Al deseleccionar el objeto, se pone el color del fondo en el gameManager a null
        super.onDeselect = () -> {
            gameManager.setUIColor(null);
        };

        // El resto de la funcion de seleccion se establece desde fuera con el setOnSelect(),
        // ya que al seleccionar un objeto ademas hay que deseleccionar el resto
        this.baseOnSelect = () -> {
            // Selecciona el objeto y cambia el color del fondo
            super.setSelected(true);
            gameManager.setUIColor(this.color);
        };
    }

    @Override
    public void render(IGraphics graphics) {
        // Pinta primero el rectangulo con el color y luego pinta los elementos
        // del padre (para que se pinte el rectangulo por debajo del borde)
        graphics.setColor(this.color);
        graphics.fillRoundRectangle(super.pos, super.width, super.height, BORDER_RADIUS);

        super.render(graphics);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.color = null;
    }
}
