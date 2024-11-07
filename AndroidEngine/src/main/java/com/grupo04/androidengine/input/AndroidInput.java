package com.grupo04.androidengine.input;

import android.annotation.SuppressLint;

import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import com.grupo04.androidengine.utilities.Vector;

import java.util.ArrayList;
import java.util.List;

// Al sobrescribir el metodo setOnTouchListener, se pide sobrescribir tambien el metodo performClick()
// ya que en el caso de que no se pueda usar el input convencion, hay servicios de accesibilidad que invocan
// a dicho metodo, por lo tanto, la logica deberia estar en el. En caso de no sobrescribirlo, se produce un warning
// Sin embargo, como no se van a ser necesarios dichos servicios, se utiliza esta instruccion para eliminar el warning
@SuppressLint("ClickableViewAccessibility")

public class AndroidInput {
    protected List<ITouchEvent> touchEvents;             // Todos los TouchEvents que se reciben en el tick
    protected List<ITouchEvent> sceneTouchEvents;        // TouchEvents que se van a mandar a la escena

    public AndroidInput(SurfaceView window) {
        this.touchEvents = new ArrayList<>();
        this.sceneTouchEvents = new ArrayList<>();

        // Se anade al SurfaceView un listener de tocar la pantalla
        window.setOnTouchListener(new View.OnTouchListener() {
            // Se sobreescribe el onTouch, que se llama cuando
            // se realiza una acción calificada como evento táctil,
            // como, presionar, soltar o cualquier gesto de movimiento
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                float x = event.getX();
                float y = event.getY();
                Vector pos = new Vector(x, y);

                switch (action) {
                    // Se produce este evento cuando el primer dedo toca la pantalla
                    case MotionEvent.ACTION_DOWN:
                        // Se produce este evento cuando los siguientes dedos despues del primero
                        // tocan la pantalla
                        // case MotionEvent.ACTION_POINTER_DOWN:
                        addEvent(new TouchEvent(TouchEvent.TouchEventType.PRESS, pos));
                        break;
                    // Se produce este evento cuando un dedo deja de tocar la pantalla,
                    // pero todavia quedan dedos tocandola
                    case MotionEvent.ACTION_UP:
                        // Se produce este evento cuando el ultimo dedo que habia en la pantalla
                        // deja de tocarla
                        // case MotionEvent.ACTION_POINTER_UP:
                        addEvent(new TouchEvent(TouchEvent.TouchEventType.RELEASE, pos));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // case MotionEvent.ACTION_HOVER_MOVE:
                        addEvent(new TouchEvent(TouchEvent.TouchEventType.DRAG, pos));
                        break;
                }

                // Se devuelve true porque ya se ha gestionado el elemento.
                // Si el elemento tuviera un padre y se quisiera que el padre
                // tambien gestionara el elemento, habria que devolver false
                return true;
            }
        });

        // A diferencia de los touch events, los generic motion events describen eventos de
        // desplazamiento producidos en Android que no implican como tal tocar la pantall, como
        // el desplazamiento de un joystick, de un raton o de un stylus; el movimiento de la
        // rueda del raton...
        window.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent event) {
                int action = event.getActionMasked();
                float x = event.getX();
                float y = event.getY();
                Vector pos = new Vector(x, y);

                switch (action) {
                    // El puntero ha entrado en el componente, en este caso en la ventana
                    case MotionEvent.ACTION_HOVER_ENTER:
                        // El puntero se ha movido
                    case MotionEvent.ACTION_HOVER_MOVE:
                        addEvent(new TouchEvent(TouchEvent.TouchEventType.MOTION, pos));
                }
                return true;
            }
        });
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

    protected synchronized void addEvent(ITouchEvent e) {
        this.touchEvents.add(e);
    }
}
