package com.almasb.jarchiver;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;

import com.almasb.jarchiver.task.AARCompressTask;
import com.almasb.jarchiver.task.AARDecompressTask;
import com.almasb.jarchiver.task.XZCompressTask;
import com.almasb.jarchiver.task.XZDecompressTask;
import com.almasb.jarchiver.task.ZipCompressTask;
import com.almasb.jarchiver.task.ZipDecompressTask;

public class Controller implements Initializable {

    @FXML
    private String sVersion;

    @FXML
    private Text textStatus;
    @FXML
    private Text textMessage;

    @FXML
    private ProgressBar progress;

    @FXML
    private ToggleGroup toggleGroupMode;

    /**
     * The whole application logic is handled via
     * this service
     */
    private CompressionService compressionService = new CompressionService();

    /**
     * Files to be compressed / decompressed
     */
    private ArrayList<Path> files = new ArrayList<Path>();

    /**
     * C - compression
     * DC - decompression
     */
    private enum Mode {
        ZIP_C, XZ_C, AAR_C, DC
    }

    /**
     * Current mode that sets application logic
     */
    private Mode mode = Mode.DC;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progress.progressProperty().bind(compressionService.progressProperty());
        progress.visibleProperty().bind(compressionService.runningProperty());

        textStatus.textProperty().bind(progress.progressProperty().multiply(100).asString("%.0f").concat("%"));
        textStatus.visibleProperty().bind(progress.visibleProperty());
        textMessage.textProperty().bind(compressionService.messageProperty());
    }

    @FXML
    private void onAbout() {
        Alert dialog = new Alert(AlertType.INFORMATION);
        dialog.setTitle("About JArchiver");
        dialog.setHeaderText("JArchiver by AlmasB" + " v" + sVersion);
        dialog.setContentText("XZ Java (http://tukaani.org/xz/java.html)\n"
                + "ControlsFX (http://fxexperience.com/controlsfx/)");
        dialog.show();
    }

    @FXML
    private void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles())
            event.acceptTransferModes(TransferMode.COPY);
        else
            event.consume();
    }

    @FXML
    private void onDragDropped(DragEvent event) {
        mode = Mode.valueOf((String) toggleGroupMode.getSelectedToggle().getProperties().get("mode"));

        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles() && !compressionService.isRunning()) {
            success = true;
            files.clear();
            files.addAll(db.getFiles().stream().map(file -> file.toPath()).collect(Collectors.toList()));
            compressionService.restart();
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private class CompressionService extends Service<Void> {
        /**
         * Default value for XZ compression preset
         */
        private int xzPreset = 6;

        @Override
        protected Task<Void> createTask() {
            switch (mode) {
                case DC:
                    return findDCTask();
                case XZ_C:
                    return new XZCompressTask(files.stream().filter(file -> Files.isRegularFile(file)).toArray(Path[]::new), xzPreset);
                case AAR_C:
                    return new AARCompressTask(files.stream().filter(file -> Files.isRegularFile(file)).toArray(Path[]::new));
                case ZIP_C:
                default:
                    return new ZipCompressTask(files.toArray(new Path[0]));
            }
        }

        private Task<Void> findDCTask() {
            if (files.size() > 0) {
                SimpleStringProperty ext = new SimpleStringProperty();

                String firstName = files.get(0).getFileName().toString();
                if (firstName.endsWith(".jar")) {
                    ext.set(".jar");
                }
                else if (firstName.endsWith(".xz")) {
                    ext.set(".xz");
                }
                else if (firstName.endsWith(".aar")) {
                    ext.set(".aar");
                }
                else {
                    ext.set("");
                }

                if (!ext.get().isEmpty()) {
                    Path[] filtered = files.stream().filter(file -> Files.isRegularFile(file) && file.getFileName().toString().endsWith(ext.get())).toArray(Path[]::new);
                    switch (ext.get()) {
                        case ".jar":
                            return new ZipDecompressTask(filtered);
                        case ".xz":
                            return new XZDecompressTask(filtered);
                        case ".aar":
                            return new AARDecompressTask(filtered);
                        default:
                            break;
                    }
                }
            }

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
