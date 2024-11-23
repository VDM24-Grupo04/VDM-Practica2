package com.grupo04.androidengine;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.grupo04.engine.Graphics;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.utilities.Vector;

public class AndroidGraphics extends Graphics {
    private final SurfaceView window;
    private final SurfaceHolder holder;
    private final Paint paint;
    private Canvas canvas;
    private final AssetManager assetManager;
    private final Rect rect;
    private final Rect src;
    private final Rect dest;
    private Path hexagon;

    public AndroidGraphics(SurfaceView window, AssetManager assetManager) {
        this.window = window;
        this.holder = window.getHolder();
        this.paint = new Paint();
        this.canvas = null;
        this.assetManager = assetManager;
        this.rect = new Rect();
        this.src = new Rect();
        this.dest = new Rect();
        this.hexagon = new Path();
    }

    @Override
    protected void calculateTransform() {
        int windowWidth = this.getWindowWidth();
        int windowHeight = this.getWindowHeight();

        float tempScaleX = (float) (windowWidth) / this.worldWidth;
        float tempScaleY = (float) (windowHeight) / this.worldHeight;
        this.scale = Math.min(tempScaleX, tempScaleY);

        this.offsetX = (windowWidth - this.worldWidth * this.scale) / 2.0f;
        this.offsetY = (windowHeight - this.worldHeight * this.scale) / 2.0f;
    }

    @Override
    protected void scale(float scale) {
        this.canvas.scale(scale, scale);
    }

    @Override
    protected void translate(float offsetX, float offsetY) {
        this.canvas.translate(offsetX, offsetY);
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
        // Pintamos el frame
        while (!this.holder.getSurface().isValid()) ;

        // Se permite editar el canvas
        this.canvas = this.holder.lockCanvas();

        this.clear(this.bgColor);

        this.calculateTransform();
        this.translate(this.offsetX, this.offsetY);
        this.scale(this.scale);
    }

    @Override
    protected boolean endFrame() {
        this.holder.unlockCanvasAndPost(canvas);
        return true;
    }

    @Override
    public void render(IScene scene) {
        // Se indica al gestor de renderizado que prepare el frame
        this.prepareFrame();
        // Se pinta la escena
        scene.render(this);
        // Se indica al gestor de renderizado que lo muestre
        this.endFrame();
    }

    @Override
    public Vector screenToWorldPoint(Vector point) {
        Vector worldPoint = new Vector();

        int windowWidth = this.getWindowWidth();
        int windowHeight = this.getWindowHeight();

        // Se divide el offset entre 2 porque hay que dejar el mismo espacio a ambos lados
        worldPoint.x = (point.x - (windowWidth - this.worldWidth * this.scale) / 2.0f) / this.scale;
        worldPoint.y = (point.y - (windowHeight - this.worldHeight * this.scale) / 2.0f) / this.scale;
        return worldPoint;
    }

    @Override
    public void clear(com.grupo04.engine.utilities.Color color) {
        int colorInt = Color.argb(color.alpha, color.red, color.green, color.blue);
        this.canvas.drawColor(colorInt);
    }

    @Override
    public void setColor(com.grupo04.engine.utilities.Color color) {
        int colorInt = Color.argb(color.alpha, color.red, color.green, color.blue);
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

        float initX = 0.0f;
        float initY = 0.0f;

        // Rotacion del hexagano en radianes y en sentido antihorario
        double rotInRadians = rotInDegrees * Math.PI / 180;

        for (int i = 0; i < nSides; i++) {
            // PI son 180 grados
            // Para dibujar un hexagano hay que dividir una circunferencia en 6 lados
            // Por lo tanto, 360 grados / 6 = 2 * PI / 6
            double pointRot = i * 2 * Math.PI / nSides;
            // Rotar el hexagono respecto a su posicion inicial
            pointRot += rotInRadians;

            float pointX = (float) (center.x + radius * Math.cos(pointRot));
            float pointY = (float) (center.y + radius * Math.sin(pointRot));

            if (i == 0) {
                initX = pointX;
                initY = pointY;
                this.hexagon.moveTo(pointX, pointY);
            } else {
                this.hexagon.lineTo(pointX, pointY);
            }
        }
        this.hexagon.lineTo(initX, initY);

        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(strokeWidth);
        this.canvas.drawPath(this.hexagon, this.paint);
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

        this.src.set(0, 0, androidImg.getWidth(), androidImg.getHeight());
        this.dest.set((int) (position.x - w / 2f), (int) (position.y - h / 2f),
                (int) (position.x + w - w / 2f),
                (int) (position.y + h - h / 2f));
        this.canvas.drawBitmap(androidImg.getImg(), this.src, this.dest, this.paint);
    }

    @Override
    public void setFont(IFont font) {
        AndroidFont androidFont = (AndroidFont) font;
        if (androidFont != null) {
            // Se establece el tamano de letra
            this.paint.setTextSize(androidFont.getSize());
            this.paint.setStyle(Paint.Style.FILL);
            // Se establece el tipo de letra
            this.paint.setTypeface(androidFont.getFont());
        }
    }

    @Override
    public IFont newFont(String name, float size, boolean bold, boolean italian) {
        return new AndroidFont(name, size, bold, italian, this.assetManager);
    }

    // Al contrario que en desktop, en android si se puede conseguir el alto del texto actual que se va a pintar.
    @Override
    public void drawText(String text, Vector position, boolean centerX, boolean centerY) {
        this.paint.getTextBounds(text, 0, text.length(), this.rect);
        if (centerX) {
            this.paint.setTextAlign(Paint.Align.CENTER);
        } else {
            this.paint.setTextAlign(Paint.Align.LEFT);
        }

        float y = position.y;
        if (centerY) {
            y -= this.rect.centerY();
        }
        this.canvas.drawText(text, position.x, y, this.paint);
    }

    @Override
    public int getTextWidth(String text) {
        this.paint.getTextBounds(text, 0, text.length(), this.rect);
        return this.rect.width();
    }

    @Override
    public int getTextHeight(String text) {
        Paint.FontMetrics fontMetrics = this.paint.getFontMetrics();
        return (int) (fontMetrics.bottom - fontMetrics.top);
    }
}
