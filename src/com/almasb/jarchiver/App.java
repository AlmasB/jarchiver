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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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

public final class App extends FXWindow {

    private CompressionService compressionService = new CompressionService();

    private File[] files;
    private File file;

    private CheckBox check = new CheckBox("Compress");

    private SimpleIntegerProperty xzPreset = new SimpleIntegerProperty(6);

    private enum Mode {
        ZIP_C, ZIP_DC, XZ_C, XZ_DC, AAR_C, AAR_DC
    }

    private Mode mode = Mode.ZIP_C;

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

        // options horizontal bar
        HBox hboxOptions = new HBox(0);

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
            }
        });

        toggleXZ.selectedProperty().addListener((obs, old, newValue) -> {
            if (newValue.booleanValue()) {
                toggleZIP.setOpacity(0.5);
                toggleXZ.setOpacity(1.0);
                toggleAAR.setOpacity(0.5);
            }
        });

        toggleAAR.selectedProperty().addListener((obs, old, newValue) -> {
            if (newValue.booleanValue()) {
                toggleZIP.setOpacity(0.5);
                toggleXZ.setOpacity(0.5);
                toggleAAR.setOpacity(1.0);
            }
        });

        toggleGroup.selectToggle(toggleZIP);
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

        check.setSelected(true);
        check.selectedProperty().addListener((obs, old, newValue) -> {
            FadeTransition ft = new FadeTransition(Duration.seconds(1), hboxOptions);
            ft.setToValue(newValue.booleanValue() ? 1 : 0);
            ft.setOnFinished(event -> {
                hboxOptions.setDisable(!newValue.booleanValue());
            });
            ft.play();
        });

        hboxOptions.setPrefWidth(APP_W * 0.8);
        hboxOptions.getChildren().addAll(compressionMode, toggleZIP, toggleXZ, toggleAAR, xzPresetSlider, xzPresetText);

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

                if (check.isSelected()) {
                    if (toggleZIP.isSelected()) {
                        files = db.getFiles().toArray(new File[0]);
                        mode = Mode.ZIP_C;
                    }
                    else if (toggleXZ.isSelected()) {
                        file = db.getFiles().get(0);
                        mode = Mode.XZ_C;
                    }
                    else if (toggleAAR.isSelected()) {
                        file = db.getFiles().get(0);
                        mode = Mode.AAR_C;
                    }
                }
                else {
                    File first = db.getFiles().get(0);

                    // check extension of the first file
                    if (first.getName().endsWith(".jar")) {
                        files = db.getFiles().toArray(new File[0]);
                        mode = Mode.ZIP_DC;
                    }
                    else if (first.getName().endsWith(".xz")) {
                        file = first;
                        mode = Mode.XZ_DC;
                    }
                    else if (first.getName().endsWith(".aar")) {
                        file = first;
                        mode = Mode.AAR_DC;
                    }
                }

                compressionService.restart();
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // progress bar
        HBox progressHBox = new HBox(10);

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(compressionService.progressProperty());
        progressBar.visibleProperty().bind(compressionService.runningProperty());

        Text progressText = new Text();
        progressText.textProperty().bind(progressBar.progressProperty().multiply(100).asString("%.0f").concat("%"));
        progressText.visibleProperty().bind(progressBar.visibleProperty());

        progressHBox.getChildren().addAll(new Text("Progress: "), progressBar, progressText);

        // memory usage bar
        MemoryUsageBar memoryBar = new MemoryUsageBar();

        // task messages
        Text message = new Text();
        message.textProperty().bind(compressionService.messageProperty());

        // VBox to contain all of the above in a vertical layout
        VBox vbox = new VBox(5);
        vbox.setPrefWidth(APP_W);
        vbox.getChildren().addAll(toolbar, secondBar, list, progressHBox, memoryBar, message);
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

        //        Notifications.create().text("Drag and drop files/folders")
        //        .owner(primaryStage)
        //        .position(Pos.CENTER)
        //        .hideAfter(Duration.seconds(2))
        //        .hideCloseButton()
        //        .showInformation();


        /*        Popup pop = new Popup();

        pop.setX(primaryStage.getX() + APP_W / 2 - 75);
        pop.setY(primaryStage.getY() + APP_H / 2 - 50);

        Rectangle r = new Rectangle(150, 100);
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
            pop.setX(primaryStage.getX());
            pop.setY(primaryStage.getY() + 50);
            msg.setText("Click help for more info");
            helpFT.play();
        });
        ft.play();*/
    }

    private class CompressionService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            switch (mode) {
                case XZ_C:
                    return new XZCompressTask(file, xzPreset.get());
                case XZ_DC:
                    return new XZDecompressTask(file);
                case AAR_C:
                    return new AARCompressTask(file);
                case AAR_DC:
                    return new AARDecompressTask(file);
                case ZIP_DC:
                    return new ZipDecompressTask(files);
                case ZIP_C:
                default:
                    return new ZipCompressTask(files);
            }
        }
    }
}
