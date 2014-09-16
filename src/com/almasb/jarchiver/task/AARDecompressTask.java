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

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.almasb.common.util.ZIPCompressor;

public final class AARDecompressTask extends AARTask {

    public AARDecompressTask(Path[] files) {
        super(files);
    }

    @Override
    protected void taskImpl(Path file) throws Exception {
        ArrayList<AARBlock> blocks = new ArrayList<AARBlock>();

        try (FileChannel fc = FileChannel.open(file)) {
            for (int i = 0; i < NUM_BLOCKS; i++) {
                // create 4byte buffer and read data length
                ByteBuffer dataLength = ByteBuffer.allocate(4);
                int len;
                do {
                    len = fc.read(dataLength);
                } while (len != -1 && dataLength.hasRemaining());

                // create data length buffer and read data
                final ByteBuffer buffer = ByteBuffer.allocate(dataLength.getInt(0));
                do {
                    len = fc.read(buffer);
                } while (len != -1 && buffer.hasRemaining());

                final AARBlock block = new AARBlock(i);
                blocks.add(block);

                workerThreads.submit(() -> {
                    block.data = new ZIPCompressor().decompress(buffer.array());
                    block.ready.countDown();
                    updateProgress(++progress, NUM_BLOCKS);
                });
            }
        }

        // write the decompressed data, removing ".aar"
        try (OutputStream os = Files.newOutputStream(
                Paths.get(file.toAbsolutePath().toString().replace(".aar", "")))) {
            for (AARBlock block : blocks) {
                block.ready.await();
                os.write(block.data);
            }
        }
    }
}
