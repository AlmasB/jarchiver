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
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import com.almasb.java.io.ResourceManager;

public final class ZipCompressTask extends JArchiverTask {

    public ZipCompressTask(File[] files) {
        super(files);
    }

    @Override
    protected void taskImpl(File file) throws Exception {
        if (file.isDirectory()) {

            ArrayList<File> files = new ArrayList<File>();
            loadFileNames(file, files);

            try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".jar");
                    JarOutputStream jos = new JarOutputStream(fos)) {

                for (File aFile : files) {
                    String name = aFile.getAbsolutePath();
                    name = name.substring(name.indexOf(file.getName()));

                    updateMessage("Compressing: " + name);

                    JarEntry entry = new JarEntry(name);
                    jos.putNextEntry(entry);
                    // assuming files aren't too big otherwise
                    // use the technique below 'read-write'
                    jos.write(ResourceManager.loadResourceAsByteArray(aFile.getAbsolutePath()));
                    jos.closeEntry();

                    updateProgress(++progress, files.size());
                }
            }
        }
        else if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                    FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".jar");
                    JarOutputStream jos = new JarOutputStream(fos)) {

                final int fileSize = (int) file.length();

                JarEntry entry = new JarEntry(file.getName());
                jos.putNextEntry(entry);

                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    jos.write(buffer, 0, len);
                    progress += len;
                    updateProgress(progress, fileSize);
                }

                jos.closeEntry();
            }
        }
    }

    /**
     * Recursively load file names from the folder
     *
     * @param folder
     * @param files
     */
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
                files.add(file);
            }
        }
    }
}
