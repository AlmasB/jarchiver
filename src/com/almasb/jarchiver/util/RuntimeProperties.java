package com.almasb.jarchiver.util;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;

public final class RuntimeProperties {

    private static final double MB = 1024.0 * 1024.0;

    private static Runtime runtime = Runtime.getRuntime();

    private static SimpleDoubleProperty usedMemory = new SimpleDoubleProperty();
    private static SimpleDoubleProperty freeMemory = new SimpleDoubleProperty(runtime.freeMemory());
    private static SimpleDoubleProperty totalJVMMemory = new SimpleDoubleProperty(runtime.totalMemory());
    private static SimpleDoubleProperty maxJVMMemory = new SimpleDoubleProperty(runtime.maxMemory());

    static {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                usedMemory.set((runtime.totalMemory() - runtime.freeMemory()) / MB);
                totalJVMMemory.set(runtime.totalMemory() / MB);
            });
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private RuntimeProperties() {}

    public static SimpleDoubleProperty usedMemoryProperty() {
        return usedMemory;
    }

    public static SimpleDoubleProperty totalJVMMemoryProperty() {
        return totalJVMMemory;
    }
}
