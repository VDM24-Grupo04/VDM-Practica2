package com.grupo04.gamelogic.gameobjects;

import com.grupo04.androidengine.IEngine;
import com.grupo04.androidengine.graphics.IGraphics;
import com.grupo04.androidengine.graphics.IImage;
import com.grupo04.androidengine.utilities.Callback;
import com.grupo04.androidengine.utilities.Vector;

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
        this.image = graphics.newImage(imagePath);
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.drawImage(this.image, this.pos, (int) this.width, (int) this.height);
    }
}