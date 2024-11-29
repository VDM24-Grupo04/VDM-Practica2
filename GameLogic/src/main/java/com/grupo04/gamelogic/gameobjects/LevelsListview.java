package com.grupo04.gamelogic.gameobjects;

import com.grupo04.engine.interfaces.IAudio;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.gamelogic.GameObject;
import com.grupo04.gamelogic.gameobjects.buttons.LevelButton;

import java.util.ArrayList;
import java.util.List;

public class LevelsListview extends GameObject {
    private final Color BUTTON_BASE_COLOR = new Color(237, 12, 46);
    private final Color BUTTON_OVER_COLOR = new Color(203, 10, 38);
    private final float BUTTON_ARC = 80f;
    private final float BUTTON_BORDER_WIDTH = 4.0f;
    private final Color BUTTON_BORDER_COLOR = new Color(0, 0, 0);
    private final Color BUTTON_FONT_COLOR = new Color(0, 0, 0);
    private final String BUTTON_FONT_NAME = "kimberley.ttf";
    private final String BUTTON_SOUND = "button.wav";

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
    private List<LevelButton> levelButtons;
    private int levelsPerRow;
    private float levelOffset;
    private float levelSize;

    public LevelsListview(Vector pos, float width, float height, Color color,
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
        this.levelButtons = new ArrayList<>();
        this.levelsPerRow = levelsPerRow;
        this.levelOffset = levelOffset;
        this.levelSize = (this.width - 2 * levelOffset - (levelsPerRow - 1) * levelOffset) / levelsPerRow;
    }

    private boolean withinArea(Vector pos) {
        return pos.x > this.topLeftOriginalPos.x && pos.x < this.topLeftOriginalPos.x + this.width &&
                pos.y > this.topLeftOriginalPos.y && pos.y < this.topLeftOriginalPos.y + this.height;
    }

    private void move(float dragDiff) {
        this.topLeftPos.y += dragDiff;

        for (LevelButton levelButton : levelButtons) {
            levelButton.move(this.topLeftPos.y);
        }
    }

    @Override
    public void init() {
        IGraphics graphics = scene.getEngine().getGraphics();
        IAudio audio = scene.getEngine().getAudio();

        for (LevelButton levelButton : levelButtons) {
            levelButton.init(graphics, audio);
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
                for (LevelButton levelButton : levelButtons) {
                    levelButton.handleEvent(touchEvent);
                }
            } else {
                if (this.resetButtonColors) {
                    this.resetButtonColors = false;
                    for (LevelButton levelButton : levelButtons) {
                        levelButton.resetColor();
                    }
                }
            }
        }
    }

    @Override
    public void render(IGraphics graphics) {
        for (LevelButton levelButton : levelButtons) {
            levelButton.render(graphics);
        }

        graphics.setColor(this.color);
        graphics.fillRectangle(this.headerMediumPos, this.width, this.headerHeight);
        graphics.fillRectangle(this.footerMediumPos, this.width, this.footerHeight);
    }

    public void addLevelButton(int levelNumber, Callback callback) {
        float x = this.itemIndex % this.levelsPerRow * (this.levelSize + this.levelOffset) + this.levelOffset + this.levelSize / 2f;
        float y = this.itemIndex / this.levelsPerRow * (this.levelSize + this.levelOffset) + this.levelOffset + this.levelSize / 2f;
        Vector relativePos = new Vector(x, y);
        Vector listviewPos = new Vector(this.topLeftPos);

        LevelButton levelButton = new LevelButton(levelNumber, listviewPos, relativePos, this.levelSize, this.levelSize, BUTTON_ARC, BUTTON_BORDER_WIDTH,
                BUTTON_BASE_COLOR, BUTTON_OVER_COLOR, BUTTON_BORDER_COLOR,
                BUTTON_FONT_NAME, false, BUTTON_FONT_COLOR,
                BUTTON_SOUND, callback);
        levelButtons.add(levelButton);

        if (this.itemIndex % levelsPerRow == 0) {
            this.totalHeight += this.levelOffset;
            this.totalHeight += levelButton.getHeight();
        }

        ++itemIndex;
    }
}
