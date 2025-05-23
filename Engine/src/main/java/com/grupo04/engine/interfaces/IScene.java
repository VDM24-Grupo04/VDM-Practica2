package com.grupo04.engine.interfaces;

import com.grupo04.engine.Sensor;

import java.util.List;

public interface IScene {
    void init();

    void handleInput(List<ITouchEvent> touchEvents);

    void update(double deltaTime);

    void fixedUpdate(double fixedDeltaTime);

    void render(IGraphics graphics);

    void refresh();

    void shutdown();

    void sensorChanged(Sensor sensor);
}
