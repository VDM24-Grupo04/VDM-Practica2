package com.grupo04.gamelogic.gameobjects;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.engine.interfaces.ITouchEvent;

import java.util.List;

public class TextButton extends Button {
    protected final float arc;
    private final Color baseCol;
    private final Color pointerOverCol;
    private Color bgCol;

    private IFont font;
    private final String text;
    private final String fontName;
    private final Color fontColor;
    private final boolean bold;

    public TextButton(Vector pos,
                      float width, float height, float arc, Color baseCol, Color pointerOverCol,
                      String text, String fontName, Color fontColor, boolean bold, String onClickSoundPath, Callback onClick) {
        super(pos, width, height, onClickSoundPath, onClick);

        this.arc = arc;
        this.baseCol = baseCol;
        this.pointerOverCol = pointerOverCol;
        this.bgCol = this.baseCol;

        this.font = null;
        this.text = text;
        this.fontName = fontName;
        this.fontColor = fontColor;
        this.bold = bold;
    }

    // Texto normal de color negro
    public TextButton(Vector pos,
                      float width, float height, float arc, Color baseCol, Color pointerOverCol,
                      String text, String fontName, String onClickSoundPath, Callback onClick) {
        this(pos, width, height, arc, baseCol, pointerOverCol, text, fontName,
                new Color(0, 0, 0), false, onClickSoundPath, onClick);
    }

    // Sin sonido al pulsar
    public TextButton(Vector pos,
                      float width, float height, float arc, Color baseCol, Color pointerOverCol,
                      String text, String fontName, Color fontColor, boolean bold, Callback onClick) {
        this(pos, width, height, arc, baseCol, pointerOverCol, text, fontName, fontColor, bold, null, onClick);
    }

    // Texto normal de color negro y sin sonido al pulsar
    public TextButton(Vector pos,
                      float width, float height, float arc, Color baseCol, Color pointerOverCol,
                      String text, String fontName, Callback onClick) {
        this(pos, width, height, arc, baseCol, pointerOverCol, text, fontName, null, onClick);
    }

    @Override
    public void init() {
        super.init();
        
        IEngine engine = this.scene.getEngine();
        IGraphics graphics = engine.getGraphics();
        this.font = graphics.newFont(this.fontName, this.height / 1.7f, this.bold, false);
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

        graphics.setColor(this.fontColor);
        graphics.setFont(this.font);
        graphics.drawText(this.text, this.pos);
    }

    @Override
    public void dereference() {
        super.dereference();
        this.font = null;
    }
}
