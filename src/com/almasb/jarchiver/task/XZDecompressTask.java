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

import org.tukaani.xz.XZInputStream;

public final class XZDecompressTask extends JArchiverTask {

    public XZDecompressTask(Path[] files) {
        super(files);
    }

    @Override
    protected void taskImpl(Path file) throws Exception {
        try (InputStream fis = Files.newInputStream(file);
                OutputStream os = Files.newOutputStream(Paths.get(file.toAbsolutePath().toString().replace(".xz", "")));
                XZInputStream in = new XZInputStream(fis)) {

            final int fileSize = (int) Files.size(file);
            byte[] buffer = new byte[8192];
            int len;

            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
                // the following isn't entirely correct
                // fileSize is of a compressed file
                // whereas len is number of bytes to write for uncompressed
                // nevertheless this is at least some way of telling the user
                // that we are doing something, it's just the bar will reach 100%
                // faster than it should
                progress += len;
                updateProgress(progress, fileSize);
            }
        }
    }
}
