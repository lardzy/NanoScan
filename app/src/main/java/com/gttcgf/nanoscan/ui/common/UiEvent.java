package com.gttcgf.nanoscan.ui.common;

public class UiEvent<T> extends Event<T> {
    public UiEvent(T content) {
        super(content);
    }
}
