package com.almasb.jarchiver.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javafx.concurrent.Task;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZOutputStream;

import com.almasb.common.util.Out;

public final class XZCompressTask extends Task<Void> {

    private final LZMA2Options options = new LZMA2Options();
    private final File file;

    public XZCompressTask(File file, int preset) {
        this.file = file;
        try {
            options.setPreset(preset);
        }
        catch (UnsupportedOptionsException e) {
            // preset values are predefined, so this won't happen
        }
    }

    @Override
    protected Void call() throws Exception {
        if (!file.isFile()) {
            updateMessage(file.getAbsolutePath() + " is not a normal file");
            return null;
        }

        long start = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(
                        file.getAbsolutePath().concat(".xz"));
                XZOutputStream out = new XZOutputStream(fos, options)) {

            byte[] buffer = new byte[8192];
            int readBytes = 0, processedSize = 0, fileSize = (int) file.length();

            while ((readBytes = fis.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
                processedSize += readBytes;
                updateProgress(processedSize, fileSize);
            }
        }
        catch (Exception e) {
            updateMessage("Compression failed: " + e.getMessage());
            Out.e(e);
        }

        updateMessage(String.format("Compression took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
        System.gc();

        return null;
    }
}
