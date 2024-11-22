package com.grupo04.engine;

import com.grupo04.engine.interfaces.IAudio;
import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

// La interfaz runnable se trata de una interfaz que cuenta con un solo metodo a implementar (run)
// Cuando se pasa una instancia de esta clase a un nuevo hilo, el hilo ejecuta el metodo run
public abstract class Engine implements IEngine, Runnable {
    private static final int MAX_NUM_FIXED_UDPATES = 150;
    private static final double FIXED_DELTA_TIME = 1000.0 / 60.0;

    // Se necesita un hilo para correr el renderizado a la par que la ejecucion de android
    private Thread mainLoopThread;
    private volatile boolean isRunning;

    // Modulos
    private Graphics graphics;
    private Audio audio;
    private Input input;

    private IScene scene;

    protected Engine() {
        this.mainLoopThread = null;
        this.isRunning = false;
        this.graphics = null;
        this.audio = null;
        this.input = null;
        this.scene = null;
    }

    protected void initModules(Graphics graphics, Audio audio, Input input) {
        this.graphics = graphics;
        this.audio = audio;
        this.input = input;
    }

    private void handleInput() {
        List<ITouchEvent> sceneTouchEvents = this.input.getTouchEvents();
        if (this.scene != null && !sceneTouchEvents.isEmpty()) {
            for (ITouchEvent event : sceneTouchEvents) {
                Vector worldPoint = this.graphics.screenToWorldPoint(event.getPos());
                event.setPos(worldPoint);
            }
            this.scene.handleInput(sceneTouchEvents);
        }
    }

    private void fixedUpdate() {
        if (this.scene != null) {
            this.scene.fixedUpdate(FIXED_DELTA_TIME);
        }
    }

    private void update(double deltaTime) {
        if (this.scene != null) {
            this.scene.update(deltaTime);
        }
    }

    private void render() {
        if (this.scene != null) {
            // Si se quedan escenas sin renderizar y se ejecuta el renderizado de un frame,
            // se produce un flickering
            this.graphics.render(this.scene);
        }
    }

    private void refresh() {
        if (this.scene != null) {
            this.scene.refresh();
        }
    }

    @Override
    public void run() {
        // Evita que se pueda llamar al run desde cualquier sitio.
        // (No se puede hacer que run sea protected o private por Runnable)
        if (this.mainLoopThread != Thread.currentThread()) {
            throw new RuntimeException("run() should not be called directly");
        }

        // Si el Thread se pone en marcha muy rápido, la vista podría todavía no estar inicializada,
        // por lo que se inicia un bucle que no hace nada pero que bloquea la ejecucion del codigo
        // posterior hasta que se haya inicializado la vista
        while (this.isRunning && !this.graphics.isWindowInitialized()) ;

        double deltaTime = 0.0;
        double lag = 0.0;

        long lastFrameTime = System.nanoTime();
        // Informe de FPS
        long previousReport = lastFrameTime;
        int frames = 0;

        while (this.isRunning) {
            // Calcular deltatime
            long currentTime = System.nanoTime();
            long nanoElapsedTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;
            deltaTime = (double) nanoElapsedTime / 1.0E9;

            handleInput();

            lag += deltaTime;

            int numFixedUpdates = 0;
            // Se realiza el fixedUpdate cada cierto tiempo determinado
            // Al acumularse el tiempo sobrante (algo) puede haber varios fixedUpdates en el mismo frame
            while (lag >= FIXED_DELTA_TIME) {
                fixedUpdate();
                lag -= FIXED_DELTA_TIME;

                // Evitar problema de Spiral of Death
                ++numFixedUpdates;
                if (numFixedUpdates > MAX_NUM_FIXED_UDPATES) {
                    lag = 0;
                    break;
                }
            }

            update(deltaTime);
            // Informar sobre los FPS a los que corre el juego
            if (currentTime - previousReport > 1000000000L) {
                long fps = frames * 1000000000L / (currentTime - previousReport);
                //System.out.println(fps + " fps");
                frames = 0;
                previousReport = currentTime;
            }
            ++frames;

            refresh();

            render();
        }
    }

    public void onResume() {
        if (!this.isRunning) {
            this.isRunning = true;
            // Se crea un nuevo hilo y se inicia
            this.mainLoopThread = new Thread(this);
            // El hilo tiene que estar sincronizado con el de android studio
            this.mainLoopThread.start();
        }
    }

    public void onPause() {
        if (this.isRunning) {
            // Se pone isRunning a false y se intenta esperar a que termine el hilo
            this.isRunning = false;
            while (true) {
                try {
                    // El hilo tiene que estar sincronizado con el de android studio
                    this.mainLoopThread.join();
                    this.mainLoopThread = null;
                    break;
                } catch (InterruptedException e) {
                    System.err.println("Error in the render thread: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void setWorldSize(int worldWidth, int worldHeight) {
        this.graphics.setWorldSize(worldWidth, worldHeight);
    }

    @Override
    public void setScene(IScene scene) {
        this.scene = scene;
        if (this.scene != null) {
            this.scene.init();
        }
    }

    @Override
    public IGraphics getGraphics() {
        return this.graphics;
    }

    @Override
    public IAudio getAudio() {
        return this.audio;
    }

    @Override
    public void writeFile(FileOutputStream file, JSONObject info) {
        if (file != null) {
            try {
                file.write(info.toString().getBytes());
            } catch (IOException e) {
                System.err.println("Error while writing in file: " + e.getMessage());
            }
        }
    }

    @Override
    public JSONObject readFile(InputStream file) {
        if (file != null) {
            JSONObject jsonObject = null;
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            try {
                InputStreamReader inputStreamReader = new InputStreamReader(file, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (IOException e) {
                System.err.println("Error while reading file: " + e.getMessage());
            } catch (JSONException e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
            }
            return jsonObject;
        }
        return null;
    }

    public void shutdown() {
        if (this.scene != null) {
            this.scene.shutdown();
        }
    }
}
