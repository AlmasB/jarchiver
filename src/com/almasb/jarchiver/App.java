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
package com.almasb.jarchiver;

import static com.almasb.jarchiver.Config.APP_H;
import static com.almasb.jarchiver.Config.APP_TITLE;
import static com.almasb.jarchiver.Config.APP_VERSION;
import static com.almasb.jarchiver.Config.APP_W;

import java.io.File;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
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
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.controlsfx.dialog.Dialogs;

import com.almasb.jarchiver.task.AARCompressTask;
import com.almasb.jarchiver.task.AARDecompressTask;
import com.almasb.jarchiver.task.XZCompressTask;
import com.almasb.jarchiver.task.XZDecompressTask;
import com.almasb.jarchiver.task.ZipCompressTask;
import com.almasb.jarchiver.task.ZipDecompressTask;
import com.almasb.java.ui.FXWindow;
import com.almasb.java.ui.fx.MemoryUsageBar;

/**
 * Application UI and its interaction with the logic layer
 *
 * @author Almas Baimagambetov (ab607@uni.brighton.ac.uk)
 * @version 1.0
 *
 */
public final class App extends FXWindow {

    private CompressionService compressionService = new CompressionService();

    /**
     * Files to be compressed / decompressed
     * initial array of size 1 allows to avoid NPE and IOB
     *
     * Do check the first element however
     *
     * TODO: replace with Optional
     */
    private File[] files = new File[1];

    private SimpleIntegerProperty xzPreset = new SimpleIntegerProperty(6);

    private enum Mode {
        ZIP_C, XZ_C, AAR_C, DC
    }

    private Mode mode = Mode.ZIP_C;

    @Override
    protected void createContent(Pane root) {
        // from top to bottom
        ToolBar toolBar = createToolbar();
        HBox optionsBar = createOptionsBar();
        Parent dragDropArea = createDragDropArea();
        HBox progressBar = createProgressBar();
        MemoryUsageBar memoryBar = new MemoryUsageBar();

        // task messages
        Text message = new Text();
        message.textProperty().bind(compressionService.messageProperty());

        // VBox to contain all of the above in a vertical layout
        VBox vbox = new VBox(5);
        vbox.setPrefWidth(APP_W);
        vbox.getChildren().addAll(toolBar, optionsBar, dragDropArea, progressBar, memoryBar, message);
        root.getChildren().addAll(vbox);
    }

    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();

        Button btnAbout = new Button("About");
        btnAbout.setOnAction(event -> {
            Dialogs.create()
            .title("About")
            .message(APP_TITLE + " v" + APP_VERSION)
            .showInformation();
        });

        Button btnHelp = new Button("Help");
        btnHelp.setOnAction(event -> {
            Dialogs.create()
            .title("Help")
            .message("Currently there are 3 compression modes: ZIP / XZ / AAR\n"
                    + "ZIP produces a .jar file with ZIP compression\n"
                    + "XZ produces a .xz file with LZMA2 compression\n"
                    + "AAR produces a .arr file with custom DEFLATE compression\n"
                    + "Note: XZ only works with a single file but provides greater compression ratio\n"
                    + "Note: AAR only works with a single file but provides faster compression times\n"
                    + "Do not use files over 2 GB")
                    .showInformation();
        });

