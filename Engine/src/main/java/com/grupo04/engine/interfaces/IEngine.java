package com.grupo04.engine.interfaces;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public interface IEngine {
    void setWorldSize(int worldWidth, int worldHeight);
    void setScene(IScene scene);
    IGraphics getGraphics();
    IAudio getAudio();
    IMobile getMobile();

    InputStream getFileInputStream(String fileName, FileType type);
    FileOutputStream getFileOutputStream(String fileName);
    void writeFile(FileOutputStream file, JSONObject jsonObject);
    JSONObject readFile(InputStream file);

    enum FileType { GAME_DATA, PROGRESS_DATA }
}
