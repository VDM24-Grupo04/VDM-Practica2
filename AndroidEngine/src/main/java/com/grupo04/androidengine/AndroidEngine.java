package com.grupo04.androidengine;

import android.content.res.AssetManager;
import android.view.SurfaceView;

import com.grupo04.androidengine.audio.AndroidAudio;
import com.grupo04.androidengine.audio.IAudio;
import com.grupo04.androidengine.ec.Scene;
import com.grupo04.androidengine.graphics.AndroidGraphics;
import com.grupo04.androidengine.input.AndroidInput;
import com.grupo04.androidengine.graphics.IGraphics;
import com.grupo04.androidengine.input.ITouchEvent;
import com.grupo04.androidengine.utilities.Vector;

import java.util.List;
import java.util.Stack;

public class AndroidEngine implements IEngine, Runnable {
    private static final int MAX_NUM_FIXED_UDPATES = 150;
    private static final double FIXED_DELTA_TIME = 1000.0 / 60.0;

    // Se necesita un hilo para correr el renderizado a la par que la ejecucion de android
    private Thread mainLoopThread;
    private volatile boolean isRunning;

    // Modulos
    private AndroidGraphics graphics;
    private AndroidAudio audio;
    private AndroidInput input;

    // Escenas
    private Stack<Scene> scenes;
    private Stack<Scene> aliveScenes;

    public AndroidEngine(SurfaceView window, AssetManager assetManager, int maxStreams) {
        this.mainLoopThread = null;
        this.isRunning = false;
        this.graphics = null;
        this.audio = null;
        this.input = null;
        this.scenes = new Stack<>();
        this.aliveScenes = new Stack<>();

        this.graphics = new AndroidGraphics(window, assetManager);
        this.audio = new AndroidAudio(assetManager, maxStreams);
        this.input = new AndroidInput(window);
    }

    @Override
    public void popScene() {
        if (!this.scenes.empty()) {
            this.scenes.peek().setAlive(false);
        }
    }

    @Override
    public void pushScene(Scene newScene) {
        this.scenes.push(newScene);
        newScene.init();
    }

    @Override
    public void changeScene(Scene newScene) {
        if (!this.scenes.empty()) {
            // Si la escena que se quiere insertar no es la misma que la activa...
            if (this.scenes.peek() != newScene) {
                this.scenes.peek().setAlive(false);
                // Se inserta la nueva escena
                this.scenes.push(newScene);
                newScene.init();
            }
        }
    }

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

    private void handleInput() {
        List<ITouchEvent> sceneTouchEvents = this.input.getTouchEvents();
        if (!this.scenes.empty() && !sceneTouchEvents.isEmpty()) {
            for (ITouchEvent event : sceneTouchEvents) {
                Vector worldPoint = this.graphics.screenToWorldPoint(event.getPos());
                event.setPos(worldPoint);
            }
            this.scenes.peek().handleInput(sceneTouchEvents);
        }
    }

    private void fixedUpdate() {
        if (!this.scenes.empty()) {
            this.scenes.peek().fixedUpdate(FIXED_DELTA_TIME);
        }
    }

    private void update(double deltaTime) {
        if (!this.scenes.empty()) {
            this.scenes.peek().update(deltaTime);
        }
    }

    private void refresh() {
        if (!this.scenes.empty()) {
            this.scenes.peek().refresh();

            boolean hasDeadScenes = false;

            while (!this.scenes.empty()) {
                Scene scene = this.scenes.peek();
                if (!scene.isAlive()) {
                    scene.dereference();
                    hasDeadScenes = true;
                } else {
                    this.aliveScenes.push(scene);
                }
                this.scenes.pop();
            }

            while (!aliveScenes.empty()) {
                Scene scene = aliveScenes.peek();
                this.scenes.push(scene);
                this.aliveScenes.pop();
            }

            // Si se ha eliminado una escena, quiere decir que se vuelve a la anterior y, por lo tanto,
            // hay que actualizar el tam del mundo
            if (!this.scenes.empty() && hasDeadScenes) {
                this.aliveScenes.clear();
                Scene currentScene = this.scenes.peek();
                this.setWorldSize(currentScene.getWorldWidth(), currentScene.getWorldHeight());
            }
        }
    }

    private void render() {
        if (!this.scenes.empty()) {
            // Si se quedan escenas sin renderizar y se ejecuta el renderizado de un frame,
            // se produce un flickering
            this.graphics.render(this.scenes.peek());
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

    public void setWorldSize(int worldWidth, int worldHeight) {
        this.graphics.setWorldSize(worldWidth, worldHeight);
    }

    @Override
    public IGraphics getGraphics() {
        return this.graphics;
    }

    @Override
    public IAudio getAudio() {
        return this.audio;
    }
}
