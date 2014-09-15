package com.almasb.jarchiver.task;

import java.io.File;

import com.almasb.common.util.Out;

import javafx.concurrent.Task;

// TODO: use java.nio.file.Files in all subclasses

/*package-private*/ abstract class JArchiverTask extends Task<Void> {

    private final File[] files;
    protected int progress = 0;

    public JArchiverTask(File[] files) {
        this.files = files;
    }

    /**
     * This method is called on the background thread
     */
    @Override
    protected Void call() throws Exception {
        for (File file : files) {
            try {
                long start = System.nanoTime();
                updateProgress(-1, -1); // set indeterminate
                updateProgress(progress, 100);
                updateMessage("Executing task on: " + file.getAbsolutePath());
                taskImpl(file);
                updateMessage(String.format("Task took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
                System.gc();
            }
            catch (Exception e) {
                updateMessage("Failed to complete task on " + file.getAbsolutePath());
                Out.e(e);
            }
        }

        return null;
    }

    protected abstract void taskImpl(File file) throws Exception;
}
