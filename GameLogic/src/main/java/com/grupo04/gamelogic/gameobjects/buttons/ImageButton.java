package com.grupo04.gamelogic.gameobjects.buttons;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Vector;

public class ImageButton extends Button {
    private IImage image;
    private final String imagePath;

    public ImageButton(Vector pos, float width, float height, String imagePath, String onClickSoundPath, Callback onClick) {
        super(pos, width, height, onClickSoundPath, onClick);

        this.imagePath = imagePath;
    }

    // Sin sonido al pulsar
    public ImageButton(Vector pos, float width, float height, String imagePath, Callback onClick) {
        this(pos, width, height, imagePath, null, onClick);
    }

    @Override
    public void init() {
        super.init();
        
        IEngine engine = this.scene.getEngine();
        IGraphics graphics = engine.getGraphics();
        this.image = graphics.newImage(this.imagePath);
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.drawImage(this.image, this.pos, (int) this.width, (int) this.height);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.image = null;
    }
}