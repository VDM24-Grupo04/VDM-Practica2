package com.grupo04.desktopengine;

import com.grupo04.engine.Graphics;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.utilities.Vector;

import javax.swing.JFrame;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;

public class DesktopGraphics extends Graphics {
    private final JFrame window;
    private Graphics2D graphics2D;
    private final BufferStrategy bufferStrategy;
    private final AffineTransform affineTransform;

    public DesktopGraphics(JFrame window, Graphics2D graphics2D, BufferStrategy bufferStrategy) {
        this.window = window;
        this.graphics2D = graphics2D;
        this.bufferStrategy = bufferStrategy;
        this.affineTransform = this.graphics2D.getTransform();
    }

    @Override
    protected void calculateTransform() {
        int widthWithoutInsets = this.getWindowWidthWithoutInsets();
        int heightWithoutInsets = this.getWindowHeightWithoutInsets();
        float tempScaleX = (float) (widthWithoutInsets) / this.worldWidth;
        float tempScaleY = (float) (heightWithoutInsets) / this.worldHeight;
        this.scale = Math.min(tempScaleX, tempScaleY);

        this.offsetX = (widthWithoutInsets - this.worldWidth * this.scale) / 2.0f;
        this.offsetY = (heightWithoutInsets - this.worldHeight * this.scale) / 2.0f;
    }

    @Override
    protected void scale(float scale) {
        this.graphics2D.scale(scale, scale);
    }

    @Override
    protected void translate(float offsetX, float offsetY) {
        this.graphics2D.translate(offsetX, offsetY);
    }

    @Override
    protected int getWindowWidth() {
        return this.window.getWidth();
    }

    @Override
    protected int getWindowHeight() {
        return this.window.getHeight();
    }

    @Override
    protected void prepareFrame() {
        this.graphics2D = (Graphics2D) this.bufferStrategy.getDrawGraphics();
        this.graphics2D.setTransform(this.affineTransform);

        this.clear(this.bgColor);

        this.calculateTransform();
        Insets insets = this.window.getInsets();
        this.translate(this.offsetX + insets.left, this.offsetY + insets.top);
        this.scale(this.scale);
    }

    @Override
    protected boolean endFrame() {
        this.graphics2D.dispose();
        this.graphics2D = null;
        if (bufferStrategy.contentsRestored()) {
            return true;
        }
        this.bufferStrategy.show();
        return this.bufferStrategy.contentsLost();
    }

    @Override
    public void render(IScene scene) {
        // Pintamos el frame
        do {
            // Se indica al gestor de renderizado que prepare el frame
            this.prepareFrame();
            // Se pinta la escena
            scene.render(this);
            // Se indica al gestor de renderizado que lo muestre
        } while (this.endFrame());
    }

    @Override
    public Vector screenToWorldPoint(Vector point) {
        Vector worldPoint = new Vector();

        int windowWidth = this.getWindowWidthWithoutInsets();
        int windowHeight = this.getWindowHeightWithoutInsets();

        Insets insets = this.window.getInsets();

        // Se divide el offset entre 2 porque hay que dejar el mismo espacio a ambos lados
        worldPoint.x = ((point.x - insets.left) - (windowWidth - this.worldWidth * this.scale) / 2.0f) / this.scale;
        worldPoint.y = ((point.y - insets.top) - (windowHeight - this.worldHeight * this.scale) / 2.0f) / this.scale;
        return worldPoint;
    }

    public int getWindowWidthWithoutInsets() {
        Insets insets = this.window.getInsets();
        return this.window.getWidth() - insets.left - insets.right;
    }

    public int getWindowHeightWithoutInsets() {
        Insets insets = this.window.getInsets();
        return this.window.getHeight() - insets.top - insets.bottom;
    }

    @Override
    public void clear(com.grupo04.engine.utilities.Color color) {
        this.setColor(color);
        this.graphics2D.fillRect(0, 0, this.getWindowWidth(), this.getWindowHeight());
        // Establece que si ya hay algo dibujado en graphics2D y se vuelve a dibujar en el,
        // se pinta por encima de lo que habia
        // Es el modo por defecto. Hay otros modos como setXORMode()...
        this.graphics2D.setPaintMode();
    }

    @Override
    public void setColor(com.grupo04.engine.utilities.Color color) {
        Color colorInt = new Color(color.red, color.green, color.blue, color.alpha);
        this.graphics2D.setColor(colorInt);
    }

    @Override
    public void drawCircle(Vector position, float radius, float strokeWidth) {
        this.graphics2D.setStroke(new BasicStroke(strokeWidth));
        this.graphics2D.drawOval((int) (position.x - radius), (int) (position.y - radius),
                (int) (radius * 2), (int) (radius * 2));
        this.graphics2D.setPaintMode();
    }

    @Override
    public void fillCircle(Vector position, float radius) {
        this.graphics2D.fillOval((int) (position.x - radius), (int) (position.y - radius),
                (int) (radius * 2), (int) (radius * 2));
        this.graphics2D.setPaintMode();
    }

