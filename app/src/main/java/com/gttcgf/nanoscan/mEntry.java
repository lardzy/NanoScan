package com.gttcgf.nanoscan;

import java.io.Serializable;
import java.util.ArrayList;

public class mEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private final float x, y;

    public mEntry(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
