package com.grupo04.androidengine.input;

import com.grupo04.androidengine.utilities.Vector;

public class TouchEvent implements ITouchEvent {
    private final TouchEventType type;  // Tipo del evento producido
    private Vector pos;                 // Posicion (sin escalar) en la que se ha producido el evento

    public TouchEvent(TouchEventType type, Vector pos) {
        this.type = type;
        this.pos = pos;
    }

    public TouchEventType getType() { return type; }
    public Vector getPos() { return pos; }
    public void setPos(Vector newPos) { this.pos = newPos; }
}
