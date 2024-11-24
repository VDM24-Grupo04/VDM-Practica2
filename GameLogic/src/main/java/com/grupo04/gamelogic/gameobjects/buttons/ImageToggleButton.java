package com.grupo04.gamelogic.gameobjects.buttons;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Vector;

public class ImageToggleButton extends ToggleButton {
    private IImage image;
    private IImage uncheckedImage;
    private final String uncheckedImagePath;
    private IImage checkedImage;
    private final String checkedImagePath;

    @Override
    protected void toggle() {
        super.toggle();
        this.image = this.uncheckedImage;
        if (this.checked) {
            this.image = this.checkedImage;
        }
    }

    public ImageToggleButton(Vector pos, int width, int height, String uncheckedImagePath, String checkedImagePath,
                             String onClickSoundPath) {
        super(pos, width, height, onClickSoundPath);

        this.uncheckedImagePath = uncheckedImagePath;
        this.checkedImagePath = checkedImagePath;
    }

    // Sin sonido al pulsar
    public ImageToggleButton(Vector pos, int width, int height, String uncheckedImagePath, String checkedImagePath) {
        this(pos, width, height, uncheckedImagePath, checkedImagePath, null);
    }

    @Override
    public void init() {
        super.init();
        IEngine engine = this.scene.getEngine();
        IGraphics graphics = engine.getGraphics();
        this.uncheckedImage = graphics.newImage(uncheckedImagePath);
        this.checkedImage = graphics.newImage(checkedImagePath);
        this.image = this.uncheckedImage;
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.drawImage(this.image, this.pos, (int) this.width, (int) this.height);
    }

    @Override
    public void dereference() {
        super.dereference();
        this.image = null;
        this.uncheckedImage = null;
        this.checkedImage = null;
    }
}
