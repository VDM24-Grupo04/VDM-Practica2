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
    void writeFile(FileOutputStream file, String info);
    String readFile(InputStream file);
    String getHash(String data);
    void shareAction(ShareActionType type, ShareParams params);

    enum FileType { GAME_DATA, PROGRESS_DATA }
    enum ShareActionType { IMAGE, TEXT }
    class ShareParams {
        public String extraText;
        public int x, y, w, h;
        public boolean fullScreen = false;
    }
}
