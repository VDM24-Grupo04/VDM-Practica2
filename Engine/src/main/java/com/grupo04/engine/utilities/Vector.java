package com.grupo04.engine.utilities;

public class Vector {
    public float x;
    public float y;

    public Vector() {
        this.x = 0;
        this.y = 0;
    }

    public Vector(float xy) {
        this.x = xy;
        this.y = xy;
    }

    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Vector other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector plus(Vector other) {
        return new Vector(this.x + other.x, this.y + other.y);
    }

    public Vector minus(Vector other) {
        return new Vector(this.x - other.x, this.y - other.y);
    }

    public Vector times(float scalar) {
        return new Vector(this.x * scalar, this.y * scalar);
    }

    public Vector div(float scalar) {
        return new Vector(this.x / scalar, this.y / scalar);
    }

    public boolean equals(Vector other) {
        return this.x == other.x && this.y == other.y;
    }

    public boolean notEquals(Vector other) {
        return !equals(other);
    }

    public float magnitudeSquared() {
        return x * x + y * y;
    }

    public float magnitude() {
        return (float) Math.sqrt(magnitudeSquared());
    }

    public Vector getNormalized() {
        float magnitude = magnitude();
        Vector normalizedVector = new Vector();
        if (magnitude != 0) {
            normalizedVector = this.div(magnitude);
        }
        return normalizedVector;
    }

    public void normalize() {
        Vector normalizedVector = getNormalized();
        this.x = normalizedVector.x;
        this.y = normalizedVector.y;
    }

    public float distance(Vector other) {
        Vector aux = this.minus(other);
        return aux.magnitude();
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Vector lerp(Vector start, Vector end, float t) {
        t = clamp(t, 0, 1);
        float lx = (1 - t) * start.x + t * end.x;
        float ly = (1 - t) * start.y + t * end.y;
        return new Vector(lx, ly);
    }
}
