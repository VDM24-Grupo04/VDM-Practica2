package com.grupo04.gamelogic.gameobjects;

import com.grupo04.androidengine.utilities.Vector;

public abstract class ToggleButton extends Button {
    protected boolean checked;

    protected void toggle() {
        this.checked = !this.checked;
    }

    protected ToggleButton(Vector pos, float width, float height, String onClickSoundPath) {
        super(pos, width, height, onClickSoundPath, null);

        this.setOnClick(this::toggle);
        this.checked = false;
    }

    public boolean isCheck() {
        return this.checked;
    }
}
