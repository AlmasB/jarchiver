/*
 * Copyright (c) 2014 Almas Baimagambetov (a.baimagambetov1@uni.brighton.ac.uk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.almasb.jarchiver.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

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
        System.gc();

        return null;
    }
}
