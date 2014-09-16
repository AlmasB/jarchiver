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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.almasb.common.util.ZIPCompressor;

public final class AARCompressTask extends AARTask {

    public AARCompressTask(File[] files) {
        super(files);
    }

    @Override
    protected void taskImpl(File legacyFile) throws Exception {
        if (!legacyFile.isFile()) {
            updateMessage(legacyFile.getAbsolutePath() + " is not a valid file");
            return;
        }

        Path file = legacyFile.toPath();
        final int fileSize = (int) Files.size(file);

        if (fileSize < NUM_BLOCKS) {
            updateMessage("File too small (<" + NUM_BLOCKS + "B)");
            return;
        }

        final int bytesPerBlock = fileSize / (NUM_BLOCKS - 1);
        final int bytesLeft = fileSize % (NUM_BLOCKS - 1);

        ArrayList<AARBlock> blocks = new ArrayList<AARBlock>();

        try (FileChannel fc = FileChannel.open(file)) {
            for (int i = 0; i < NUM_BLOCKS; i++) {
                final AARBlock block = new AARBlock(i);
                blocks.add(block);

                final ByteBuffer buffer = ByteBuffer.allocate(i == NUM_BLOCKS - 1 ? bytesLeft : bytesPerBlock);
                int len;
                do {
                    len = fc.read(buffer);
                } while (len != -1 && buffer.hasRemaining());

                workerThreads.submit(() -> {
                    block.data = new ZIPCompressor().compress(buffer.array());
                    block.ready.countDown();
                    updateProgress(++progress, NUM_BLOCKS);
                });
            }
        }

        try (OutputStream fos = Files.newOutputStream(
                Paths.get(file.toAbsolutePath().toString().concat(".aar")))) {
            for (AARBlock block : blocks) {
                block.ready.await();

                byte[] length = ByteBuffer.allocate(4).putInt(block.data.length).array();
                fos.write(length);
                fos.write(block.data);
            }
        }
    }
}
