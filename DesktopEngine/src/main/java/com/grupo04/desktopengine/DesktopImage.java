package com.grupo04.desktopengine;

import com.grupo04.engine.interfaces.IImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DesktopImage implements IImage {
    private BufferedImage img;

    DesktopImage(String fileName) {
        try {
            this.img = ImageIO.read(new File("./assets/images/" + fileName));
        }
        catch (IOException e) {
            System.err.println("Error in the image with name " + fileName + ": " + e.getMessage());
        }
    }

    public BufferedImage getImg() {
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
