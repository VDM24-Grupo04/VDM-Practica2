package com.grupo04.gamelogic.gameobjects;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.GameObject;

public class TextWithIcon extends GameObject {
    private Vector pos;
    private float spacing;

    private float fontSize;
    private Vector textPos;
    private String text;
    private String fontName;
    private Color fontColor;
    private boolean bold;
    private IFont font;

    private int imageSize;
    private Vector imagePos;
    private String imagePath;
    private IImage image;

    private boolean hasCalculateParams;

    public TextWithIcon(Vector pos, float size, float spacing,
                        String text, String fontName, Color fontColor, boolean bold,
                        String imagePath) {
        super();

        this.pos = pos;
        this.spacing = spacing;

        this.fontSize = size;
        this.text = text;
        this.fontName = fontName;
        this.fontColor = fontColor;
        this.bold = bold;

        this.imageSize = (int) (size / 1.3f);
        this.imagePath = imagePath;

        this.calculatePos();
    }

    private void calculatePos() {
        this.textPos = new Vector(this.pos);
        this.textPos.x += this.spacing / 2f;
        this.imagePos = new Vector(this.pos);
        this.imagePos.x -= (this.imageSize / 2f + this.spacing / 2f);
        this.hasCalculateParams = false;
    }

    @Override
    public void init() {
        IGraphics graphics = this.scene.getEngine().getGraphics();
        this.font = graphics.newFont(this.fontName, this.fontSize, this.bold, false);
        this.image = graphics.newImage(this.imagePath);
    }

    @Override
    public void render(IGraphics graphics) {
        graphics.setFont(this.font);

        if (!this.hasCalculateParams) {
            float coinsTextWidth = graphics.getTextWidth(this.text);
            this.textPos.x += coinsTextWidth / 2f;
            this.hasCalculateParams = true;
        }

        graphics.drawImage(this.image, this.imagePos, imageSize, imageSize);

        graphics.setColor(this.fontColor);
        graphics.drawText(this.text, this.textPos);
    }

    public void setText(String newText) {
        this.text = newText;
    }

    public void setPos(Vector newPos) {
        this.pos = new Vector(newPos);
        this.calculatePos();
    }

    public void setPos(float x) {
        this.pos.x = x;
        this.calculatePos();
    }
}
