package com.grupo04.engine.interfaces;

public interface ISensor {
    enum SensorType {GYROSCOPE}

    SensorType getType();

    float[] getValues();
}
