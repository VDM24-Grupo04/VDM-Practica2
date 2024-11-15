package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.gameobjects.shopItems.ShopColor;

public class ShopScene extends Scene {

    public ShopScene(IEngine engine) {
        super(-5, engine, 400, 600, new Color(255, 255, 255));

        // Al iniciar la escena se hace un fade out
        setFade(Fade.OUT, 0.25);

    }

    @Override
    public void init() {
        String BUTTON_SOUND = "button.wav";
        float BUTTON_WIDTH = 100f;
        float BUTTON_HEIGHT = 100f;
        IGraphics graphics = getEngine().getGraphics();

        IImage coinImg = graphics.newImage("close.png");

         ShopColor shopColor = new ShopColor(new Vector(100f, 100f), BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_SOUND,
                 50, coinImg, new Color(255, 255, 0));
        addGameObject(shopColor);

        super.init();
    }
}

