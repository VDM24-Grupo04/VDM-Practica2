package com.grupo04.engine.interfaces;

// Se ha creado una clase en el motor común que enmascara un sensor, para
// poder usarlos en la lógica y crear mecánicas a partir de ellos.
// Sin embargo, de momento solo existen sensores en Android
public interface ISensor {
    enum SensorType {GYROSCOPE}

    SensorType getType();

    float[] getValues();
}
