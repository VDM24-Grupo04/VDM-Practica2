package com.grupo04.gamelogic.gameobjects.shopItems;

import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.gameobjects.ShopItem;

public class ShopColor extends ShopItem {
    private Color color;

    public ShopColor(Vector pos, float width, float height, String onClickSoundPath, int price, IImage coinImage, Color color) {
        super(pos, width, height, onClickSoundPath, price, coinImage);

        this.color = color;

        // Se cambia la funcion a llamar al pulsar el boton (la funcion es unica
        // para cada clase hija de ShopItem y lo que cambian son los parametros)
        super.setOnClick(()-> {
            System.out.println("Color bought");
        });
    }

    @Override
    public void render(IGraphics graphics) {
        // Pinta primero el rectangulo con el color y luego pinta los elementos
        // del padre (para que se pinte el rectangulo por debajo del borde)
        graphics.setColor(this.color);
        graphics.fillRoundRectangle(super.pos, super.width, super.height, super.BORDER_RADIUS);
        super.render(graphics);
    }

    @Override
    public void dereference() {
        super.dereference();
        this.color = null;
    }
}
