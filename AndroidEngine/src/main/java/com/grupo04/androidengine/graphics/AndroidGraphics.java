package com.grupo04.androidengine.graphics;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.grupo04.androidengine.ec.Scene;
import com.grupo04.androidengine.utilities.Color;
import com.grupo04.androidengine.utilities.Vector;

public class AndroidGraphics implements IGraphics {
    private final SurfaceView window;
    private final SurfaceHolder holder;
    private final Paint paint;
    private Canvas canvas;
    private final AssetManager assetManager;

    protected int worldWidth;
    protected int worldHeight;
    protected float scale;
    protected float offsetX;
    protected float offsetY;
    protected Color bgColor;

    public AndroidGraphics(SurfaceView window, AssetManager assetManager) {
        this.worldWidth = 0;
        this.worldHeight = 0;
        this.scale = 1.0f;
        this.offsetX = 0.0f;
        this.offsetY = 0.0f;
        this.bgColor = new Color(255, 255, 255, 255);

        this.window = window;
        this.holder = window.getHolder();
        this.paint = new Paint();
        this.canvas = null;
        this.assetManager = assetManager;
    }

    protected void calculateTransform() {
        int windowWidth = this.getWindowWidth();
        int windowHeight = this.getWindowHeight();

        float tempScaleX = (float) (windowWidth) / this.worldWidth;
        float tempScaleY = (float) (windowHeight) / this.worldHeight;
        this.scale = Math.min(tempScaleX, tempScaleY);

        this.offsetX = (windowWidth - this.worldWidth * this.scale) / 2.0f;
        this.offsetY = (windowHeight - this.worldHeight * this.scale) / 2.0f;
    }

    protected void scale(float scale) {
        this.canvas.scale(scale, scale);
    }

    protected void translate(float offsetX, float offsetY) {
        this.canvas.translate(offsetX, offsetY);
    }

    protected int getWindowWidth() {
        return this.window.getWidth();
    }

    protected int getWindowHeight() {
        return this.window.getHeight();
    }

    protected void prepareFrame() {
        // Pintamos el frame
        while (!this.holder.getSurface().isValid()) ;

        // Se permite editar el canvas
        this.canvas = this.holder.lockCanvas();

        this.clear(this.bgColor);

        this.calculateTransform();
        this.translate(this.offsetX, this.offsetY);
        this.scale(this.scale);
    }

    protected boolean endFrame() {
        this.holder.unlockCanvasAndPost(canvas);
        return true;
    }

    public void render(Scene currentScene) {
        // Se indica al gestor de renderizado que prepare el frame
        this.prepareFrame();
        // Se pinta la escena
        currentScene.render(this);
        // Se indica al gestor de renderizado que lo muestre
        this.endFrame();
    }

    public boolean isWindowInitialized() {
        return this.getWindowWidth() != 0;
    }

