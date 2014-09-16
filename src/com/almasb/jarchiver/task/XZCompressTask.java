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

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZOutputStream;

public final class XZCompressTask extends JArchiverTask {

    private final LZMA2Options options = new LZMA2Options();

    public XZCompressTask(File[] files, int preset) {
        super(files);
        try {
            options.setPreset(preset);
        }
        catch (UnsupportedOptionsException e) {
            // preset values are predefined, so this won't happen
        }
    }

    @Override
    protected void taskImpl(File file) throws Exception {
        if (!file.isFile()) {
            updateMessage(file.getAbsolutePath() + " is not a valid file");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".xz");
                XZOutputStream out = new XZOutputStream(fos, options)) {

            byte[] buffer = new byte[8192];
            int len = 0;
            int fileSize = (int) file.length();

            while ((len = fis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                progress += len;
                updateProgress(progress, fileSize);
            }
        }
    }
}
