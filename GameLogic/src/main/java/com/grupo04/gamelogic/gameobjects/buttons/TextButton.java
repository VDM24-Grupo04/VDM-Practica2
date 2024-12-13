package com.grupo04.gamelogic.gameobjects.buttons;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.engine.interfaces.ITouchEvent;

import java.util.List;

public class TextButton extends Button {
    private final float arc;
    private final Color baseCol;
    private final Color pointerOverCol;
    private Color bgCol;

    private IFont font;
    private final String text;
    private final float fontSize;
    private final String fontName;
    private final Color fontColor;
    private final boolean bold;

    private IImage image;
    private final int imageSize;
    private final Vector imagePos;
    private String imagePath;
    private boolean hasCalculateParams;

    public TextButton(Vector pos,
                      float width, float height, float arc, Color baseCol, Color pointerOverCol,
                      String text, String fontName, Color fontColor, boolean bold,
                      String onClickSoundPath, Callback onClick) {
        super(pos, width, height, onClickSoundPath, onClick);

        this.arc = arc;
        this.baseCol = baseCol;
        this.pointerOverCol = pointerOverCol;
        this.bgCol = this.baseCol;

        this.font = null;
        this.text = text;
        this.fontSize = this.height / 1.8f;
        this.fontName = fontName;
        this.fontColor = fontColor;
        this.bold = bold;

        this.image = null;
        this.imageSize = (int) (this.fontSize / 1.5f);
        this.imagePos = new Vector(pos);
        this.imagePath = null;
        this.hasCalculateParams = false;
    }

    // Texto normal de color negro
    public TextButton(Vector pos,
                      float width, float height, float arc, Color baseCol, Color pointerOverCol,
                      String text, String fontName,
                      String onClickSoundPath, Callback onClick) {
        this(pos, width, height, arc, baseCol, pointerOverCol, text, fontName,
                new Color(0, 0, 0), false, onClickSoundPath, onClick);
    }

    // Sin sonido al pulsar
    public TextButton(Vector pos,
                      float width, float height, float arc, Color baseCol, Color pointerOverCol,
                      String text, String fontName, Color fontColor, boolean bold,
                      Callback onClick) {
        this(pos, width, height, arc, baseCol, pointerOverCol, text, fontName, fontColor, bold, null, onClick);
    }

    public TextButton(Vector pos,
                      float width, float height, float arc, Color baseCol, Color pointerOverCol,
                      String text, String fontName, Color fontColor, boolean bold,
                      String imagePath,
                      String onClickSoundPath, Callback onClick) {
        this(pos, width, height, arc, baseCol, pointerOverCol, text, fontName, fontColor, bold, onClickSoundPath, onClick);

        this.imagePath = imagePath;
    }

    @Override
    public void init() {
        super.init();

        IGraphics graphics = this.scene.getEngine().getGraphics();
        this.font = graphics.newFont(this.fontName, this.fontSize, this.bold, false);
        if (this.imagePath != null) {
            this.image = graphics.newImage(this.imagePath);
        }
    }

    @Override
    public void handleInput(List<ITouchEvent> touchEvents) {
        super.handleInput(touchEvents);

        for (ITouchEvent touchEvent : touchEvents) {
            if (touchEvent.getType() == ITouchEvent.TouchEventType.MOTION) {
                if (withinArea(touchEvent.getPos())) {
                    // Se podria añadir un sonido cuando este encima
                    this.bgCol = this.pointerOverCol;
                } else {
                    // Se podria añadir un sonido cuando no este encima
                    this.bgCol = this.baseCol;
                }
            }
        }
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.setColor(this.bgCol);
        graphics.fillRoundRectangle(this.pos, this.width, this.height, this.arc);

        graphics.setFont(this.font);

        if (this.image != null) {
            if (!this.hasCalculateParams) {
                float textWidth = graphics.getTextWidth(this.text);
                float offset = (this.width - textWidth) / 5f;
                this.imagePos.x -= (textWidth / 2f + offset);
                this.hasCalculateParams = true;
            }
            graphics.drawImage(this.image, this.imagePos, this.imageSize, this.imageSize);
        }

        graphics.setColor(this.fontColor);
        graphics.drawText(this.text, this.pos);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.font = null;
    }
}
