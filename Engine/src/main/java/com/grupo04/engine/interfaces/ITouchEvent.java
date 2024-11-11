package com.grupo04.engine.interfaces;

import com.grupo04.engine.utilities.Vector;

public interface ITouchEvent  {
    enum TouchEventType { NONE, PRESS, RELEASE, DRAG, MOTION }
    TouchEventType getType();
    Vector getPos();
    void setPos(Vector newPos);
    void setPos(int posX, int posY);
}
