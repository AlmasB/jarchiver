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

    private final File file;
    private int offset = 0;
    private int progress = 0;

    public AARDecompressTask(File file) {
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        if (!file.isFile() || !file.getName().endsWith(".aar")) {
            updateMessage("Not .aar file");
            return null;
        }

        long start = System.nanoTime();

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

        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4));

        for (AARBlock block : blocks) {
            block.ready.await();
            fos.write(block.data);
        }

        fos.close();

        updateMessage(String.format("Decompression took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
        //System.gc();

        return null;
    }

    //private static int offset = 0;

    //            FileInputStream fis = new FileInputStream("sdl.aar");
    //
    //            byte[] data = ByteWriter.getBytes(fis);
    //            fis.close();
    //
    //
    //            ArrayList<Block> blocks = new ArrayList<Block>();
    //
    //            final CountDownLatch latch = new CountDownLatch(32);
    //
    //            for (int i = 0; i < 33; i++) {
    //                final int length = ByteBuffer.wrap(Arrays.copyOfRange(data, offset, offset + 4)).getInt();
    //                System.out.println(i + " : " + length);
    //                offset += 4;
    //
    //                final Block block = new Block(i);
    //                blocks.add(block);
    //
    //                final int localOffset = offset;
    //
    //                workers.submit(() -> {
    //                    byte[] dd = Arrays.copyOfRange(data, localOffset, localOffset + length);
    //
    //                    System.out.println(block.number + "block : " + dd.length);
    //
    //                    block.data = new ZIPCompressor().decompress(dd);
    //                    latch.countDown();
    //                });
    //
    //                offset += length;
    //            }
    //
    //
    //            latch.await();
    //
    //            workers.shutdownNow();
    //
    //
    //            FileOutputStream fos = new FileOutputStream("sdl.zip");
    //
    //            for (Block block : blocks) {
    //                fos.write(block.data);
    //            }
    //
    //            fos.close();
}
