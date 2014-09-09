package com.almasb.jarchiver.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.almasb.common.util.Out;

import javafx.concurrent.Task;

public final class ZipDecompressTask extends Task<Void> {

    private final File[] files;

    public ZipDecompressTask(File... files) {
        this.files = files;
    }

    @Override
    protected Void call() throws Exception {
        long start = System.nanoTime();

        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".jar"))
                continue;

            FileInputStream fis = new FileInputStream(file);
            JarInputStream jis = new JarInputStream(fis);

            FileOutputStream fos;

            JarEntry entry = null;
            while ((entry = jis.getNextJarEntry()) != null) {
                String fileName = entry.getName();

                if (fileName.contains(File.separator)) {
                    File parentDirs = new File(file.getParent() + File.separator + fileName.substring(0, fileName.lastIndexOf(File.separator)) + File.separator);
                    if (!parentDirs.exists()) {
                        parentDirs.mkdirs();
                    }
                }

                updateMessage("Decompressing: " + fileName);

                fos = new FileOutputStream(file.getParent() + File.separator + fileName);
                byte[] buf = new byte[8192];
                int len;
                while ((len = jis.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                fos.close();
            }

            jis.close();
            fis.close();
        }

        updateMessage(String.format("Decompression took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
        //System.gc();

        return null;
    }
}
