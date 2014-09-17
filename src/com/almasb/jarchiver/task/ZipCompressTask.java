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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class ZipCompressTask extends JArchiverTask {

    public ZipCompressTask(Path[] files) {
        super(files);
    }

    @Override
    protected void taskImpl(Path file) throws Exception {
        if (Files.isDirectory(file)) {
            ArrayList<Path> files = new ArrayList<Path>();

            Files.walk(file).forEach(innerFile -> {
                if (Files.isRegularFile(innerFile))
                    files.add(innerFile);
            });

            try (OutputStream fos = Files.newOutputStream(Paths.get(file.toAbsolutePath().toString().concat(".jar")));
                    JarOutputStream jos = new JarOutputStream(fos)) {

                for (Path aFile : files) {
                    String name = file.getFileName().resolve(file.relativize(aFile)).toString();
                    updateMessage("Compressing: " + name);

                    JarEntry entry = new JarEntry(name);
                    jos.putNextEntry(entry);
                    // assuming files aren't too big otherwise
                    // use the technique below 'read-write'
                    jos.write(Files.readAllBytes(aFile));
                    jos.closeEntry();

                    updateProgress(++progress, files.size());
                }
            }
        }
        else if (Files.isRegularFile(file)) {
            try (InputStream fis = Files.newInputStream(file);
                    OutputStream fos = Files.newOutputStream(Paths.get(file.toAbsolutePath().toString().concat(".jar")));
                    JarOutputStream jos = new JarOutputStream(fos)) {

                final int fileSize = (int) Files.size(file);

                JarEntry entry = new JarEntry(file.getFileName().toString());
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
}