        toolbar.getItems().addAll(btnHelp, btnAbout);
        return toolbar;
    }

    private HBox createOptionsBar() {
        Text compressionMode = new Text("Mode: ");
        compressionMode.setFont(Font.font(compressionMode.getFont().getFamily(), 16));

        ToggleGroup toggleGroup = new ToggleGroup();

        ToggleButton toggleZIP = new ToggleButton("ZIP");
        toggleZIP.setToggleGroup(toggleGroup);

        ToggleButton toggleXZ = new ToggleButton("XZ");
        toggleXZ.setToggleGroup(toggleGroup);

        ToggleButton toggleAAR = new ToggleButton("AAR");
        toggleAAR.setToggleGroup(toggleGroup);

        toggleZIP.selectedProperty().addListener((obs, old, newValue) -> {
            if (newValue.booleanValue()) {
                toggleZIP.setOpacity(1.0);
                toggleXZ.setOpacity(0.5);
                toggleAAR.setOpacity(0.5);
                mode = Mode.ZIP_C;
            }
        });

        toggleXZ.selectedProperty().addListener((obs, old, newValue) -> {
            if (newValue.booleanValue()) {
                toggleZIP.setOpacity(0.5);
                toggleXZ.setOpacity(1.0);
                toggleAAR.setOpacity(0.5);
                mode = Mode.XZ_C;
            }
        });

        toggleAAR.selectedProperty().addListener((obs, old, newValue) -> {
            if (newValue.booleanValue()) {
                toggleZIP.setOpacity(0.5);
                toggleXZ.setOpacity(0.5);
                toggleAAR.setOpacity(1.0);
                mode = Mode.AAR_C;
            }
        });

        toggleGroup.selectToggle(toggleZIP);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                toggleGroup.selectToggle(oldValue);
            }
        });

        Text xzPresetText = new Text();
        xzPresetText.setText(xzPreset.get() + " (" + "Medium" + ")");

        Slider xzPresetSlider = new Slider(0, 9, 6);
        xzPresetSlider.setSnapToTicks(true);
        xzPresetSlider.setMajorTickUnit(1);
        xzPresetSlider.disableProperty().bind(toggleXZ.selectedProperty().not());
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

        HBox leftHBox = new HBox(0);
        leftHBox.setPrefWidth(APP_W * 0.8);
        leftHBox.getChildren().addAll(compressionMode, toggleZIP, toggleXZ, toggleAAR, xzPresetSlider, xzPresetText);

        CheckBox check = new CheckBox("Compress");
        check.setSelected(true);
        check.selectedProperty().addListener((obs, old, newValue) -> {
            if (newValue.booleanValue()) {
                ToggleButton btn = (ToggleButton) toggleGroup.getSelectedToggle();
                switch (btn.getText()) {
                    case "ZIP":
                        mode = Mode.ZIP_C;
                        break;
                    case "XZ":
                        mode = Mode.XZ_C;
                        break;
                    case "AAR":
                        mode = Mode.AAR_C;
                        break;
                }
            }
            else
                mode = Mode.DC;

            FadeTransition ft = new FadeTransition(Duration.seconds(1), leftHBox);
            ft.setToValue(newValue.booleanValue() ? 1 : 0);
            ft.setOnFinished(event -> {
                leftHBox.setDisable(!newValue.booleanValue());
            });
            ft.play();
        });

        HBox optionsHBox = new HBox(0);
        optionsHBox.getChildren().addAll(leftHBox, check);
        return optionsHBox;
    }

    private Parent createDragDropArea() {
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
                files = db.getFiles().toArray(new File[0]);
                compressionService.restart();
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return list;
    }

    private HBox createProgressBar() {
        HBox progressHBox = new HBox(10);

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(compressionService.progressProperty());
        progressBar.visibleProperty().bind(compressionService.runningProperty());

        Text progressText = new Text();
        progressText.textProperty().bind(progressBar.progressProperty().multiply(100).asString("%.0f").concat("%"));
        progressText.visibleProperty().bind(progressBar.visibleProperty());

        progressHBox.getChildren().addAll(new Text("Progress: "), progressBar, progressText);
        return progressHBox;
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

        Popup pop = new Popup();

        pop.setX(primaryStage.getX() + APP_W / 2 - 75);
        pop.setY(primaryStage.getY() + APP_H / 2 - 50);

        Rectangle r = new Rectangle(150, 50);
        r.setFill(Color.AQUA);
        r.setStroke(Color.BLUEVIOLET);
        r.setArcHeight(30);
        r.setArcWidth(30);

        StackPane stack = new StackPane();
        Text msg = new Text("Drag and drop files/folders");
        stack.getChildren().addAll(r, msg);
        stack.setOpacity(0);

        pop.getContent().addAll(stack);
        pop.show(primaryStage);

        FadeTransition helpFT = new FadeTransition(Duration.seconds(1.5), stack);
        helpFT.setToValue(1);
        helpFT.setAutoReverse(true);
        helpFT.setCycleCount(2);
        helpFT.setOnFinished(event -> {
            pop.hide();
        });

        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), stack);
        ft.setToValue(1);
        ft.setAutoReverse(true);
        ft.setCycleCount(2);
        ft.setOnFinished(event -> {
            pop.setX(primaryStage.getX() + 90);
            pop.setY(primaryStage.getY() + 20);
            msg.setText("Click help for more info");
            helpFT.play();
        });
        ft.play();
    }

    private class CompressionService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            switch (mode) {
                case DC:
                    return findDCTask();
                case XZ_C:
                    return new XZCompressTask(files[0], xzPreset.get());
                case AAR_C:
                    return new AARCompressTask(files[0]);
                case ZIP_C:
                default:
                    return new ZipCompressTask(files);
            }
        }

        // TODO: either check extensions for ALL files here or do something about it
        private Task<Void> findDCTask() {
            File file = files[0];
            if (file == null || !file.isFile())
                return new DummyTask();

            if (file.getName().endsWith(".jar"))
                return new ZipDecompressTask(files);
            if (file.getName().endsWith(".xz"))
                return new XZDecompressTask(file);
            if (file.getName().endsWith(".aar"))
                return new AARDecompressTask(file);

            return new DummyTask();
        }
    }

    private class DummyTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            updateMessage("Wrong file extension");
            return null;
        }
    }
}
