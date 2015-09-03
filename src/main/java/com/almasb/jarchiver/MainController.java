package com.almasb.jarchiver;

import java.io.File;

import com.almasb.jarchiver.task.UnZipTask;
import com.almasb.jarchiver.task.ZipTask;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainController {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label labelMessage;

    public void unZip() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(new ExtensionFilter("ZIP file", "*.zip"));
//        fileChooser.setTitle("Select ZIP file");
//        File file = fileChooser.showOpenDialog(null);
//        if (file != null) {
//            Task<Void> task = new UnZipTask(file.toPath());
//
//            Thread thread = new Thread(task);
//            thread.setDaemon(true);
//            thread.start();
//
//            TaskDialog dialog = new TaskDialog(task);
//            dialog.showAndWait();
//        }
    }

    public void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles())
            event.acceptTransferModes(TransferMode.COPY);
        else
            event.consume();
    }

    public void onDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;

            Task<?> task;

            File file = db.getFiles().get(0);

            if (file.getName().endsWith(".zip")) {
                task = new UnZipTask(file.toPath());
            }
            else {
                task = new ZipTask(db.getFiles());
            }

            labelMessage.textProperty().bind(task.messageProperty());
            progressBar.progressProperty().bind(task.progressProperty());

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
        event.setDropCompleted(success);
        event.consume();
    }

    public void about() {

    }

    public void close() {
        Platform.exit();
    }
}
