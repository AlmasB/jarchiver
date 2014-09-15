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

public final class AARCompressTask extends AARTask {

    private final File file;
    private int progress = 0;

    public AARCompressTask(File file) {
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        if (!file.isFile()) {
            updateMessage("Not a file");
            return null;
        }

        updateProgress(0, NUM_BLOCKS);
        long start = System.nanoTime();

        byte[] data = Files.readAllBytes(file.toPath());

        if (data.length < NUM_BLOCKS) {
            updateMessage("File too small (<" + NUM_BLOCKS + "B)");
            return null;
        }

        int bytesPerBlock = data.length / (NUM_BLOCKS - 1);
        int bytesLeft = data.length % (NUM_BLOCKS - 1);

        ArrayList<AARBlock> blocks = new ArrayList<AARBlock>();

        for (int i = 0; i < NUM_BLOCKS; i++) {
            final AARBlock block = new AARBlock(i);
            blocks.add(block);
            workerThreads.submit(() -> {
                block.data = new ZIPCompressor().compress(Arrays.copyOfRange(data, block.number*bytesPerBlock,
                        block.number*bytesPerBlock + (block.number == NUM_BLOCKS - 1 ? bytesLeft : bytesPerBlock)));

                block.ready.countDown();
                updateProgress(++progress, NUM_BLOCKS);
            });
        }

        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".aar");

        for (AARBlock block : blocks) {
            block.ready.await();

            byte[] length = ByteBuffer.allocate(4).putInt(block.data.length).array();
            fos.write(length);
            fos.write(block.data);
        }

        fos.close();

        updateMessage(String.format("Compression took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
        System.gc();

        return null;
    }
}
