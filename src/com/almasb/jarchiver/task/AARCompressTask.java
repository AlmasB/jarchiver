package com.almasb.jarchiver.task;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import com.almasb.jarchiver.util.ZIPCompressor;

public final class AARCompressTask extends AARTask {

    private final File file;

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

        //final CountDownLatch latch = new CountDownLatch(NUM_BLOCKS);
        IntegerProperty progress = new SimpleIntegerProperty();

        for (int i = 0; i < NUM_BLOCKS; i++) {
            final AARBlock block = new AARBlock(i);
            blocks.add(block);
            workerThreads.submit(() -> {
                block.data = new ZIPCompressor().compress(Arrays.copyOfRange(data, block.number*bytesPerBlock,
                        block.number*bytesPerBlock + (block.number == NUM_BLOCKS - 1 ? bytesLeft : bytesPerBlock)));

                block.ready.countDown();
                //latch.countDown();
                progress.set(progress.get() + 1);
                updateProgress(progress.get(), NUM_BLOCKS);
            });
        }

        //latch.await();

        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".aar");

        for (AARBlock block : blocks) {
            block.ready.await();

            byte[] length = ByteBuffer.allocate(4).putInt(block.data.length).array();
            fos.write(length);
            fos.write(block.data);
        }

        fos.close();

        updateMessage(String.format("Compression took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
        //System.gc();

        return null;
    }

    //      FileInputStream fis = new FileInputStream("sdl.zip");
    //
    //      byte[] data = ByteWriter.getBytes(fis);
    //      fis.close();
    //
    //      if (data.length < 32) return;
    //
    //
    //
    //      int bytesPerAARBlock = data.length / 32;
    //      int bytesLeft = data.length % 32;
    //
    //      System.out.println(data.length + " " + bytesPerAARBlock + " " + bytesLeft);
    //
    //      ArrayList<AARBlock> AARBlocks = new ArrayList<AARBlock>();
    //
    //      final CountDownLatch latch = new CountDownLatch(33);
    //
    //      for (int i = 0; i < 32; i++) {
    //          final AARBlock AARBlock = new AARBlock(i);
    //          AARBlocks.add(AARBlock);
    //          workers.submit(() -> {
    //              //if (AARBlock.number != 31)
    //              AARBlock.data = new ZIPCompressor().compress(Arrays.copyOfRange(data, AARBlock.number*bytesPerAARBlock, AARBlock.number*bytesPerAARBlock + bytesPerAARBlock));
    //              //else
    //              //AARBlock.data = new ZIPCompressor().compress(Arrays.copyOfRange(data, AARBlock.number*bytesPerAARBlock, AARBlock.number*bytesPerAARBlock + bytesLeft));
    //
    //              latch.countDown();
    //          });
    //      }
    //
    //      workers.submit(() -> {
    //
    //          AARBlock AARBlock = new AARBlock(32);
    //          AARBlocks.add(AARBlock);
    //
    //          AARBlock.data = new ZIPCompressor().compress(Arrays.copyOfRange(data, AARBlock.number*bytesPerAARBlock, AARBlock.number*bytesPerAARBlock + bytesLeft));
    //
    //          latch.countDown();
    //      });
    //
    //      latch.await();
    //
    //      workers.shutdownNow();
    //
    //
    //
    //
    //
    //      FileOutputStream fos = new FileOutputStream("sdl.aar");
    //
    //
    //      for (AARBlock AARBlock : AARBlocks) {
    //          byte[] length = ByteBuffer.allocate(4).putInt(AARBlock.data.length).array();
    //          System.out.println(AARBlock.number + " : " + ByteBuffer.wrap(length).getInt());
    //
    //          fos.write(length);
    //          fos.write(AARBlock.data);
    //      }
    //
    //      fos.close();
}
