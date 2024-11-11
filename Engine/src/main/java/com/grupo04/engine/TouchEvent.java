package com.grupo04.engine;

import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Vector;

public class TouchEvent implements ITouchEvent {
    private TouchEventType type;  // Tipo del evento producido
    private Vector pos;           // Posicion (sin escalar) en la que se ha producido el evento

    public TouchEvent(TouchEventType type, int posX, int posY) {
        this.type = type;
        this.pos = new Vector(posX, posY);
    }

    public TouchEventType getType() { return type; }
    public void setType(TouchEventType type) { this.type = type; }
    public Vector getPos() { return pos; }
    public void setPos(Vector newPos) { this.pos = newPos; }
    public void setPos(int posX, int posY) {
        this.pos.x = posX;
        this.pos.y = posY;
    }
}
