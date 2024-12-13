package com.grupo04.gamelogic.gameobjects;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.utilities.Color;
import com.grupo04.gamelogic.GameObject;
import com.grupo04.engine.utilities.Vector;

public class Text extends GameObject {
    private Vector pos;
    private final String[] texts;
    private String fontName;
    private float size;
    private boolean bold;
    private boolean italic;
    private Color color;
    private IFont font;
    // Indentancion de las lineas de texto
    // Hay que tener en cuenta que inicialmente
    // todo el texto esta alineado a la izquierda
    // respecto a la primera linea de texto
    private final float[] indentation;
    // Espacio entre las diferentes lineas
    private final float lineSpacing;

    // Ancho del primer texto
    private float firstTextWidth;
    // Ancho de todos los textos
    private float[] textsWidths;
    // Alto del primer texto
    private float[] textsHeights;
    // Alto de todos los textos
    private float firstTextHeight;
    // Alto total de todas las lineas
    private float fullHeight;
    // Calcular la posicion de cada linea de texto
    private Vector textPos;

    private boolean hasCalculateParams;
    private IGraphics graphics;

    private void calculateParams() {
        this.fullHeight = 0f;

        // Se establece la fuente para poder calcular los tamanos de los textos
        this.graphics.setFont(this.font);
        // Guardar en arrays ancho y alto de los textos para no tener que calcularlos todo el rato
        for (int i = 0; i < this.texts.length; ++i) {
            String text = this.texts[i];
            this.textsWidths[i] = this.graphics.getTextWidth(text);
            this.textsHeights[i] = this.graphics.getTextHeight(text);

            this.fullHeight += this.textsHeights[i];
            if (i > 0) {
                this.fullHeight += this.lineSpacing;
            } else {
                this.firstTextWidth = this.textsWidths[i];
                this.firstTextHeight = this.textsHeights[i];
            }
        }
    }

    private void initCommonParams(Vector pos, String fontName, float size, boolean bold, boolean italic,
                                  Color color) {
        this.pos = pos;
        this.fontName = fontName;
        this.size = size;
        this.bold = bold;
        this.italic = italic;
        this.color = color;

        this.firstTextWidth = 0f;
        this.textsWidths = new float[this.texts.length];
        this.textsHeights = new float[this.texts.length];
        this.fullHeight = 0f;
        this.textPos = new Vector();

        this.hasCalculateParams = true;
        this.graphics = null;
    }

    // Texto de multiples lineas con indentacion (respecto a la posicion original del primer texto)
    public Text(Vector pos, String[] texts, String fontName, float size, boolean bold, boolean italic,
                Color color, float[] indentation, float lineSpacing) {
        super();

        this.texts = texts;
        this.indentation = indentation;
        this.lineSpacing = lineSpacing;

        initCommonParams(pos, fontName, size, bold, italic, color);
    }

    // Texto de multiples lineas sin indentacion
    public Text(Vector pos, String[] texts, String fontName, float size, boolean bold, boolean italic,
                Color color, float lineSpacing) {
        super();

        this.texts = texts;
        this.indentation = new float[0];
        this.lineSpacing = lineSpacing;

        initCommonParams(pos, fontName, size, bold, italic, color);
    }

    // Texto de una sola linea
    public Text(Vector pos, String text, String fontName, float size, boolean bold, boolean italic,
                Color color) {
        super();

        this.texts = new String[]{text};
        this.indentation = new float[0];
        this.lineSpacing = 0;

        initCommonParams(pos, fontName, size, bold, italic, color);
    }

    @Override
    public void init() {
        IEngine engine = this.scene.getEngine();
        this.graphics = engine.getGraphics();
        this.font = this.graphics.newFont(this.fontName, this.size, this.bold, this.italic);
    }

    @Override
    public void render(IGraphics graphics) {
        if (this.hasCalculateParams) {
            calculateParams();
            this.hasCalculateParams = false;
        }

        graphics.setColor(this.color);
        graphics.setFont(this.font);

        // Centrar el texto horziontalmente
        this.textPos.x = 0f;
        this.textPos.y = this.pos.y + this.firstTextHeight / 2f - this.fullHeight / 2f;
        for (int i = 0; i < this.texts.length; ++i) {
            String currentText = this.texts[i];
            this.textPos.x = this.pos.x;
            // Para textos que que no sean el de la primera linea...
            if (i > 0) {
                // Se alinea a la izquiera con el texto de la primera linea...
                float widthDiff = this.textsWidths[i] - this.firstTextWidth;
                this.textPos.x += widthDiff / 2f;

                // Se coloca en otra linea con un interlineado...
                float textHeight = this.textsHeights[i - 1];
                this.textPos.y += textHeight + this.lineSpacing;
            }
            // Se indenta si fuera necesario
            if (i < this.indentation.length) {
                this.textPos.x += this.indentation[i];
            }

            graphics.drawText(currentText, this.textPos);
        }
    }

    public void setTextLine(String text, int line) {
        this.texts[line] = text;
        this.hasCalculateParams = true;
    }

    public void setTextLine(String text) {
        setTextLine(text, 0);
    }

    @Override
    public void dereference() {
        super.dereference();

        this.font = null;
    }
}
