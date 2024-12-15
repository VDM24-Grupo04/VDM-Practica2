package com.grupo04.gamelogic;

import com.grupo04.engine.Sensor;
import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.utilities.Callback;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.interfaces.IImage;
import com.grupo04.engine.utilities.Color;
import com.grupo04.engine.utilities.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class Scene implements IScene {
    public enum Fade {NONE, IN, OUT}

    private boolean alive;
    private final List<GameObject> gameObjects;
    private final HashMap<String, GameObject> handlers;

    protected IImage bgImage;
    private final Vector bgImagePos;
    protected Color UIColor;

    private Fade fade;
    private double fadeDuration;
    private double fadeTimer;
    private Color fadeColor;
    private final Vector fadePos;
    private Callback onFadeEnd;

    protected IEngine engine;
    protected int worldWidth;
    protected int worldHeight;

    protected GameManager gameManager;

    protected Scene(IEngine engine, int worldWidth, int worldHeight, Color UIColor) {
        this.alive = true;
        this.gameObjects = new ArrayList<>();
        this.handlers = new HashMap<>();

        this.bgImage = null;
        this.UIColor = UIColor;

        this.fade = Fade.NONE;
        this.fadeDuration = 0;
        this.fadeTimer = 0;
        this.fadeColor = new Color(0, 0, 0);
        this.onFadeEnd = null;

        this.engine = engine;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.engine.setWorldSize(this.worldWidth, this.worldHeight);

        this.fadePos = new Vector(this.worldWidth / 2.0f, this.worldHeight / 2.0f);
        this.bgImagePos = new Vector(this.worldWidth / 2.0f, this.worldHeight / 2.0f);

        this.gameManager = null;
    }

    protected Scene(IEngine engine, int worldWidth, int worldHeight) {
        this(engine, worldWidth, worldHeight, null);
    }

    // Color del fondo de la ventana
    protected Scene(IEngine engine, int worldWidth, int worldHeight, Color bgColor, Color UIColor) {
        this(engine, worldWidth, worldHeight);
        this.engine.getGraphics().setClearColor(bgColor);
        this.UIColor = UIColor;
    }

    // Fondo del juego (ruta de una imagen)
    protected Scene(IEngine engine, int worldWidth, int worldHeight, String bgImageFileName, Color UIColor) {
        this(engine, worldWidth, worldHeight);
        this.bgImage = this.engine.getGraphics().newImage(bgImageFileName);
        this.UIColor = UIColor;
    }

    // Color del fondo de la ventana y fondo del juego (ruta de una imagen)
    protected Scene(IEngine engine, int worldWidth, int worldHeight, Color bgColor, String bgImageFileName, Color UIColor) {
        this(engine, worldWidth, worldHeight);
        this.engine.getGraphics().setClearColor(bgColor);
        this.bgImage = this.engine.getGraphics().newImage(bgImageFileName);
        this.UIColor = UIColor;
    }

    public void setUIColor(Color color) { this.UIColor = color; }
    public Color getUIColor() { return this.UIColor; }

    // Hacer que la escena comience con un fade. Se le puede pasar solo el tipo de fade,
    // solo el tipo y la duracion, o el tipo, la duracion y el color del fade.
    // ** fadeDuration tiene que ir en segundos
    public void setFade(Fade fade) {
        this.fade = fade;
        this.fadeDuration = 0.2;
        this.fadeTimer = 0;
    }

    public void setFade(Fade fade, double fadeDuration) {
        this.fade = fade;
        this.fadeDuration = fadeDuration;
        this.fadeTimer = 0;
    }

    public void setFade(Fade fade, double fadeDuration, Color fadeColor) {
        setFade(fade, fadeDuration);
        this.fadeColor = fadeColor;

        if (this.fade == Fade.IN) {
            fadeColor.alpha = 0;
        } else if (this.fade == Fade.OUT) {
            fadeColor.alpha = 0;
        }
    }

    public void setFade(Fade fade, Color fadeColor) {
        setFade(fade);
        this.fadeColor = fadeColor;
    }

    public void setFadeCallback(Callback onFadeEnd) {
        this.onFadeEnd = onFadeEnd;
    }

    public void addGameObject(GameObject gameObject) {
        this.gameObjects.add(gameObject);
        gameObject.setScene(this);
    }

    public void addGameObject(GameObject gameObject, String handler) {
        this.gameObjects.add(gameObject);
        if (!this.handlers.containsKey(handler)) {
            this.handlers.put(handler, gameObject);
            gameObject.setId(handler);
        }
        gameObject.setScene(this);
    }

    public GameObject getHandler(String handler) {
        return this.handlers.get(handler);
    }

    @Override
    public void handleInput(List<ITouchEvent> touchEvents) {
        if (this.fadeTimer >= this.fadeDuration) {
            if (!this.gameObjects.isEmpty()) {
                for (GameObject gameObject : this.gameObjects) {
                    gameObject.handleInput(touchEvents);
                }
            }
        }
    }

    @Override
    public void update(double deltaTime) {
        // Si ya se ha terminado de reproducir el fade, permite que se actualicen los objetos
        if (this.fadeTimer >= this.fadeDuration) {
            if (!this.gameObjects.isEmpty()) {
                for (GameObject gameObject : this.gameObjects) {
                    gameObject.update(deltaTime);
                }
            }
        }
        // Si no, actualiza el contador del fade
        else {
            this.fadeTimer += deltaTime;

            // Si el fade justo se ha acabado y hay un callback al acabar, se llama
            if (this.fadeTimer >= this.fadeDuration && this.onFadeEnd != null) {
                this.onFadeEnd.call();
            }
        }
    }

    public void refresh() {
        if (!this.gameObjects.isEmpty()) {
            HashSet<GameObject> deadGameObjects = new HashSet<>();
            for (GameObject gameObject : this.gameObjects) {
                if (!gameObject.isAlive()) {
                    deadGameObjects.add(gameObject);
                }
            }
            for (GameObject deletedGameObject : deadGameObjects) {
                this.gameObjects.remove(deletedGameObject);
                // Elimina el objeto que corresponde con la clave y además,
                // devuelve true o false si existe o no, respectivamente
                this.handlers.remove(deletedGameObject.getId());
                deletedGameObject.dereference();
            }
            deadGameObjects.clear();
        }
    }

    @Override
    public void fixedUpdate(double fixedDeltaTime) {
        if (this.fadeTimer >= this.fadeDuration) {
            if (!this.gameObjects.isEmpty()) {
                for (GameObject gameObject : this.gameObjects) {
                    gameObject.fixedUpdate(fixedDeltaTime);
                }
            }
        }
    }

    @Override
    public void render(IGraphics graphics) {
        if (this.bgImage != null && this.bgImagePos != null) {
            graphics.drawImage(this.bgImage, this.bgImagePos, this.worldWidth, this.worldHeight);
        }

        if (!this.gameObjects.isEmpty()) {
            for (GameObject gameObject : this.gameObjects) {
                gameObject.render(graphics);
            }
        }

        // Si no se ha terminado de reproducir el fade
        if (this.fadeTimer < this.fadeDuration) {
            // Se calcula el alpha del fondo en base al tiempo que dura la animacion y al tiempo que lleva reproducido
            int alpha = (int) ((255.0f * this.fadeTimer) / this.fadeDuration);

            // Si es un fade out, se hace a la inversa
            if (this.fade == Fade.OUT) {
                alpha = 255 - alpha;
            }

            // Si el alpha es mayor que 0, se cambia el color del fondo
            // y se pinta un rectangulo que ocupa toda la pantalla
            if (alpha > 0) {
                this.fadeColor.alpha = alpha;
                graphics.setColor(this.fadeColor);
                graphics.fillRectangle(this.fadePos, this.worldWidth, this.worldHeight);
            }
        }
    }

    // El recolector de basura elimina un objeto cuando no hay mas punteros hacia ese objeto.
    // Una escena tiene gameobjects y cada gameobject tiene un puntero a la escena. De modo que
    // cuando se saque una escena de la pila, esta nunca se va a eliminar porque hay un "ciclo"
    // de referencias.
    // Este metodo funciona como una especie de delete y pone todas las referencias del objeto a null.
    // Si la escena o el gameobject hijos tienen mas referencias a otros objetos, debe hacerse override
    // de este metodo y poner esas referencias tambien a null
    public void dereference() {
        if (!this.gameObjects.isEmpty()) {
            for (GameObject gameObject : this.gameObjects) {
                gameObject.dereference();
            }
        }
        this.engine = null;
        this.gameObjects.clear();
        this.handlers.clear();
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return this.alive;
    }

    // Coger las diferentes referencias
    @Override
    public void init() {
        if (!this.gameObjects.isEmpty()) {
            for (GameObject gameObject : this.gameObjects) {
                gameObject.init();
            }
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void sensorChanged(Sensor sensor) {
    }

    public void saveJson() {
    }

    public int getWorldWidth() {
        return this.worldWidth;
    }
    public int getWorldHeight() {
        return this.worldHeight;
    }

    public IEngine getEngine() {
        return this.engine;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    public GameManager getGameManager() {
        return this.gameManager;
    }
}
