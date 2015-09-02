package com.almasb.jarchiver;

import java.io.File;

import com.almasb.jarchiver.task.UnZipTask;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainController {

    public void unZip() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("ZIP file", "*.zip"));
        fileChooser.setTitle("Select ZIP file");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Task<Void> task = new UnZipTask(file.toPath());

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

            TaskDialog dialog = new TaskDialog(task);
            dialog.showAndWait();
        }
    }

    public void about() {

    }

    public void close() {
        Platform.exit();
    }
}
