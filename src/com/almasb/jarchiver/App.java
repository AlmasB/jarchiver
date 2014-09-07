package com.almasb.jarchiver;

import static com.almasb.jarchiver.Config.APP_H;
import static com.almasb.jarchiver.Config.APP_TITLE;
import static com.almasb.jarchiver.Config.APP_W;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import com.almasb.common.util.Out;
import com.almasb.java.io.ByteWriter;
import com.almasb.java.ui.FXWindow;

public final class App extends FXWindow {

    // possibly queue and later to create 1 compressed file out of all files
    private Optional<File> fileToCompress = Optional.empty(), fileToDecompress = Optional.empty();
    private CompressionService compressor = new CompressionService();
    private DecompressionService decompressor = new DecompressionService();

    private File[] filesToCompress;

    private CheckBox check = new CheckBox("Decompression");

    @Override
    protected void createContent(Pane root) {
        Rectangle rect = new Rectangle(APP_W - 7, APP_H - 30);
        rect.setArcWidth(50);
        rect.setArcHeight(50);
        rect.setFill(null);
        rect.setStroke(Color.BLACK);

        Text message = new Text("Drag'n'drop a file for compression/decompression");

        ProgressIndicator progress = new ProgressIndicator();
        progress.visibleProperty().bind(compressor.runningProperty());
        progress.progressProperty().bind(compressor.progressProperty());

        StackPane stack = new StackPane();
        stack.getChildren().addAll(rect, message, progress);
        root.getChildren().addAll(stack, check);
    }

    @Override
    protected void initScene(Scene scene) {
        scene.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            else {
                event.consume();
            }
        });
        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;

                filesToCompress = db.getFiles().toArray(new File[0]);
                compressor.restart();

                /*String filePath = null;
                for (File file : db.getFiles()) {
                    filePath = file.getAbsolutePath();
                    Out.println(filePath + " length: " + file.length());

                    if (!check.isSelected()) {
                        fileToCompress = Optional.of(file);
                        compressor.restart();
                    }
                    else {
                        fileToDecompress = Optional.of(file);
                        decompressor.restart();
                    }
                }*/
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @Override
    protected void initStage(Stage primaryStage) {
        primaryStage.setWidth(APP_W);
        primaryStage.setHeight(APP_H);
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private class CompressionService extends Service<Void> {
        private LZMA2Options options = new LZMA2Options();
        //options.setPreset(9);

        @Override
        protected Task<Void> createTask() {
            return new ZipTask(filesToCompress);
            /*return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    fileToCompress.ifPresent(file -> {
                        try (FileInputStream fis = new FileInputStream(file);
                                FileOutputStream fos = new FileOutputStream(
                                        file.getAbsolutePath().toString().concat(".ja"));
                                XZOutputStream out = new XZOutputStream(fos, options)) {

                            byte[] data = ByteWriter.getBytes(fis);
                            int fileInSize = data.length;
                            int processedSize = 0;

                            byte[] buffer = new byte[8192];
                            int readBytes;

                            int iterations = fileInSize / 8192;
                            int bytesLeft = fileInSize % 8192;

                            for (int i = 0; i < iterations; i++) {
                                buffer = Arrays.copyOfRange(data, i*8192, i*8192 + 8192);
                                out.write(buffer);
                                processedSize += 8192;
                                updateProgress(processedSize, fileInSize);
                            }

                            buffer = Arrays.copyOfRange(data, iterations*8192, iterations*8192 + bytesLeft);
                            out.write(buffer);

                            //                            while ((readBytes = fis.read(buf)) != -1) {
                            //                                out.write(buf, 0, readBytes);
                            //                                processedSize += readBytes;
                            //                                updateProgress(processedSize, fileInSize);
                            //                            }
                        }
                        catch (Exception e) {
                            Out.i("Compression failed");
                            Out.e(e);
                        }
                    });

                    fileToCompress = Optional.empty();

                    return null;
                }
            };*/
        }
    }

    private class DecompressionService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    fileToDecompress.ifPresent(file -> {
                        try (FileInputStream fis = new FileInputStream(file);
                                FileOutputStream fos = new FileOutputStream(
                                        file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-3));
                                XZInputStream in = new XZInputStream(fis)) {

                            byte[] buf = new byte[8192];
                            int size;
                            while ((size = in.read(buf)) != -1)
                                fos.write(buf, 0, size);
                        }
                        catch (Exception e) {
                            Out.i("Failed to decompress");
                            Out.e(e);
                        }
                    });

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
            };
        }
    }
}
