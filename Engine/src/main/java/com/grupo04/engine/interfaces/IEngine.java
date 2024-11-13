package com.grupo04.engine.interfaces;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public interface IEngine {
    void setWorldSize(int worldWidth, int worldHeight);
    void setScene(IScene scene);
    IGraphics getGraphics();
    IAudio getAudio();
    FileInputStream getFileInputStream(String fileName);
    FileOutputStream getFileOutputStream(String fileName);
    void writeFile(FileOutputStream file, JSONObject jsonObject);
    JSONObject readFile(FileInputStream file);
}
