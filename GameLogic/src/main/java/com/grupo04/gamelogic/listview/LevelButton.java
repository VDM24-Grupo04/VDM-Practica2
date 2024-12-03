package com.grupo04.gamelogic.listview;

import com.grupo04.engine.interfaces.IAudio;
import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;

public class LevelButton extends ListviewButton {
    private boolean locked;

    private Color bgCol;
    private final Color unlockedCol;
    private final Color unlockedPointoverCol;

    private Color borderCol;
    private float arc;
    private float borderWidth;

    private IFont font;
    private final String text;
    private final Color fontColor;

    private IImage image;
    private int imageSize;

    private IAudio audio;
    private ISound onClickSound;
    private Callback onClick;

    public LevelButton(int levelNumber, boolean locked, Color[] colors,
                       Color borderCol, float arc, float borderWidth,
                       Color fontColor, IFont font, IImage image, ISound onClickSound,
                       Callback onClick) {

        this.locked = locked;

        this.unlockedCol = colors[0];
        this.bgCol = this.unlockedCol;
        if (this.locked) {
            this.bgCol = colors[2];
        }
        this.unlockedPointoverCol = colors[1];

        this.borderCol = borderCol;
        this.arc = arc;
        this.borderWidth = borderWidth;

        this.text = Integer.toString(levelNumber);
        this.font = font;
        this.fontColor = fontColor;

        this.imageSize = (int) (height / 1.7f);
        this.image = image;

        this.audio = null;
        this.onClickSound = onClickSound;

        this.onClick = onClick;
    }

    @Override
    public void init(IEngine engine, Vector relativePos, Vector listviewPos, float width, float height) {
        super.init(engine, relativePos, listviewPos, width, height);

        this.audio = engine.getAudio();
    }

    @Override
    public void handleEvent(ITouchEvent touchEvent) {
        if (!this.locked) {
            switch (touchEvent.getType()) {
                case PRESS:
                    if (withinArea(touchEvent.getPos())) {
                        if (this.onClickSound != null) {
                            this.audio.playSound(this.onClickSound);
                        }
                        this.onClick.call();
                    }
                    break;
                case MOTION:
                    if (withinArea(touchEvent.getPos())) {
                        // Se podria añadir un sonido cuando este encima
                        this.bgCol = this.unlockedPointoverCol;
                    } else {
                        // Se podria añadir un sonido cuando no este encima
                        this.bgCol = this.unlockedCol;
                    }
            }
        }
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.setColor(this.borderCol);
        graphics.drawRoundRectangle(this.pos, this.width, this.height, this.arc, this.borderWidth);

        graphics.setColor(this.bgCol);
        graphics.fillRoundRectangle(this.pos, this.width, this.height, this.arc);
        graphics.setColor(this.fontColor);

        if (this.locked) {
            graphics.drawImage(image, this.pos, this.imageSize, this.imageSize);
        } else {
            graphics.setColor(this.fontColor);
            graphics.setFont(this.font);
            graphics.drawText(this.text, this.pos);
        }
    }
}