    @Override
    public void drawRectangle(Vector position, float w, float h, float strokeWidth) {
        this.graphics2D.setStroke(new BasicStroke(strokeWidth));
        this.graphics2D.drawRect((int) (position.x - w / 2), (int) (position.y - h / 2),
                (int) w, (int) h);
        this.graphics2D.setPaintMode();
    }

    @Override
    public void fillRectangle(Vector position, float w, float h) {
        this.graphics2D.fillRect((int) (position.x - w / 2), (int) (position.y - h / 2),
                (int) w, (int) h);
        this.graphics2D.setPaintMode();
    }

    @Override
    public void drawRoundRectangle(Vector position, float w, float h, float arc, float strokeWidth) {
        this.graphics2D.setStroke(new BasicStroke(strokeWidth));
        this.graphics2D.drawRoundRect((int) (position.x - w / 2), (int) (position.y - h / 2),
                (int) w, (int) h, (int) arc, (int) arc);
        this.graphics2D.setPaintMode();
    }

    @Override
    public void fillRoundRectangle(Vector position, float w, float h, float arc) {
        this.graphics2D.fillRoundRect((int) (position.x - w / 2), (int) (position.y - h / 2),
                (int) w, (int) h, (int) arc, (int) arc);
        this.graphics2D.setPaintMode();
    }

    @Override
    public void drawLine(Vector initPos, Vector endPos, float strokeWidth) {
        this.graphics2D.setStroke(new BasicStroke(strokeWidth));
        this.graphics2D.drawLine((int) initPos.x, (int) initPos.y,
                (int) endPos.x, (int) endPos.y);
        this.graphics2D.setPaintMode();
    }

    @Override
    public void drawHexagon(Vector center, float radius, float rotInDegrees, float strokeWidth) {
        // Numero de lados del poligono
        int nSides = 6;
        Polygon hexagon = new Polygon();

        // Rotacion del hexagano en radianes y en sentido antihorario
        double rotInRadians = rotInDegrees * Math.PI / 180;

        for (int i = 0; i < nSides; i++) {
            // PI son 180 grados
            // Para dibujar un hexagano hay que dividir una circunferencia en 6 lados
            // Por lo tanto, 360 grados / 6 = 2 * PI / 6
            double pointRot = i * 2 * Math.PI / nSides;
            // Rotar el hexagono respecto a su posicion inicial
            pointRot += rotInRadians;

            int x = (int) (center.x + radius * Math.cos(pointRot));
            int y = (int) (center.y + radius * Math.sin(pointRot));

            hexagon.addPoint((int) x, (int) y);
        }

        this.graphics2D.setStroke(new BasicStroke(strokeWidth));
        this.graphics2D.drawPolygon(hexagon);
        this.graphics2D.setPaintMode();
    }

    @Override
    public IImage newImage(String name) {
        return new DesktopImage(name);
    }

    @Override
    public void drawImage(IImage img, Vector position) {
        DesktopImage desktopImage = (DesktopImage) img;
        this.graphics2D.drawImage(desktopImage.getImg(),
                (int) (position.x - desktopImage.getWidth() / 2f),
                (int) (position.y - desktopImage.getHeight() / 2f), null);
        this.graphics2D.setPaintMode();
    }

    @Override
    public void drawImage(IImage img, Vector position, int w, int h) {
        DesktopImage desktopImage = (DesktopImage) img;
        this.graphics2D.drawImage(desktopImage.getImg(),
                (int) (position.x - w / 2f),
                (int) (position.y - h / 2f), w, h, null);
        this.graphics2D.setPaintMode();
    }

    @Override
    public IFont newFont(String name, float size, boolean bold, boolean italian) {
        return new DesktopFont(name, size, bold, italian);
    }

    @Override
    public void setFont(IFont font) {
        DesktopFont desktopFont = (DesktopFont) font;
        this.graphics2D.setFont(desktopFont.getFont());
    }

    // Los textos no se dibujan como el resto de objetos desde una esquina del cuadro que los delimita,
    // sino que se pintan desde el principio de una linea (baseline), como cuando se escribe en papel
    // Obtener el ancho del texto es sencillo, pero obtener el alto del texto no ya que se solo se puede
    // obtener las metricas de la tipografia:
    // - Ascent -> altura desde la baseline hasta el caracter mas alto de la tipografia
    // - Descent -> altrua dessde la baseline hasta el caracter mas bajo de la tipografia
    // - Leading -> separacion entre el texto si hubiera varias lineas
    // - Height -> ascent + descent + leading
    @Override
    public void drawText(String text, Vector position, boolean centerX, boolean centerY) {
        FontMetrics fontMetrics = this.graphics2D.getFontMetrics();

        float h = fontMetrics.getHeight();
        float ascent = fontMetrics.getAscent();

        float x = position.x;
        float y = position.y;

        if (centerX) {
            x -= getTextWidth(text) / 2f;
        }
        if (centerY) {
            y -= h / 2;
            y += ascent;
        }
        this.graphics2D.drawString(text, x, y);
    }

    public int getTextWidth(String text) {
        FontMetrics fontMetrics = this.graphics2D.getFontMetrics();
        return fontMetrics.stringWidth(text);
    }

    public int getTextHeight(String text) {
        FontMetrics fontMetrics = this.graphics2D.getFontMetrics();
        return fontMetrics.getHeight();
    }
}
