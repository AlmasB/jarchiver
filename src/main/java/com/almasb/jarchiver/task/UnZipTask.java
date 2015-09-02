package com.almasb.jarchiver.task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipFile;

import javafx.concurrent.Task;

public class UnZipTask extends Task<Void> {
    private final Path file;

    public UnZipTask(Path file) {
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        try (ZipFile zipFile = new ZipFile(file.toFile())) {
            int files = zipFile.size();
            CountDownLatch latch = new CountDownLatch(files);

            zipFile.stream().parallel().forEach(entry -> {
                try {
                    updateMessage("Unzipping " + entry.getName());
                    Path outEntry = file.getParent().resolve(entry.getName());

                    if (entry.isDirectory()) {
                        Files.createDirectories(outEntry);
                    }
                    else {
                        Files.createDirectories(outEntry.getParent());
                        Files.copy(zipFile.getInputStream(entry), outEntry);
                    }

                    latch.countDown();
                    updateProgress(files - latch.getCount(), files);
                }
                catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            });

            latch.await();
        }

        return null;
    }
}
