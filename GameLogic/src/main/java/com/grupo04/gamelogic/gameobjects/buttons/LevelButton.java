package com.grupo04.gamelogic.gameobjects.buttons;

import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;

public class LevelButton extends TextButton {
    protected final int BORDER_THICKNESS = 3;       // Grosor del rectanguglo del borde

    public LevelButton(Vector pos,
                       float width, float height, float arc, Color baseCol, Color pointerOverCol,
                       String text, String fontName,
                       String onClickSoundPath) {
        super(pos, width, height, arc, baseCol, pointerOverCol, text, fontName, onClickSoundPath, () -> { });
    }

    @Override
    public void render(IGraphics graphics) {
        super.render(graphics);
        // Dibuja el rectangulo del borde
        graphics.setColor(super.fontColor);
        graphics.drawRoundRectangle(super.pos, super.width, super.height, super.arc, this.BORDER_THICKNESS);
    }

    public void setOnClick(Callback onClick) { this.onClick = onClick; }
}
