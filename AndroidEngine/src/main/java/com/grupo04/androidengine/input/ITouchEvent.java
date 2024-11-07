package com.grupo04.androidengine.input;

import com.grupo04.androidengine.utilities.Vector;

public interface ITouchEvent  {
    enum TouchEventType { PRESS, RELEASE, DRAG, MOTION }
    TouchEventType getType();
    Vector getPos();
    void setPos(Vector newPos);
}
