package com.almasb.jarchiver.task;

import javafx.concurrent.Task;

public class XZDecompressTask extends Task<Void> {
    @Override
    protected Void call() throws Exception {


        //                    fileToDecompress.ifPresent(file -> {
        //                        try (FileInputStream fis = new FileInputStream(file);
        //                                FileOutputStream fos = new FileOutputStream(
        //                                        file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-3));
        //                                XZInputStream in = new XZInputStream(fis)) {
        //
        //                            byte[] buf = new byte[8192];
        //                            int size;
        //                            while ((size = in.read(buf)) != -1)
        //                                fos.write(buf, 0, size);
        //                        }
        //                        catch (Exception e) {
        //                            Out.i("Failed to decompress");
        //                            Out.e(e);
        //                        }
        //                    });

        //                    byte[] buf = new byte[8192];
        //
        //                    InputStream in = new FileInputStream("sdl.xz");
        //                    FileOutputStream fos = new FileOutputStream("sdl.zip");
        //
        //                    try {
        //                        in = new XZInputStream(in);
        //
        //                        int size;
        //                        while ((size = in.read(buf)) != -1)
        //                            fos.write(buf, 0, size);
        //                    }
        //                    finally {
        //                        in.close();
        //                        fos.close();
        //                    }

        return null;
    }
}
