package com.grupo04.gamelogic.listview;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.utilities.Vector;

public abstract class ListviewItem {
    protected Vector relativePos;
    protected Vector pos;
    protected float width;
    protected float height;

    public ListviewItem() {
        this.relativePos = null;
        this.pos = null;
        this.height = 0f;
        this.width = 0f;
    }

    public void move(float listviewPosY) {
        this.pos.y = listviewPosY + this.relativePos.y;
    }

    public void init(IEngine engine, Vector relativePos, Vector listviewPos, float width, float height) {
        this.relativePos = relativePos;
        this.pos = listviewPos.plus(this.relativePos);
        this.width = width;
        this.height = height;
    }

    public abstract void render(IGraphics graphics);

    public void dereference() { };
}
