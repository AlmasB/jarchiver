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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parent AARTask which holds info about the AAR format
 *
 * Also the same shared {@link java.util.concurrent.ExecutorService}
 * both for compression and decompression
 *
 * @author Almas Baimagambetov (ab607@uni.brighton.ac.uk)
 * @version 1.0
 *
 */
/*package-private*/ abstract class AARTask extends JArchiverTask {

    /**
     * The AAR file consists of this many blocks
     */
    protected static final int NUM_BLOCKS = 64;

    /**
     * Shared worker threads
     */
    protected static ExecutorService workerThreads = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public AARTask(File[] files) {
        super(files);
    }

    /**
     * Represents one block of compressed data
     * of AAR format
     *
     * @author Almas Baimagambetov (ab607@uni.brighton.ac.uk)
     * @version 1.0
     *
     */
    /*package-private*/ static final class AARBlock {
        /**
         * Block number in sequence, i.e. 1st, 2nd, etc
         */
        public final int number;

        /**
         * The data block holds
         */
        public byte[] data;

        /**
         * Whether this block is ready to be written / read
         */
        public final CountDownLatch ready = new CountDownLatch(1);

        /**
         * Creates an AAR block with given position
         *
         * @param number
         *              the position in sequence
         */
        public AARBlock(int number) {
            this.number = number;
        }
    }
}
