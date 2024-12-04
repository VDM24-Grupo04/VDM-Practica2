package com.grupo04.gamelogic.listview;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Vector;

public abstract class ListviewButton extends ListviewItem {
    protected Vector topLeft;

    public ListviewButton() {
        super();
        this.topLeft = null;
    }

    protected boolean withinArea(Vector pos) {
        return pos.x > this.topLeft.x && pos.x < this.topLeft.x + width &&
                pos.y > this.topLeft.y && pos.y < this.topLeft.y + height;
    }

    @Override
    public void init(IEngine engine, Vector relativePos, Vector listviewPos, float width, float height) {
        super.init(engine, relativePos, listviewPos, width, height);
        this.topLeft = new Vector(this.pos.x - width / 2f, this.pos.y - height / 2f);
    }

    @Override
    public void move(float listviewPosY) {
        super.move(listviewPosY);
        this.topLeft.y = this.pos.y - this.height / 2f;
    }

    public abstract void handleEvent(ITouchEvent touchEvent);

    public void update(double deltaTime) { };
}
