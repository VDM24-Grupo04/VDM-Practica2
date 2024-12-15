package com.grupo04.gamelogic;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.interfaces.ITouchEvent;
import com.grupo04.engine.utilities.Color;

import java.util.List;
import java.util.Stack;

public abstract class SceneManager implements IScene {
    // Escenas
    private final Stack<Scene> aliveScenes;

    protected final Stack<Scene> scenes;
    protected final IEngine engine;

    public SceneManager(IEngine engine) {
        this.engine = engine;
        this.scenes = new Stack<>();
        this.aliveScenes = new Stack<>();
    }

    protected void initNewScene(Scene newScene) {
        newScene.init();
    }

    @Override
    public void init() {
    }

    public void popScene() {
        if (!this.scenes.empty()) {
            this.scenes.peek().setAlive(false);
        }
    }

    public void pushScene(Scene newScene) {
        this.scenes.push(newScene);
        this.initNewScene(newScene);
    }

    public void changeScene(Scene newScene) {
        if (!this.scenes.empty()) {
            // Si la escena que se quiere insertar no es la misma que la activa...
            if (this.scenes.peek() != newScene) {
                this.scenes.peek().setAlive(false);
                // Se inserta la nueva escena
                this.scenes.push(newScene);
                this.initNewScene(newScene);
            }
        }
    }

    @Override
    public void handleInput(List<ITouchEvent> touchEvents) {
        if (!this.scenes.empty()) {
            this.scenes.peek().handleInput(touchEvents);
        }
    }

    @Override
    public void update(double deltaTime) {
        if (!this.scenes.empty()) {
            this.scenes.peek().update(deltaTime);
        }
    }

    @Override
    public void fixedUpdate(double fixedDeltaTime) {
        if (!this.scenes.empty()) {
            this.scenes.peek().fixedUpdate(fixedDeltaTime);
        }
    }

    @Override
    public void render(IGraphics graphics) {
        if (!this.scenes.empty()) {
            this.scenes.peek().render(graphics);
        }
    }

    @Override
    public void refresh() {
        if (!this.scenes.empty()) {
            this.scenes.peek().refresh();

            boolean hasDeadScenes = false;

            while (!this.scenes.empty()) {
                Scene scene = this.scenes.peek();
                if (!scene.isAlive()) {
                    // Cada vez que se vaya a quitar una escena de la pila
                    // se llama a su shutdown para asignar los valores a medias
                    // en caso de ser GameScene o ShopScene
                    // scene.shutdown();
                    scene.dereference();
                    hasDeadScenes = true;
                } else {
                    this.aliveScenes.push(scene);
                }
                this.scenes.pop();
            }

            while (!this.aliveScenes.empty()) {
                Scene scene = this.aliveScenes.peek();
                this.scenes.push(scene);
                this.aliveScenes.pop();
            }

            // Si se ha eliminado una escena, quiere decir que se vuelve a la anterior y, por lo tanto,
            // hay que actualizar el tam del mundo
            if (!this.scenes.empty() && hasDeadScenes) {
                this.aliveScenes.clear();
                Scene currentScene = this.scenes.peek();
                this.engine.setWorldSize(currentScene.getWorldWidth(), currentScene.getWorldHeight());
            }
        }
    }

    @Override
    public void shutdown() {
    }
}
