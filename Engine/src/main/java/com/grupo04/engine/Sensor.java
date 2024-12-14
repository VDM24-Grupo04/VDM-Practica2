package com.grupo04.engine;

import com.grupo04.engine.interfaces.ISensor;

public class Sensor implements ISensor {
    private SensorType type;
    private float[] values;

    public Sensor(SensorType type, float[] values) {
        this.type = type;
        this.values = values;
    }

    public SensorType getType() {
        return type;
    }

    public float[] getValues() {
        return values;
    }
}
