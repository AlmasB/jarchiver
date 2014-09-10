package com.almasb.jarchiver.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javafx.concurrent.Task;

import org.tukaani.xz.XZInputStream;

public class XZDecompressTask extends Task<Void> {

    private final File file;

    public XZDecompressTask(File file) {
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        if (!file.isFile() || !file.getName().endsWith(".xz")) {
            updateMessage("Not .xz file");
            return null;
        }

        long start = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(
                        file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-3));
                XZInputStream in = new XZInputStream(fis)) {

            byte[] buf = new byte[8192];
            int size;
            while ((size = in.read(buf)) != -1)
                fos.write(buf, 0, size);
        }
        catch (Exception e) {
            updateMessage("Decompression failed");
        }

        updateMessage(String.format("Compression took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
        //System.gc();

        return null;
    }
}
