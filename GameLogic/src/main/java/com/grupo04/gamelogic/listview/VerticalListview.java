package com.grupo04.gamelogic.listview;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.GameManager;
import com.grupo04.gamelogic.GameObject;

import java.util.ArrayList;
import java.util.List;

public class VerticalListview extends GameObject {
    private final Vector topLeftOriginalPos;
    private final Vector topLeftPos;
    private float previousDrag;
    private final float width;
    private final float height;
    private float totalHeight;
    private final float originalBottomHeight;

    private Vector headerMediumPos;
    private Vector footerMediumPos;

    private final List<ListviewButton> buttons;
    private final int itemsPerRow;
    private final float itemOffsetX;
    private final float itemOffsetY;
    private final float itemSize;
    private int itemIndex;

    private GameManager gameManager;

    public VerticalListview(Vector pos, float width, float height,
                            int elementsPerRow, float itemOffsetX, float itemOffsetY) {
        this.topLeftOriginalPos = new Vector(pos.x - width / 2f, pos.y - height / 2f);
        this.topLeftPos = new Vector(this.topLeftOriginalPos);
        this.previousDrag = 0;
        this.width = width;
        this.height = height;
        this.totalHeight = itemOffsetX;
        this.originalBottomHeight = this.topLeftOriginalPos.y + this.height;

        this.headerMediumPos = null;
        this.footerMediumPos = null;

        this.buttons = new ArrayList<>();
        this.itemsPerRow = elementsPerRow;
        this.itemOffsetX = itemOffsetX;
        this.itemOffsetY = itemOffsetY;
        this.itemSize = (this.width - 2 * itemOffsetX - (elementsPerRow - 1) * itemOffsetX) / elementsPerRow;
        this.itemIndex = 0;

        this.gameManager = null;
    }

    private boolean withinArea(Vector pos) {
        return pos.x > this.topLeftOriginalPos.x && pos.x < this.topLeftOriginalPos.x + this.width &&
                pos.y > this.topLeftOriginalPos.y && pos.y < this.topLeftOriginalPos.y + this.height;
    }

    private void move(float dragDiff) {
        this.topLeftPos.y += dragDiff;

        for (ListviewButton button : this.buttons) {
            button.move(this.topLeftPos.y);
        }
    }

    @Override
    public void init() {
        this.gameManager = this.scene.getGameManager();

        IEngine engine = this.scene.getEngine();
        for (ListviewButton button : this.buttons) {
            float x = this.itemIndex % this.itemsPerRow * (this.itemSize + this.itemOffsetX) + this.itemOffsetX + this.itemSize / 2f;
            float y = this.itemIndex / this.itemsPerRow * (this.itemSize + this.itemOffsetY) + this.itemOffsetY + this.itemSize / 2f;
            Vector relativePos = new Vector(x, y);
            Vector listviewPos = new Vector(this.topLeftPos);

            button.init(engine, relativePos, listviewPos, this.itemSize, this.itemSize);

            if (this.itemIndex % this.itemsPerRow == 0) {
                this.totalHeight += this.itemOffsetY;
                this.totalHeight += this.itemSize;
            }

            ++this.itemIndex;
        }

        this.headerMediumPos = new Vector(this.topLeftOriginalPos.x + this.width / 2f, this.topLeftOriginalPos.y - this.totalHeight / 2f);

        this.footerMediumPos = new Vector(this.topLeftOriginalPos.x + this.width / 2f, this.topLeftOriginalPos.y + this.height + this.totalHeight / 2f);
    }

    @Override
    public void handleInput(List<ITouchEvent> touchEvents) {
        for (ITouchEvent touchEvent : touchEvents) {
            Vector touchEventPos = touchEvent.getPos();
            if (this.withinArea(touchEventPos)) {
                switch (touchEvent.getType()) {
                    case PRESS:
                        this.previousDrag = touchEventPos.y;
                        break;
                    case DRAG:
                        float currentDrag = touchEventPos.y;
                        float dragDiff = currentDrag - this.previousDrag;
                        this.previousDrag = currentDrag;

                        // Se mueve hacia abajo
                        if (dragDiff > 0) {
                            if (this.topLeftPos.y < this.topLeftOriginalPos.y) {
                                this.move(dragDiff);
                            }
                        }
                        // Se mueve hacia arriba
                        else {
                            float bottomHeight = this.topLeftPos.y + this.totalHeight;
                            if (bottomHeight > this.originalBottomHeight) {
                                this.move(dragDiff);
                            }
                        }
                        break;
                    case RELEASE:
                        this.previousDrag = 0f;
                        break;
                }

                for (ListviewButton button : this.buttons) {
                    button.handleEvent(touchEvent);
                }
            }
        }
    }

    @Override
    public void render(IGraphics graphics) {
        for (ListviewButton button : this.buttons) {
            button.render(graphics);
        }

        graphics.setColor(this.gameManager.getBgColor());
        graphics.fillRectangle(this.headerMediumPos, this.width, this.totalHeight);
        graphics.fillRectangle(this.footerMediumPos, this.width, this.totalHeight);
    }

    @Override
    public void update(double deltaTime) {
        for (ListviewButton button : this.buttons) {
            button.update(deltaTime);
        }
    }

    @Override
    public void dereference() {
        for (ListviewButton button : this.buttons) {
            button.dereference();
        }
    }

    public void addButton(ListviewButton button) {
        this.buttons.add(button);
    }

    public float getItemSize() {
        return this.itemSize;
    }
}
