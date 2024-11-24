package com.grupo04.engine;

import com.grupo04.engine.interfaces.ITouchEvent;

import java.util.ArrayList;
import java.util.List;

public class Input {
    protected List<ITouchEvent> touchEvents;             // Todos los TouchEvents que se reciben en el tick
    protected List<ITouchEvent> sceneTouchEvents;        // TouchEvents que se van a mandar a la escena

    protected final int MAX_EVENTS;                     // Tamano de la pool de eventos
    protected int oldestEvent;                          // Indice del evento mas antiguo en la pool
    protected TouchEvent[] eventPool;                   // Pool de eventos reutilizables

    // Se usan 2 listas porque la deteccion del input y la ejecucion del juego se hacen en hilos
    // distintos, por lo que se puede estar recibiendo input a la vez que se esta gestionando,

    public Input() {
        this.touchEvents = new ArrayList<>();
        this.sceneTouchEvents = new ArrayList<>();

        this.MAX_EVENTS = 10;
        this.oldestEvent = 0;
        this.eventPool = new TouchEvent[this.MAX_EVENTS];
        for (int i = 0; i < this.MAX_EVENTS; i++) {
            this.eventPool[i] = new TouchEvent(ITouchEvent.TouchEventType.NONE, 0, 0);
        }
    }

    // Obtiene los TouchEvents que le va a mandar a la escena
    // Tienen que ser synchronized porque se puede estar modificando la lista
    // en el hilo que recoge el input y a la vez en el bucle principal. Con synchronized
    // se asegura que la lista no pueda ser modificada por 2 hilos al mismo tiempo
    public synchronized List<ITouchEvent> getTouchEvents() {
        // Se reinicia la lista de elementos que enviar a la escena
        this.sceneTouchEvents.clear();

        // Si hay eventos de input
        if (!this.touchEvents.isEmpty()) {
            // addAll clona en aux los elementos de touchEvents
            this.sceneTouchEvents.addAll(this.touchEvents);

            // Limpia los eventos porque ya se los ha pasado a la escena
            this.touchEvents.clear();
        }

        // Devuelve los TouchEvents de la escena
        return this.sceneTouchEvents;
    }

    // Anade un evento a la lista de eventos
    protected synchronized void addEvent(ITouchEvent.TouchEventType type, int posX, int posY) {
        // Coge el evento mas antiguo y lo sobreescribe
        TouchEvent evt = this.eventPool[this.oldestEvent];
        evt.setType(type);
        evt.setPos(posX, posY);

        // Actualiza el evento para que el mas antiguo sea el siguiente (como se van sobreescribiendo
        // de 0 a MAX_EVENTS, cuando vuelva a llegar a 0, ese sera el evento mas antiguo)
        this.oldestEvent++;
        this.oldestEvent %= this.MAX_EVENTS;

        // Anade el evento a la lista
        this.touchEvents.add(evt);
    }
}
