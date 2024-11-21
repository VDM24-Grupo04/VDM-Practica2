package com.grupo04.desktopengine;

import com.grupo04.engine.Engine;
import com.grupo04.engine.Input;
import com.grupo04.engine.TouchEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

public class DesktopInput extends Input {
    DesktopInput(JFrame window, Engine engine) {
        super();

        // Se añade al JFrame un listener de raton
        window.addMouseListener(new MouseAdapter() {
            // Se sobrescribe el evento de pulsar el raton
            @Override
            public void mousePressed(MouseEvent e) {
                addEvent(TouchEvent.TouchEventType.PRESS, e.getX(), e.getY());
            }

            // Se sobrescribe el evento de soltar el raton
            @Override
            public void mouseReleased(MouseEvent e) {
                addEvent(TouchEvent.TouchEventType.RELEASE, e.getX(), e.getY());
            }
        });

        // Se anade al JFrame un listener de movimiento del raton
        window.addMouseMotionListener(new MouseAdapter() {
            // Se sobrescribe el evento de arrastrar el raton
            @Override
            public void mouseDragged(MouseEvent e) {
                addEvent(TouchEvent.TouchEventType.DRAG, e.getX(), e.getY());
            }

            // Se sobrescribe el evento de que ha entrado el raton en el componente, en este caso en la ventana
            @Override
            public void mouseEntered(MouseEvent e) {
                addEvent(TouchEvent.TouchEventType.MOTION, e.getX(), e.getY());
            }

            // Se sobrescribe el evento de que se ha movido el raton
            @Override
            public void mouseMoved(MouseEvent e) {
                addEvent(TouchEvent.TouchEventType.MOTION, e.getX(), e.getY());
            }
        });

    }
}
