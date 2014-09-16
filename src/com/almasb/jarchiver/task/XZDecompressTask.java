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

import org.tukaani.xz.XZInputStream;

public final class XZDecompressTask extends JArchiverTask {

    public XZDecompressTask(File[] files) {
        super(files);
    }

    @Override
    protected void taskImpl(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(
                        file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-3));
                XZInputStream in = new XZInputStream(fis)) {

            final int fileSize = (int) file.length();
            byte[] buffer = new byte[8192];
            int len;

            while ((len = in.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
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
