package com.grupo04.androidengine.ec;

import com.grupo04.androidengine.graphics.IGraphics;
import com.grupo04.androidengine.input.ITouchEvent;

import java.util.List;

public abstract class GameObject {
    private String id;
    private boolean alive;
    protected IScene scene;

    protected GameObject() {
        this.id = null;
        this.alive = true;
        this.scene = null;
    }

    public void handleInput(List<ITouchEvent> touchEvents) {
    }

    public void render(IGraphics graphics) {
    }

    public void update(double deltaTime) {
    }

    public void fixedUpdate(double fixedDeltaTime) {
    }

    // Ver dereference de la clase Scene
    // Si un objeto1 tiene una referencia a un objeto2, dicha referencia debe ser debil
    // porque en el caso de eliminar de la escena el objeto2, este no podria borrarse de memoria
    // porque objeto1 tendria una referencia a el
    // Referencia debil -> no cuenta como puntero a la hora de comprobar si se puede eliminar un objeto de memoria
    // Ej de uso.
    // WeakReference<Object> reference = new WeakReference<Object>(object);
    // Object weakObject = reference.get();
    // if (weakObject != null) {
    //
    // }
    public void dereference() {
        this.scene = null;
    }

    // Cuando se llama a esta metodo, la escena ya esta completamente creada y el gameobjet tiene una referencia a ella
    // Por lo tanto, el procedimiento de uso es utilizar la constructora para inicializar los atributos del gameobject
    // y este metodo para coger referencias a otros gameobjects de la escena y acceder al motor
    public void init() {
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setScene(IScene scene) {
        this.scene = scene;
    }
}
