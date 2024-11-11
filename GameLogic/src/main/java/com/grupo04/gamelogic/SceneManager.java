package com.grupo04.gamelogic;

import com.grupo04.engine.interfaces.IEngine;
import com.grupo04.engine.interfaces.IGraphics;
import com.grupo04.engine.interfaces.IScene;
import com.grupo04.engine.interfaces.ITouchEvent;

import java.util.List;
import java.util.Stack;

public class SceneManager implements IScene {
    // Escenas
    private final IEngine engine;
    private final Stack<Scene> scenes;
    private final Stack<Scene> aliveScenes;

    public SceneManager(IEngine engine) {
        this.engine = engine;
        this.scenes = new Stack<>();
        this.aliveScenes = new Stack<>();
    }

    public void popScene() {
        if (!this.scenes.empty()) {
            this.scenes.peek().setAlive(false);
        }
    }

    public void pushScene(Scene newScene) {
        this.scenes.push(newScene);
        newScene.setSceneManager(this);
        newScene.init();
    }

    public void changeScene(Scene newScene) {
        if (!this.scenes.empty()) {
            // Si la escena que se quiere insertar no es la misma que la activa...
            if (this.scenes.peek() != newScene) {
                this.scenes.peek().setAlive(false);
                // Se inserta la nueva escena
                this.scenes.push(newScene);
                newScene.setSceneManager(this);
                newScene.init();
            }
        }
    }

    @Override
    public void handleInput(List<ITouchEvent> touchEvents) {
        if (!this.scenes.empty()) {
            scenes.peek().handleInput(touchEvents);
        }
    }

    @Override
    public void update(double deltaTime) {
        if (!this.scenes.empty()) {
            scenes.peek().update(deltaTime);
        }
    }

    @Override
    public void fixedUpdate(double fixedDeltaTime) {
        if (!this.scenes.empty()) {
            scenes.peek().fixedUpdate(fixedDeltaTime);
        }
    }

    @Override
    public void render(IGraphics graphics) {
        if (!this.scenes.empty()) {
            scenes.peek().render(graphics);
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
}
