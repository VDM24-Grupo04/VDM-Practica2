package com.grupo04.gamelogic.gameobjects.buttons;

import com.grupo04.engine.interfaces.IAudio;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;

public class LevelButton {
    private Vector relativePos;
    private Vector pos;

    private float width;
    private float height;
    private Vector topLeft;
    private float arc;
    private float borderWidth;

    private final Color baseCol;
    private final Color pointerOverCol;
    private Color bgCol;
    private Color borderCol;

    private IFont font;
    private final String text;
    private String fontName;
    private float fontSize;
    private boolean bold;
    private final Color fontColor;

    private IAudio audio;
    private ISound onClickSound;
    private String onClickSoundPath;

    private Callback onClick;

    public LevelButton(int levelNumber, Vector listviewPos, Vector relativePos, float width, float height, float arc, float borderWidth,
                       Color baseCol, Color pointerOverCol, Color borderCol,
                       String fontName, boolean bold, Color fontColor,
                       String onClickSoundPath,
                       Callback onClick) {
        this.relativePos = relativePos;
        this.pos = listviewPos.plus(this.relativePos);

        // Punto superior izquierdo
        this.topLeft = new Vector(this.pos.x - width / 2f, this.pos.y - height / 2f);

        this.width = width;
        this.height = height;

        this.arc = arc;
        this.borderWidth = borderWidth;

        this.baseCol = baseCol;
        this.pointerOverCol = pointerOverCol;
        this.bgCol = this.baseCol;
        this.borderCol = borderCol;

        this.text = Integer.toString(levelNumber);
        this.fontName = fontName;
        this.fontSize = this.height / 1.8f;
        this.bold = bold;
        this.font = null;
        this.fontColor = fontColor;
        this.onClickSoundPath = onClickSoundPath;

        this.audio = null;
        this.onClickSound = null;

        this.onClick = onClick;
    }

    private boolean withinArea(Vector pos) {
        return pos.x > this.topLeft.x && pos.x < this.topLeft.x + width &&
                pos.y > this.topLeft.y && pos.y < this.topLeft.y + height;
    }

    public void init(IGraphics graphics, IAudio audio) {
        this.font = graphics.newFont(fontName, fontSize, bold, false);
        this.audio = audio;
        this.onClickSound = audio.newSound(onClickSoundPath);
    }

    public void handleEvent(ITouchEvent touchEvent) {
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
                    this.bgCol = this.pointerOverCol;
                } else {
                    // Se podria añadir un sonido cuando no este encima
                    this.bgCol = this.baseCol;
                }
        }
    }

    public void render(IGraphics graphics) {
        graphics.setColor(this.borderCol);
        graphics.drawRoundRectangle(this.pos, this.width, this.height, this.arc, this.borderWidth);

        graphics.setColor(this.bgCol);
        graphics.fillRoundRectangle(this.pos, this.width, this.height, this.arc);
        graphics.setColor(this.fontColor);

        graphics.setColor(this.fontColor);
        graphics.setFont(this.font);
        graphics.drawText(this.text, this.pos);
    }

    public void move(float listviewPosY) {
        this.pos.y = listviewPosY + this.relativePos.y;
        this.topLeft.y = this.pos.y - this.height / 2f;
    }

    public void resetColor() {
        this.bgCol = this.baseCol;
    }

    public float getHeight() {
        return this.height;
    }
}
