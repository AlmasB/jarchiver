package com.almasb.jarchiver.task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javafx.concurrent.Task;

public class ZipTask extends Task<Void> {

    private static final Predicate<Path> NOT_DIRECTORY = path -> !Files.isDirectory(path);

    private List<File> files;

    public ZipTask(List<File> files) {
        this.files = files;
    }

    @Override
    protected Void call() throws Exception {
        Path outDir = files.get(0).toPath().getParent();
        Path outFile = outDir.resolve(outDir.getFileName().toString() + ".zip");

        try (ZipOutputStream zipOut = new ZipOutputStream(
                new BufferedOutputStream(Files.newOutputStream(outFile)))) {

            files.stream()
                .map(File::toPath)
                .flatMap(this::expand)
                .filter(NOT_DIRECTORY)
                .forEach(path -> {
                    String entryName = outDir.relativize(path).toString();
                    updateMessage("Zipping: " + entryName);

                    ZipEntry entry = new ZipEntry(entryName);
                    try {
                        zipOut.putNextEntry(entry);
                        Files.copy(path, zipOut);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        }

        return null;
    }

    private Stream<Path> expand(Path path) {
        try {
            return Files.walk(path);
        }
        catch (Exception e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }
}