    public void setWorldSize(int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void setClearColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public Vector screenToWorldPoint(Vector point) {
        Vector worldPoint = new Vector();

        int windowWidth = this.getWindowWidth();
        int windowHeight = this.getWindowHeight();

        // Se divide el offset entre 2 porque hay que dejar el mismo espacio a ambos lados
        worldPoint.x = (point.x - (windowWidth - this.worldWidth * this.scale) / 2.0f) / this.scale;
        worldPoint.y = (point.y - (windowHeight - this.worldHeight * this.scale) / 2.0f) / this.scale;
        return worldPoint;
    }

    public void clear(Color color) {
        int colorInt = android.graphics.Color.argb(color.alpha, color.red, color.green, color.blue);
        this.canvas.drawColor(colorInt);
    }

    public void setColor(Color color) {
        int colorInt = android.graphics.Color.argb(color.alpha, color.red, color.green, color.blue);
        this.paint.setColor(colorInt);
    }

    @Override
    public void drawCircle(Vector position, float radius, float strokeWidth) {
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(strokeWidth);
        this.canvas.drawCircle(position.x, position.y, radius, this.paint);
    }

    @Override
    public void fillCircle(Vector position, float radius) {
        this.paint.setStyle(Paint.Style.FILL);
        this.canvas.drawCircle(position.x, position.y, radius, this.paint);
    }

    @Override
    public void drawRectangle(Vector position, float w, float h, float strokeWidth) {
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(strokeWidth);
        this.canvas.drawRect(position.x - w / 2, position.y - h / 2,
                position.x + w - w / 2, position.y + h - h / 2, this.paint);
    }

    @Override
    public void fillRectangle(Vector position, float w, float h) {
        this.paint.setStyle(Paint.Style.FILL);
        this.canvas.drawRect(position.x - w / 2, position.y - h / 2,
                position.x + w - w / 2, position.y + h - h / 2, this.paint);
    }

    @Override
    public void drawRoundRectangle(Vector position, float w, float h, float arc, float strokeWidth) {
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(strokeWidth);
        this.canvas.drawRoundRect(position.x - w / 2, position.y - h / 2,
                position.x + w - w / 2, position.y + h - h / 2,
                arc, arc, this.paint);
    }

    @Override
    public void fillRoundRectangle(Vector position, float w, float h, float arc) {
        this.paint.setStyle(Paint.Style.FILL);
        this.canvas.drawRoundRect(position.x - w / 2, position.y - h / 2,
                position.x + w - w / 2, position.y + h - h / 2,
                arc, arc, this.paint);
    }

    @Override
    public void drawLine(Vector initPos, Vector endPos, float strokeWidth) {
        // No importa el estilo
        this.paint.setStrokeWidth(strokeWidth);
        this.canvas.drawLine(initPos.x, initPos.y, endPos.x, endPos.y, this.paint);
    }

    @Override
    public void drawHexagon(Vector center, float radius, float rotInDegrees, float strokeWidth) {
        // Numero de lados del poligono
        int nSides = 6;
        Path hexagon = new Path();
        Vector initPoint = new Vector();

        // Rotacion del hexagano en radianes y en sentido antihorario
        double rotInRadians = rotInDegrees * Math.PI / 180;

        for (int i = 0; i < nSides; i++) {
            // PI son 180 grados
            // Para dibujar un hexagano hay que dividir una circunferencia en 6 lados
            // Por lo tanto, 360 grados / 6 = 2 * PI / 6
            double pointRot = i * 2 * Math.PI / nSides;
            // Rotar el hexagono respecto a su posicion inicial
            pointRot += rotInRadians;

            Vector point = new Vector();
            point.x = (float) (center.x + radius * Math.cos(pointRot));
            point.y = (float) (center.y + radius * Math.sin(pointRot));

            if (i == 0) {
                initPoint.x = point.x;
                initPoint.y = point.y;
                hexagon.moveTo(point.x, point.y);
            } else {
                hexagon.lineTo(point.x, point.y);
            }
        }
        hexagon.lineTo(initPoint.x, initPoint.y);

        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(strokeWidth);
        this.canvas.drawPath(hexagon, this.paint);
    }

    @Override
    public void drawHexagon(Vector center, float radius, float strokeWidth) {
        drawHexagon(center, radius, 0, strokeWidth);
    }

    @Override
    public IImage newImage(String name) {
        return new AndroidImage(name, this.assetManager);
    }

    @Override
    public void drawImage(IImage img, Vector position) {
        AndroidImage androidImg = (AndroidImage) img;
        float w = (float) androidImg.getWidth();
        float h = (float) androidImg.getHeight();
        this.canvas.drawBitmap(androidImg.getImg(),
                position.x - w / 2,
                position.y - h / 2, this.paint);
    }

    @Override
    public void drawImage(IImage img, Vector position, int w, int h) {
        AndroidImage androidImg = (AndroidImage) img;

        Rect src = new Rect(0, 0, androidImg.getWidth(), androidImg.getHeight());
        Rect dest = new Rect((int) (position.x - w / 2f), (int) (position.y - h / 2f),
                (int) (position.x + w - w / 2f),
                (int) (position.y + h - h / 2f));
        this.canvas.drawBitmap(androidImg.getImg(), src, dest, this.paint);
    }

    @Override
    public void setFont(IFont font) {
        AndroidFont androidFont = (AndroidFont) font;
        // Se establece el tamano de letra
        this.paint.setTextSize(font.getSize());
        this.paint.setStyle(Paint.Style.FILL);
        // Se establece el tipo de letra
        this.paint.setTypeface(androidFont.getFont());
    }

    @Override
    public IFont newFont(String name, float size, boolean bold, boolean italian) {
        return new AndroidFont(name, size, bold, italian, this.assetManager);
    }

    // Al contrario que en desktop, en android si se puede conseguir el alto del texto actual que se va a pintar.
    @Override
    public void drawText(String text, Vector position) {
        Rect rect = new Rect();
        this.paint.getTextBounds(text, 0, text.length(), rect);
        this.paint.setTextAlign(Paint.Align.CENTER);
        float y = position.y - rect.centerY();
        this.canvas.drawText(text, position.x, y, this.paint);
    }

    @Override
    public int getTextWidth(String text) {
        Rect rect = new Rect();
        this.paint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }

    @Override
    public int getTextHeight(String text) {
        Paint.FontMetrics fontMetrics = this.paint.getFontMetrics();
        return (int) (fontMetrics.bottom - fontMetrics.top);
    }
}
