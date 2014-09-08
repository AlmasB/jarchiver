package com.almasb.jarchiver.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import javafx.concurrent.Task;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZOutputStream;

import com.almasb.common.util.Out;
import com.almasb.java.io.ByteWriter;

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
                        file.getAbsolutePath().toString().concat(".xz"));
                XZOutputStream out = new XZOutputStream(fos, options)) {

            byte[] buffer = new byte[8192];
            int readBytes = 0, processedSize = 0, fileSize = (int) file.length();



            //            byte[] data = ByteWriter.getBytes(fis);
            //            int fileInSize = data.length;
            //            int processedSize = 0;
            //
            //            byte[] buffer = new byte[8192];
            //
            //            int iterations = fileInSize / 8192;
            //            int bytesLeft = fileInSize % 8192;
            //
            //            for (int i = 0; i < iterations; i++) {
            //                buffer = Arrays.copyOfRange(data, i*8192, i*8192 + 8192);
            //                out.write(buffer);
            //                processedSize += 8192;
            //                updateProgress(processedSize, fileInSize);
            //            }
            //
            //            buffer = Arrays.copyOfRange(data, iterations*8192, iterations*8192 + bytesLeft);
            //            out.write(buffer);

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
        //System.gc();

        return null;
    }
}
