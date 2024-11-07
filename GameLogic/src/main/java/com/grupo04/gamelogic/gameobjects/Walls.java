package com.grupo04.gamelogic.gameobjects;

import com.grupo04.androidengine.graphics.IGraphics;
import com.grupo04.androidengine.utilities.Color;
import com.grupo04.androidengine.ec.GameObject;
import com.grupo04.androidengine.utilities.Vector;

public class Walls extends GameObject {
    final Color color = new Color(60, 60, 60);
    Vector[] pos;
    Vector[] sizes;

    public Walls(int thickness, int headerOffset, int width, int height) {
        super();

        this.pos = new Vector[]{
                new Vector(thickness / 2.0f, headerOffset + (height - headerOffset) / 2.0f),
                new Vector(width / 2.0f, headerOffset + thickness / 2.0f),
                new Vector(width - (thickness / 2.0f), headerOffset + (height - headerOffset) / 2.0f)
        };
        this.sizes = new Vector[]{
                new Vector(thickness, height - headerOffset),
                new Vector(width, thickness),
        };
    }

    @Override
    public void render(IGraphics graphics) {
        super.render(graphics);
        graphics.setColor(this.color);
        graphics.fillRectangle(this.pos[0], this.sizes[0].x, this.sizes[0].y);
        graphics.fillRectangle(this.pos[1], this.sizes[1].x, this.sizes[1].y);
        graphics.fillRectangle(this.pos[2], this.sizes[0].x, this.sizes[0].y);
    }
}
