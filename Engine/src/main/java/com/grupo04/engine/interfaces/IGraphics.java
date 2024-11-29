package com.grupo04.engine.interfaces;

import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;

public interface IGraphics {
    void setClearColor(Color clearColor);
    void clear(Color color);

    void setColor(Color color);

    void drawCircle(Vector position, float radius, float strokeWidth);
    void fillCircle(Vector position, float radius);
    void drawRectangle(Vector position, float w, float h, float strokeWidth);
    void fillRectangle(Vector position, float w, float h);
    void drawRoundRectangle(Vector position, float w, float h, float arc, float strokeWidth);
    void fillRoundRectangle(Vector position, float w, float h, float arc);
    void drawLine(Vector initPos, Vector endPos, float strokeWidth);
    void drawHexagon(Vector center, float radius, float rotInDegrees, float strokeWidth);
    void drawHexagon(Vector center, float radius, float strokeWidth);

    IImage newImage(String name);
    void drawImage(IImage img, Vector position);
    void drawImage(IImage img, Vector position, int w, int h);

    IFont newFont(String name, float size, boolean bold, boolean italic);
    void setFont(IFont font);
    void drawText(String text, Vector position, boolean centerX, boolean centerY);
    void drawText(String text, Vector position);
    int getTextWidth(String text);
    int getTextHeight(String text);
}
