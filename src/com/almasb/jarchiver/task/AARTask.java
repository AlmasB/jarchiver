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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;

/*package-private*/ abstract class AARTask extends Task<Void> {

    protected static final int NUM_BLOCKS = 64;

    protected static ExecutorService workerThreads = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Represents one block of compressed data
     * of AAR format
     *
     * @author Almas Baimagambetov (ab607@uni.brighton.ac.uk)
     * @version 1.0
     *
     */
    /*package-private*/ static final class AARBlock {
        public final int number;
        public byte[] data;
        public final CountDownLatch ready = new CountDownLatch(1);

        public AARBlock(int number) {
            this.number = number;
        }
    }
}
