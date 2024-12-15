package com.grupo04.gamelogic.scenes;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.Scene;
import com.grupo04.gamelogic.gameobjects.Text;

public class CheaterScene extends Scene {
    private final String TEXT_FONT = "TheMeshroomRegular.ttf";
    private final Color TEXT_COLOR = new Color(0, 0, 0);
    private final float TEXT_SIZE = 70;
    private final float[] TEXT_INDENTING = new float[]{3f, -10f};
    private final float TEXT_LINE_SPACING = -30f;

    public CheaterScene(IEngine engine, Color UIColor) {
        super(engine, 400, 600, new Color(255, 0, 0), UIColor);

        Text title = new Text(new Vector(this.worldWidth / 2f, this.worldHeight / 2f), new String[]{"Cheater!"},
                TEXT_FONT, TEXT_SIZE, false, false, TEXT_COLOR,
                TEXT_INDENTING, TEXT_LINE_SPACING);
        addGameObject(title);

        setFade(Fade.OUT, 0.0);
    }

    public CheaterScene(IEngine engine) {
        this(engine, null);
    }
}
