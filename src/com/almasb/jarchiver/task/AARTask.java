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
