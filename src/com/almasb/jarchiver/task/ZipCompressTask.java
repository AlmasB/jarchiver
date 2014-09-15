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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import com.almasb.common.util.Out;
import com.almasb.java.io.ResourceManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public final class ZipCompressTask extends Task<Void> {

    private final File[] files;

    public ZipCompressTask(File... files) {
        this.files = files;
    }

    @Override
    protected Void call() throws Exception {
        long start = System.nanoTime();

        for (File file : files) {
            if (file.isDirectory()) {
                ArrayList<File> files = new ArrayList<File>();
                loadFileNames(file, files);

                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".jar");
                JarOutputStream jos = new JarOutputStream(fos);

                final SimpleIntegerProperty progress = new SimpleIntegerProperty(0);

                files.stream().forEach(aFile -> {
                    try {
                        String name = aFile.getAbsolutePath();
                        name = name.substring(name.indexOf(file.getName()));

                        updateMessage("Compressing: " + name);

                        JarEntry entry = new JarEntry(name);
                        jos.putNextEntry(entry);
                        jos.write(ResourceManager.loadResourceAsByteArray(aFile.getAbsolutePath()));
                        jos.closeEntry();

                        progress.set(progress.get() + 1);
                        updateProgress(progress.get(), files.size());
                    }
                    catch (Exception e) {
                        Out.i("Failed to compress " + aFile.getAbsolutePath());
                        Out.e("call()", "Failed to compress file", this, e);
                    }
                });

                jos.close();
                fos.close();
            }
            else {
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".jar");
                JarOutputStream jos = new JarOutputStream(fos);

                final SimpleIntegerProperty progress = new SimpleIntegerProperty(0);

                try {
                    JarEntry entry = new JarEntry(file.getName());
                    jos.putNextEntry(entry);
                    jos.write(ResourceManager.loadResourceAsByteArray(file.getAbsolutePath()));
                    jos.closeEntry();

                    progress.set(progress.get() + 1);
                    updateProgress(progress.get(), 1);
                }
                catch (Exception e) {
                    Out.i("Failed to compress " + file.getAbsolutePath());
                    Out.e("call()", "Failed to compress file", this, e);
                }

                jos.close();
                fos.close();
            }
        }

        updateMessage(String.format("Compression took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
        System.gc();

        return null;
    }

    private void loadFileNames(File folder, ArrayList<File> files) {
        File[] allFiles = folder.listFiles();
        if (allFiles == null) {
            return;
        }

        for (File file : allFiles) {
            if (file.isDirectory()) {
                loadFileNames(file, files);
            }
            else {
                //                String name = aFile.toString();
                //                if (name.contains(File.separator)) {
                //                    name = name.substring(name.lastIndexOf(File.separator) + 1);
                //                }
                files.add(file);
            }
        }
    }
}
