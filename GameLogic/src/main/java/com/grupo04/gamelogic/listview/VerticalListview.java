package com.grupo04.gamelogic.listview;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.GameObject;

import java.util.ArrayList;
import java.util.List;

public class VerticalListview extends GameObject {
    private Vector topLeftOriginalPos;
    private Vector topLeftPos;
    private float previousDrag;
    private float width;
    private float height;
    private float totalHeight;
    private Color color;

    private float headerTopHeight;
    private Vector headerMediumPos;
    private float headerHeight;

    private float footerBottomHeight;
    private Vector footerMediumPos;
    private float footerHeight;

    private boolean resetButtonColors;
    private int itemIndex;
    private List<ListviewButton> buttons;
    private int itemsPerRow;
    private float itemOffset;
    private float itemSize;

    public VerticalListview(Vector pos, float width, float height, Color color,
                            float headerHeight, float footerHeight,
                            int levelsPerRow, float levelOffset) {
        this.topLeftOriginalPos = new Vector(pos.x - width / 2f, pos.y - height / 2f);
        this.topLeftPos = new Vector(this.topLeftOriginalPos);
        this.previousDrag = 0;
        this.width = width;
        this.height = height;
        this.totalHeight = levelOffset;
        this.color = color;

        this.headerTopHeight = this.topLeftOriginalPos.y - headerHeight;
        this.headerMediumPos = new Vector(this.topLeftOriginalPos.x + width / 2f, this.topLeftOriginalPos.y - headerHeight / 2f);
        this.headerHeight = headerHeight;

        this.footerBottomHeight = this.topLeftOriginalPos.y + height + footerHeight;
        this.footerMediumPos = new Vector(this.topLeftOriginalPos.x + width / 2f, this.topLeftOriginalPos.y + height + footerHeight / 2f);
        this.footerHeight = footerHeight;

        this.resetButtonColors = false;
        this.buttons = new ArrayList<>();
        this.itemsPerRow = levelsPerRow;
        this.itemOffset = levelOffset;
        this.itemSize = (this.width - 2 * levelOffset - (levelsPerRow - 1) * levelOffset) / levelsPerRow;
    }

    private boolean withinArea(Vector pos) {
        return pos.x > this.topLeftOriginalPos.x && pos.x < this.topLeftOriginalPos.x + this.width &&
                pos.y > this.topLeftOriginalPos.y && pos.y < this.topLeftOriginalPos.y + this.height;
    }

    private void move(float dragDiff) {
        this.topLeftPos.y += dragDiff;

        for (ListviewButton button : buttons) {
            button.move(this.topLeftPos.y);
        }
    }

    @Override
    public void init() {
        IEngine engine = scene.getEngine();

        for (ListviewButton button : buttons) {
            float x = this.itemIndex % this.itemsPerRow * (this.itemSize + this.itemOffset) + this.itemOffset + this.itemSize / 2f;
            float y = this.itemIndex / this.itemsPerRow * (this.itemSize + this.itemOffset) + this.itemOffset + this.itemSize / 2f;
            Vector relativePos = new Vector(x, y);
            Vector listviewPos = new Vector(this.topLeftPos);

            button.init(engine, relativePos, listviewPos, this.itemSize, this.itemSize);

            if (this.itemIndex % itemsPerRow == 0) {
                this.totalHeight += this.itemOffset;
                this.totalHeight += this.itemSize;
            }

            ++itemIndex;
        }
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
                        float dragDiff = currentDrag - previousDrag;
                        this.previousDrag = currentDrag;

                        // Se mueve hacia abajo
                        if (dragDiff > 0) {
                            float bottomHeight = this.topLeftPos.y + this.totalHeight;
                            if (bottomHeight < footerBottomHeight) {
                                this.move(dragDiff);
                            }
                        }
                        // Se mueve hacia arriba
                        else {
                            float topHeight = this.topLeftPos.y + dragDiff;
                            if (topHeight > headerTopHeight) {
                                this.move(dragDiff);
                            }
                        }
                        break;
                    case RELEASE:
                        this.previousDrag = 0f;
                        break;
                }

                this.resetButtonColors = true;
                for (ListviewButton button : buttons) {
                    button.handleEvent(touchEvent);
                }
            }
        }
    }

    @Override
    public void render(IGraphics graphics) {
        for (ListviewButton button : buttons) {
            button.render(graphics);
        }

        graphics.setColor(this.color);
        graphics.fillRectangle(this.headerMediumPos, this.width, this.headerHeight);
        graphics.fillRectangle(this.footerMediumPos, this.width, this.footerHeight);
    }

    public void addButton(ListviewButton button) {
        buttons.add(button);
    }
}
