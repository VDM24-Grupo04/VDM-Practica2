package com.grupo04.gamelogic.gameobjects;

import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;

import java.util.List;

// Clase de la que heredaran todos los objetos de la tienda
public abstract class ShopItem extends Button {
    private int price;          // Precio
    private Vector pricePos;    // Posicion del texto del precio
    private IFont priceFont;    // IFont que usara el texto
    private String PRICE_FONT = "kimberley.ttf";    // Nombre del archivi de fuente que usara priceFont
    private final int PRICE_SIZE = 20;              // Tamano de la fuente
    private final int PRICE_OFFSET = 10;            // Separacion entre el texto y el cuadro con el objeto
    private Color PRICE_COLOR = new Color(0, 0, 0);     // Color del texto

    private IImage coinImage;       // Imagen de la moneda
    private Vector coinImagePos;    // Posicion de la imagen de la moneda
    private final float COIN_SIZE = PRICE_SIZE;     // Tamano de la imagen de la moneda

    private boolean bought;         // Si se ha comprado el objeto o no
    private boolean selected;       // Si el objeto esta seleccionado o no
    private Color SELECTED_COLOR = new Color(0, 255, 0);        // Color del borde seleccionado
    protected final int BORDER_RADIUS = 25;         // Radio del rectangulo del borde
    protected final int BORDER_THICKNESS = 3;       // Grosor del rectanguglo del borde

    private final double DOUBLE_TOUCH_THRESHOLD = 0.5;      // Tiempo en segundos maximo para detectar dobles pulsaciones
    private boolean hasTouched;     // Si se ha pulsado una vez (para detectar una segunda pulsacion)
    private double touchTimer;      // Tiempo desde la primera pulsacion

    public ShopItem(Vector pos, float width, float height, String onClickSoundPath, int price, IImage coinImage) {
        super(pos, width, height, onClickSoundPath, ()-> { });

        this.price = price;
        this.coinImage = coinImage;
        this.bought = false;
        this.selected = false;
        this.hasTouched = false;
        this.touchTimer = 0;

        // Se crean los vectores de posicion
        this.pricePos = new Vector(super.pos.x + PRICE_OFFSET, super.pos.y + super.height / 2 + PRICE_OFFSET * 2);
        this.coinImagePos = new Vector(this.pricePos.x - this.COIN_SIZE * 2, (float) (this.pricePos.y - this.COIN_SIZE * 0.1));
    }


    @Override
    public void init() {
        super.init();

        // Se crea la fuente
        this.priceFont = scene.getEngine().getGraphics().newFont(this.PRICE_FONT, this.PRICE_SIZE, true, false);
    }


    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        // Si se ha pulsado una vez y el objeto no ha sido comprado
        if (hasTouched && !bought) {
            // Actualiza el contador para detectar una doble pulsacion
            this.touchTimer += deltaTime;

            // Si no se ha detectado una doble pulsacion, pone que se ha pulsado a false
            if (this.touchTimer >= this.DOUBLE_TOUCH_THRESHOLD) {
                this.hasTouched = false;
            }
        }

    }

    @Override
    public void render(IGraphics graphics) {
        super.render(graphics);

        // Si no se ha comprado el objeto
        if (!bought) {
            // Se pinta el texto del precio
            graphics.setColor(this.PRICE_COLOR);
            graphics.setFont(this.priceFont);
            graphics.drawText(((Integer) this.price).toString(), this.pricePos);

            // Se pinta la imagen de la moneda al lado del texto
            graphics.drawImage(this.coinImage, this.coinImagePos, (int) COIN_SIZE, (int) COIN_SIZE);

            // Se pone el color del borde al por defecto (no esta seleccionado)
            graphics.setColor(this.PRICE_COLOR);
        }
        // Si no, si se se ha comprado y esta seleccionado, se pone el color del borde al seleccionado
        else if (selected) {
            graphics.setColor(this.SELECTED_COLOR);
        }
        // Si no, es que se ha comprado pero no se ha seleccionado y se
        // pone el color del borde al por defecto (no esta seleccionado)
        else {
            graphics.setColor(this.PRICE_COLOR);
        }

        // Dibuja el rectangulo del borde
        graphics.drawRoundRectangle(super.pos, super.width, super.height, this.BORDER_RADIUS, this.BORDER_THICKNESS);

    }

    @Override
    public void handleInput(List<ITouchEvent> touchEvents) {
        for (ITouchEvent touchEvent : touchEvents) {
            if (touchEvent.getType() == ITouchEvent.TouchEventType.PRESS) {
                // Si el evento es de pulsacion y esta dentro del area del boton
                if (withinArea(touchEvent.getPos())) {
                    // Si se ha comprado el objeto, lo selecciona
                    if (this.bought) {
                        if (!this.selected) {
                            super.onClick.call();
                        }
                        this.selected = true;
                    }
                    // Si no,
                    else {
                        // Si no se habia pulsado antes, pone que se ha pulsado
                        // una vez y reinicia el contador de doble pulsacion
                        if (!this.hasTouched) {
                            this.hasTouched = true;
                            this.touchTimer = 0;
                        }
                        // Si no, si se detecta doble pulsacion, intenta comprar el objeto
                        else if (this.touchTimer < this.DOUBLE_TOUCH_THRESHOLD) {
                            this.bought = this.tryBuying();
                        }
                    }

                    // Reproduce el sonido del boton
                    super.playOnClickSound();
                }
            }
        }
    }

    @Override
    public void dereference() {
        super.dereference();

        this.pricePos = null;
        this.priceFont = null;
        this.PRICE_COLOR = null;

//        this.coinImage = null;
        this.coinImagePos = null;
        this.SELECTED_COLOR = null;
    }


    private boolean tryBuying() {
        return true;
    }

    public void deselect() {
        this.selected = false;
    }
}
