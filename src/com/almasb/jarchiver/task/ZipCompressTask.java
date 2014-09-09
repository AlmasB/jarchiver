package com.almasb.jarchiver.task;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import com.almasb.common.util.Out;
import com.almasb.java.io.ResourceManager;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public final class ZipCompressTask extends Task<Void> {

    private final File[] files;

    public ZipCompressTask(File... files) {
        this.files = files;
    }

    @Override
    protected Void call() throws Exception {
        long start = System.nanoTime();

        for (File file : files) {
            if (file.isDirectory()) {
                ArrayList<File> files = new ArrayList<File>();
                loadFileNames(file, files);

                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".jar");
                JarOutputStream jos = new JarOutputStream(fos);

                final SimpleIntegerProperty progress = new SimpleIntegerProperty(0);

                files.stream().forEach(aFile -> {
                    try {
                        String name = aFile.getAbsolutePath();
                        name = name.substring(name.indexOf(file.getName()));

                        updateMessage("Compressing: " + name);

                        JarEntry entry = new JarEntry(name);
                        jos.putNextEntry(entry);
                        jos.write(ResourceManager.loadResourceAsByteArray(aFile.getAbsolutePath()));
                        jos.closeEntry();

                        progress.set(progress.get() + 1);
                        updateProgress(progress.get(), files.size());
                    }
                    catch (Exception e) {
                        Out.i("Failed to compress " + aFile.getAbsolutePath());
                        Out.e("call()", "Failed to compress file", this, e);
                    }
                });

                jos.close();
                fos.close();
            }
            else {
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + ".jar");
                JarOutputStream jos = new JarOutputStream(fos);

                final SimpleIntegerProperty progress = new SimpleIntegerProperty(0);

                try {
                    JarEntry entry = new JarEntry(file.getName());
                    jos.putNextEntry(entry);
                    jos.write(ResourceManager.loadResourceAsByteArray(file.getAbsolutePath()));
                    jos.closeEntry();

                    progress.set(progress.get() + 1);
                    updateProgress(progress.get(), 1);
                }
                catch (Exception e) {
                    Out.i("Failed to compress " + file.getAbsolutePath());
                    Out.e("call()", "Failed to compress file", this, e);
                }

                jos.close();
                fos.close();
            }
        }

        updateMessage(String.format("Compression took: %.3f s", (System.nanoTime() - start) / 1000000000.0));
        //System.gc();

        return null;
    }

    private void loadFileNames(File folder, ArrayList<File> files) {
        File[] allFiles = folder.listFiles();
        if (allFiles == null) {
            return;
        }

        for (File file : allFiles) {
            if (file.isDirectory()) {
                loadFileNames(file, files);
            }
            else {
                //                String name = aFile.toString();
                //                if (name.contains(File.separator)) {
                //                    name = name.substring(name.lastIndexOf(File.separator) + 1);
                //                }
                files.add(file);
            }
        }
    }
}
