package com.grupo04.gamelogic.listview;

import com.grupo04.engine.interfaces.IAudio;
import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IFont;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.GameManager;

public class ShopItemButton extends ListviewButton {
    private final int price;    // Precio
    private Vector pricePos;    // Posicion del texto del precio
    private IFont priceFont;    // IFont que usara el texto
    private final Color priceColor;
    private final int PRICE_OFFSET = 10; // Separacion entre el texto y el cuadro con el objeto

    private final IImage coinImage; // Imagen de la moneda
    private Vector coinImagePos;    // Posicion de la imagen de la moneda
    private final float coinSize;   // Tamano de la imagen de la moneda

    private IAudio audio;
    private final ISound onClickSound;
    protected Callback onSelect, baseOnSelect, onDeselect;

    private boolean bought;         // Si se ha comprado el objeto o no
    private boolean selected;       // Si el objeto esta seleccionado o no
    private final Color selectedColor;
    protected int BORDER_RADIUS = 25;           // Radio del rectangulo del borde
    protected final int BORDER_THICKNESS = 3;   // Grosor del rectanguglo del borde

    private final double DOUBLE_TOUCH_THRESHOLD = 0.5; // Tiempo en segundos maximo para detectar dobles pulsaciones
    private boolean hasTouched;     // Si se ha pulsado una vez (para detectar una segunda pulsacion)
    private double touchTimer;      // Tiempo desde la primera pulsacion

    GameManager gameManager;

    public ShopItemButton(int price, IFont priceFont, Color priceColor, IImage coinImage,
                          int coinSize, Color selectedColor, ISound buttonClickSound, GameManager gameManager) {
        super();

        this.price = price;
        this.priceFont = priceFont;
        this.priceColor = priceColor;

        this.coinImage = coinImage;
        this.coinSize = coinSize;
        this.selectedColor = selectedColor;

        this.onClickSound = buttonClickSound;

        this.onSelect = () -> {
        };
        this.onDeselect = () -> {
        };

        this.bought = false;
        this.selected = false;
        this.hasTouched = false;
        this.touchTimer = 0;

        // Se crean los vectores de posicion
        this.pricePos = new Vector(0, 0);
        this.coinImagePos = new Vector(0, 0);

        this.gameManager = gameManager;
    }

    @Override
    public void init(IEngine engine, Vector relativePos, Vector listviewPos, float width, float height) {
        super.init(engine, relativePos, listviewPos, width, height);

        this.audio = engine.getAudio();

        // Cambia la posicion y la esquina superior izquierda del padre
        this.pos.x = this.topLeft.x + this.width / 2;
        this.pos.y = this.topLeft.y + this.height / 2;
        updatePricePos();
    }

    @Override
    public void handleEvent(ITouchEvent touchEvent) {
        if (touchEvent.getType() == ITouchEvent.TouchEventType.PRESS) {
            // Si el evento es de pulsacion y esta dentro del area del boton
            if (withinArea(touchEvent.getPos())) {
                // Si se ha comprado el objeto, hace toggle de su estado de seleccion
                if (this.bought) {
                    setSelected(!this.selected);

                    // Si se ha seleccionado, llama a la funcion de seleccion
                    if (this.selected) {
                        this.onSelect.call();
                    }
                    // Si no, llama a la funcion de deseleccion
                    else {
                        this.onDeselect.call();
                    }
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
                        // Si el numero de monedas tras comprar el objeto es >= que 0,
                        // compra el objeto, cambia el numero de monedas
                        if (this.gameManager.getCoins() - this.price >= 0) {
                            this.bought = true;
                            this.gameManager.decreaseCoins(this.price);
                        }
                    }
                }

                // Reproduce el sonido del boton
                this.audio.playSound(this.onClickSound);
            }
        }
    }

    @Override
    public void render(IGraphics graphics) {
        // Si no se ha comprado el objeto
        if (!this.bought) {
            // Se pinta el texto del precio
            graphics.setColor(this.priceColor);
            graphics.setFont(this.priceFont);
            graphics.drawText(((Integer) this.price).toString(), this.pricePos, false, true);

            // Se pinta la imagen de la moneda al lado del texto
            graphics.drawImage(this.coinImage, this.coinImagePos, (int) this.coinSize, (int) this.coinSize);

            // Se pone el color del borde al por defecto (no esta seleccionado)
            graphics.setColor(this.priceColor);
        }
        // Si no, si se se ha comprado y esta seleccionado, se pone el color del borde al seleccionado
        else if (this.selected) {
            graphics.setColor(this.selectedColor);
        }
        // Si no, es que se ha comprado pero no se ha seleccionado y se
        // pone el color del borde al por defecto (no esta seleccionado)
        else {
            graphics.setColor(this.priceColor);
        }

        // Dibuja el rectangulo del borde
        graphics.drawRoundRectangle(super.pos, super.width, super.height, this.BORDER_RADIUS, this.BORDER_THICKNESS);
    }

    @Override
    public void update(double deltaTime) {
        updatePricePos();

        // Si se ha pulsado una vez y el objeto no ha sido comprado
        if (this.hasTouched && !this.bought) {
            // Actualiza el contador para detectar una doble pulsacion
            this.touchTimer += deltaTime;

            // Si no se ha detectado una doble pulsacion, pone que se ha pulsado a false
            if (this.touchTimer >= DOUBLE_TOUCH_THRESHOLD) {
                this.hasTouched = false;
            }
        }
    }

    @Override
    public void dereference() {
        this.pricePos = null;
        this.priceFont = null;

        this.coinImagePos = null;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public boolean getBought() {
        return this.bought;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void select() {
        setSelected(true);
        this.onSelect.call();
    }
    public void deselect() {
        setSelected(false);
        this.onDeselect.call();
    }

    private void updatePricePos() {
        this.pricePos.x = this.pos.x;
        this.pricePos.y = this.pos.y + this.height / 2 + PRICE_OFFSET * 2;

        this.coinImagePos.x = this.pricePos.x - this.coinSize;
        this.coinImagePos.y = this.pricePos.y;
    }

    // Hace que la funcion onSelect sea una llamada a la funcion indicada
    // y una llamada a la funcion onSelect base (en vez de sobreescribirla)
    public void setOnSelectExtra(Callback extraFunc) {
        this.onSelect = () -> {
            extraFunc.call();
            baseOnSelect.call();
        };
    }
}
