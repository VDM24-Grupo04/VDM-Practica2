package com.grupo04.gamelogic.gameobjects;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.gamelogic.GameObject;
import com.grupo04.engine.utilities.Vector;
import com.grupo04.engine.interfaces.IAudio;
import com.grupo04.engine.interfaces.ISound;
import com.grupo04.engine.interfaces.ITouchEvent;

import java.util.List;

public abstract class Button extends GameObject {
    private IAudio audio;
    private final String onClickSoundPath;
    private ISound onClickSound;

    private Callback onClick;

    private final Vector topLeft;

    protected Vector pos;
    protected float width, height;

    private void playOnClickSound() {
        if (this.onClickSound != null) {
            this.audio.playSound(this.onClickSound);
        }
    }

    public ISound getOnClickSound() {
        return this.onClickSound;
    }

    protected void setOnClick(Callback callback) {
        this.onClick = callback;
    }

    protected boolean withinArea(Vector pos) {
        return pos.x > this.topLeft.x && pos.x < this.topLeft.x + width &&
                pos.y > this.topLeft.y && pos.y < this.topLeft.y + height;
    }

    protected Button(Vector pos, float width, float height, String onClickSoundPath, Callback onClick) {
        super();

        this.pos = pos;

        this.width = width;
        this.height = height;
        // Punto superior izquierdo
        this.topLeft = new Vector(pos.x - (float) width / 2, pos.y - (float) height / 2);

        this.audio = null;
        this.onClickSoundPath = onClickSoundPath;
        this.onClickSound = null;

        this.onClick = onClick;
    }

    @Override
    public void init() {
        IEngine engine = this.scene.getEngine();
        this.audio = engine.getAudio();
        if (this.onClickSoundPath != null) {
            this.onClickSound = this.audio.newSound(this.onClickSoundPath);
        }
    }

    @Override
    public void handleInput(List<ITouchEvent> touchEvents) {
        for (ITouchEvent touchEvent : touchEvents) {
            if (touchEvent.getType() == ITouchEvent.TouchEventType.PRESS) {
                if (withinArea(touchEvent.getPos())) {
                    playOnClickSound();
                    this.onClick.call();
                }
            }
        }
    }

    @Override
    public void dereference() {
        super.dereference();

        this.audio = null;
        this.onClick = null;
        this.onClickSound = null;
    }
}
