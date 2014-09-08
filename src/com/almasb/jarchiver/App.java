package com.almasb.jarchiver;

import static com.almasb.jarchiver.Config.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.controlsfx.dialog.Dialogs;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import com.almasb.common.util.Out;
import com.almasb.jarchiver.task.ZipCompressTask;
import com.almasb.java.io.ByteWriter;
import com.almasb.java.ui.FXWindow;

public final class App extends FXWindow {

    private Optional<File> fileToCompress = Optional.empty(), fileToDecompress = Optional.empty();
    private CompressionService compressor = new CompressionService();
    private DecompressionService decompressor = new DecompressionService();

    private File[] filesToCompress;

    private CheckBox check = new CheckBox("Compress");

    private SimpleIntegerProperty xzPreset = new SimpleIntegerProperty(6);

    @Override
    protected void createContent(Pane root) {
        // from top to bottom

        // toolbar
        ToolBar toolbar = new ToolBar();

        Button btnAbout = new Button("About");
        btnAbout.setOnAction(event -> {
            Dialogs.create()
            .title("About")
            .message(APP_TITLE + " v" + APP_VERSION)
            .showInformation();
        });

        toolbar.getItems().add(btnAbout);

        // options horizontal bar
        HBox hboxOptions = new HBox(0);

        Text compressionMode = new Text("Compression: ");
        compressionMode.setFont(Font.font(compressionMode.getFont().getFamily(), 16));

        ToggleGroup toggleGroup = new ToggleGroup();

        ToggleButton zipToggle = new ToggleButton("ZIP");
        zipToggle.setToggleGroup(toggleGroup);

        ToggleButton xzToggle = new ToggleButton("XZ");
        xzToggle.setToggleGroup(toggleGroup);

        zipToggle.selectedProperty().addListener((obs, old, newValue) -> {
            if (newValue.booleanValue()) {
                zipToggle.setOpacity(1.0);
                xzToggle.setOpacity(0.5);
            }
        });

        xzToggle.selectedProperty().addListener((obs, old, newValue) -> {
            if (newValue.booleanValue()) {
                zipToggle.setOpacity(0.5);
                xzToggle.setOpacity(1.0);
            }
        });

        toggleGroup.selectToggle(zipToggle);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                toggleGroup.selectToggle(oldValue);
            }
        });

        Text xzPresetText = new Text();
        xzPresetText.setText(6 + " (" + "Medium" + ")");

        Slider xzPresetSlider = new Slider(0, 9, 6);
        xzPresetSlider.setSnapToTicks(true);
        xzPresetSlider.setMajorTickUnit(1);
        xzPresetSlider.disableProperty().bind(xzToggle.selectedProperty().not());
        xzPresetSlider.valueProperty().addListener((obs, old, newValue) -> {
            int preset = newValue.intValue();

            xzPreset.set(preset);
            String compression = "";
            if (preset >= 7) {
                compression = "High";
            }
            else if (preset <= 3) {
                compression = "Low";
            }
            else {
                compression = "Medium";
            }

            xzPresetText.setText(preset + " (" + compression + ")");
        });

        check.setSelected(true);
        check.selectedProperty().addListener((obs, old, newValue) -> {
            FadeTransition ft = new FadeTransition(Duration.seconds(1), hboxOptions);
            ft.setToValue(newValue.booleanValue() ? 1 : 0);
            ft.setOnFinished(event -> {
                hboxOptions.setDisable(!newValue.booleanValue());
            });
            ft.play();
        });
        //hboxOptions.visibleProperty().bind(check.selectedProperty());
        hboxOptions.setPrefWidth(APP_W * 0.75);
        hboxOptions.getChildren().addAll(compressionMode, zipToggle, xzToggle, xzPresetSlider, xzPresetText);

        HBox secondBar = new HBox(0);
        secondBar.getChildren().addAll(hboxOptions, check);

        // drag and drop area and filenames view
        ListView<String> list = new ListView<String>();
        list.setMaxHeight(APP_H * 0.6);

        list.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles())
                event.acceptTransferModes(TransferMode.COPY);
            else
                event.consume();
        });
        list.setOnDragDropped(event -> {
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

        // progress bar
        HBox progressHBox = new HBox(10);

        ProgressBar progressBar = new ProgressBar();
        // TODO: rebind to decompressor, do this somewhere else later
        progressBar.progressProperty().bind(compressor.progressProperty());
        progressBar.visibleProperty().bind(compressor.runningProperty());

        Text progressText = new Text();
        progressText.textProperty().bind(progressBar.progressProperty().multiply(100).asString("%.0f").concat("%"));
        progressText.visibleProperty().bind(progressBar.visibleProperty());

        progressHBox.getChildren().addAll(new Text("Progress: "), progressBar, progressText);

        // memory usage bar
        HBox memoryHBox = new HBox(10);

        ProgressBar memoryUsageBar = new ProgressBar();
        memoryUsageBar.setStyle("-fx-accent: rgb(0,255,25)");
        memoryUsageBar.progressProperty().bind(RuntimeProperties.usedMemoryProperty().divide(RuntimeProperties.totalJVMMemoryProperty()));

        Text memoryText = new Text();
        memoryText.textProperty().bind(RuntimeProperties.usedMemoryProperty().asString("%.0f")
                .concat(" / ").concat(RuntimeProperties.totalJVMMemoryProperty().asString("%.0f").concat(" MB")));

        memoryHBox.getChildren().addAll(new Text("Memory Usage: "), memoryUsageBar, memoryText);

        Text message = new Text();
        message.textProperty().bind(toggleGroup.selectedToggleProperty().asString());

        // VBox to contain all of the above in a vertical layout
        VBox vbox = new VBox(5);
        vbox.setPrefWidth(APP_W);
        vbox.getChildren().addAll(toolbar, secondBar, list, progressHBox, memoryHBox, message);
        root.getChildren().addAll(vbox);
    }

    @Override
    protected void initScene(Scene scene) {
    }

    @Override
    protected void initStage(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
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
            return new ZipCompressTask(filesToCompress);
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
