package com.almasb.jarchiver.task;

import java.nio.file.Path;

import com.almasb.common.util.Out;

import javafx.concurrent.Task;

/*package-private*/ abstract class JArchiverTask extends Task<Void> {

    private final Path[] files;
    protected int progress = 0;

    public JArchiverTask(Path[] files) {
        this.files = files;
    }

    /**
     * This method is called on the background thread
     */
    @Override
    protected Void call() throws Exception {
        long start = System.nanoTime();
        for (Path file : files) {
            try {
                updateProgress(-1, -1); // set indeterminate
                progress = 0;
                updateProgress(progress, 100);
                updateMessage("Executing task on: " + file.toAbsolutePath());
                taskImpl(file);

                System.gc();
            }
            catch (Exception e) {
                updateMessage("Failed to complete task on " + file.toAbsolutePath());
                Out.e(e);
                return null;
            }
        }
        updateMessage(String.format("Task took: %.3f s", (System.nanoTime() - start) / 1000000000.0));

        return null;
    }

    /**
     * Actual implementation of the compression/decompression task
     *
     * @param file
     * @throws Exception
     */
    protected abstract void taskImpl(Path file) throws Exception;
}
