package com.grupo04.gamelogic.gameobjects.shopItems;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;

public class ShopBgColor extends ShopItem {
    private Color color;
    private Callback baseOnSelect;

    public ShopBgColor(float width, float height, String onClickSoundPath,
                       int price, IFont priceFont, Color priceColor,
                       IImage coinImage, int coinSize, Color selectedColor, Color color)
    {
        super(width, height, onClickSoundPath, price, priceFont, priceColor, coinImage, coinSize, selectedColor);

        this.color = color;

        // Al deseleccionar el objeto, se pone el color del fondo en el gameManager a null
        super.onDeselect = () -> {
            scene.getGameManager().setBgColor(null);
        };

        // El resto da funcion de seleccion se establece desde fuera con el setOnSelect(),
        // ya que al seleccionar un objeto ademas hay que deseleccionar el resto
        this.baseOnSelect = () -> {
            // Selecciona el objeto y cambia el color del fondo
            super.setSelected(true);
            scene.getGameManager().setBgColor(this.color);
        };
    }

    @Override
    public void render(IGraphics graphics) {
        // Pinta primero el rectangulo con el color y luego pinta los elementos
        // del padre (para que se pinte el rectangulo por debajo del borde)
        graphics.setColor(this.color);
        graphics.fillRoundRectangle(super.pos, super.width, super.height, this.BORDER_RADIUS);

        super.render(graphics);
    }

    @Override
    public void dereference() {
        super.dereference();
        this.color = null;
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
