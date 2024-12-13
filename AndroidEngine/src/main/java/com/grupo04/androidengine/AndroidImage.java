package com.grupo04.androidengine;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.grupo04.engine.interfaces.IImage;

import java.io.IOException;
import java.io.InputStream;

public class AndroidImage implements IImage {
    private Bitmap img;

    AndroidImage(String fileName, AssetManager assetManager) {
        try {
            InputStream is = assetManager.open("images/" + fileName);
            this.img = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            System.err.println("Error in the font with name " + fileName + ": " + e.getMessage());
        }
    }

    public Bitmap getImg() {
        return this.img;
    }

    @Override
    public int getWidth() {
        return this.img.getWidth();
    }

    @Override
    public int getHeight() {
        return this.img.getHeight();
    }

    @Override
    public boolean isValid() { return this.img != null; }
}
