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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import com.almasb.common.util.ZIPCompressor;

public final class AARDecompressTask extends AARTask {

    public AARDecompressTask(File[] files) {
        super(files);
    }

    @Override
    protected void taskImpl(File file) throws Exception {
        int offset = 0;

        byte[] data = Files.readAllBytes(file.toPath());

        ArrayList<AARBlock> blocks = new ArrayList<AARBlock>();
        for (int i = 0; i < NUM_BLOCKS; i++) {
            final int length = ByteBuffer.wrap(Arrays.copyOfRange(data, offset, offset + 4)).getInt();
            offset += 4;

            final AARBlock block = new AARBlock(i);
            blocks.add(block);

            final int localOffset = offset;

            workerThreads.submit(() -> {
                block.data = new ZIPCompressor().decompress(Arrays.copyOfRange(data, localOffset, localOffset + length));
                block.ready.countDown();
                updateProgress(++progress, NUM_BLOCKS);
            });

            offset += length;
        }

        try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4))) {
            for (AARBlock block : blocks) {
                block.ready.await();
                fos.write(block.data);
            }
        }
    }
}
