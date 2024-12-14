package com.grupo04.androidengine;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.view.SurfaceView;

import com.google.android.gms.ads.AdView;
import com.grupo04.engine.Engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AndroidEngine extends Engine {
    private final Activity mainActivity;

    public AndroidEngine(Activity mainActivity, SurfaceView window, AssetManager assetManager, AdView adView, int maxStreams) {
        super();

        this.mainActivity = mainActivity;

        AndroidGraphics androidGraphics = new AndroidGraphics(window, assetManager);
        AndroidAudio androidAudio = new AndroidAudio(assetManager, maxStreams);
        AndroidInput androidInput = new AndroidInput(window);
        AndroidMobile androidMobile = new AndroidMobile(this.mainActivity, window, adView);

        System.loadLibrary("HashLibrary");

        this.initModules(androidGraphics, androidAudio, androidInput, androidMobile);
    }

    @Override
    public InputStream getFileInputStream(String fileName, FileType type) {
        if (type == FileType.PROGRESS_DATA) {
            File file = new File(this.mainActivity.getFilesDir(), fileName);
            if (!file.exists()) {
                System.out.println("File not found: " + fileName);
                return null;
            }

            // Si el archivo existe
            try {
                return this.mainActivity.openFileInput(fileName);
            } catch (IOException e) {
                System.err.println("Error while getting FileInputStream from " + fileName + ": " + e.getMessage());
                return null;
            }
        } else {
            try {
                return this.mainActivity.getAssets().open(fileName);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    public FileOutputStream getFileOutputStream(String fileName) {
        try {
            return this.mainActivity.openFileOutput(fileName, Context.MODE_PRIVATE);
        } catch (IOException e) {
            System.err.println("Error while getting FileOutputStream from: " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public void eraseFile(String fileName) {
        File file = new File(this.mainActivity.getFilesDir(), fileName);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File was successfully deleted");
            } else {
                System.err.println("Failed to delete the file");
            }
        } else {
            System.err.println("File does not exist");
        }
    }

    @Override
    public String[] listDirectories(String filePath, FileType type) {
        AssetManager assetManager = this.mainActivity.getAssets();
        try {
            // Lista los archivos existentes a partir de la ruta dada
            // y para cada archivo comprueba si es un directorio.
            // En caso de error, devuelve []
            return Arrays.stream(Objects.requireNonNull(assetManager.list(filePath)))
                    .filter(dir -> {
                        try {
                            return Objects.requireNonNull(assetManager.list(filePath + "/" + dir)).length > 0;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .toArray(String[]::new);
        } catch (IOException e) {
            System.err.println("Error while listing directories in " + filePath + ": " + e.getMessage());
            return new String[0];
        }
    }

    @Override
    public String[] listFiles(String filePath, FileType type) {
        AssetManager assetManager = this.mainActivity.getAssets();
        try {
            // Si no termina en "/", hay que añadirselo
            if (!filePath.endsWith("/")) {
                filePath += "/";
            }

            String[] entries = assetManager.list(filePath);
            if (entries == null) {
                return new String[0];
            }

            List<String> files = new ArrayList<>();
            for (String entry : entries) {
                try {
                    InputStream is = assetManager.open(filePath + entry);
                    is.close();
                    files.add(entry);
                } catch (IOException e) {
                    // Si no se puede abrir, se asume que es un directorio
                }
            }
            return files.toArray(new String[0]);
        } catch (IOException e) {
            System.err.println("Error while listing files in " + filePath + ": " + e.getMessage());
            return new String[0];
        }
    }

    @Override
    public void onResume() {
        mobile.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mobile.onPause();
        super.onPause();
    }

    private native String hash(String data);

    @Override
    public String getHash(String data) {
        return hash(data);
    }
}
