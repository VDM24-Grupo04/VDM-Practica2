package com.grupo04.engine;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;

public abstract class Graphics implements IGraphics {
    protected int worldWidth;
    protected int worldHeight;
    protected float scale;
    protected float offsetX;
    protected float offsetY;
    protected Color bgColor;

    protected Graphics() {
        this.worldWidth = 0;
        this.worldHeight = 0;
        this.scale = 1.0f;
        this.offsetX = 0.0f;
        this.offsetY = 0.0f;
        this.bgColor = new Color(255, 255, 255, 255);
    }

    protected abstract void calculateTransform();

    protected abstract void scale(float scale);

    protected abstract void translate(float offsetX, float offsetY);

    protected abstract int getWindowWidth();

    protected abstract int getWindowHeight();

    protected abstract void prepareFrame();

    protected abstract boolean endFrame();

    public abstract void render(IScene currentScene);

    public boolean isWindowInitialized() {
        return this.getWindowWidth() != 0;
    }

    public abstract Vector screenToWorldPoint(Vector point);

    public void setWorldSize(int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    @Override
    public void setClearColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    @Override
    public abstract void clear(Color color);

    @Override
    public abstract void setColor(Color color);

    @Override
    public abstract void drawCircle(Vector position, float radius, float strokeWidth);

    @Override
    public abstract void fillCircle(Vector position, float radius);

    @Override
    public abstract void drawRectangle(Vector position, float w, float h, float strokeWidth);

    @Override
    public abstract void fillRectangle(Vector position, float w, float h);

    @Override
    public abstract void drawRoundRectangle(Vector position, float w, float h, float arc, float strokeWidth);

    @Override
    public abstract void fillRoundRectangle(Vector position, float w, float h, float arc);

    @Override
    public abstract void drawLine(Vector initPos, Vector endPos, float strokeWidth);

    @Override
    public abstract void drawHexagon(Vector center, float radius, float rotInDegrees, float strokeWidth);

    @Override
    public void drawHexagon(Vector center, float radius, float strokeWidth) {
        drawHexagon(center, radius, 0, strokeWidth);
    }

    @Override
    public abstract IImage newImage(String name);

    @Override
    public abstract void drawImage(IImage img, Vector position);

    @Override
    public abstract void drawImage(IImage img, Vector position, int w, int h);

    @Override
    public abstract IFont newFont(String name, float size, boolean bold, boolean italic);

    @Override
    public abstract void setFont(IFont font);

    @Override
    public abstract void drawText(String text, Vector position, boolean centerX, boolean centerY);

    @Override
    public void drawText(String text, Vector position) {
        drawText(text, position, true, true);
    }

    @Override
    public abstract int getTextWidth(String text);

    @Override
    public abstract int getTextHeight(String text);
}
