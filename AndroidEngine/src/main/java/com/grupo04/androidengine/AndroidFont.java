package com.grupo04.androidengine;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import com.grupo04.engine.interfaces.IFont;

public class AndroidFont implements IFont {
    private Typeface typeface;
    private final float size;

    public AndroidFont(String name, float size, boolean bold, boolean italic, AssetManager assetManager) {
        this.size = size;
        try {
            this.typeface = Typeface.createFromAsset(assetManager, "fonts/" + name);
            if (bold && italic) {
                this.typeface = Typeface.create(this.typeface, Typeface.BOLD_ITALIC);
            } else if (bold) {
                this.typeface = Typeface.create(this.typeface, Typeface.BOLD);
            } else if (italic) {
                this.typeface = Typeface.create(this.typeface, Typeface.ITALIC);
            }
        } catch (RuntimeException e) {
            System.err.println("Error in the font with name " + name + ": " + e.getMessage());
        }
    }

    public Typeface getFont() {
        return this.typeface;
    }

    public float getSize() {
        return size;
    }
}
