package com.almasb.jarchiver;

import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressBar;

public class TaskDialog {

    private Dialog<ButtonType> dialog = new Dialog<>();
    private final Task<?> task;

    public TaskDialog(Task<?> task) {
        this.task = task;
        task.setOnSucceeded(event -> dialog.close());

        ProgressBar progress = new ProgressBar();
        progress.setPrefWidth(250);
        progress.progressProperty().bind(task.progressProperty());

        dialog.titleProperty().bind(task.titleProperty());
        dialog.headerTextProperty().bind(task.messageProperty());
        dialog.getDialogPane().setContent(progress);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }

    public void showAndWait() {
        dialog.showAndWait().ifPresent(button -> {
            if (button == ButtonType.CANCEL) {
                task.cancel(true);
            }
        });
    }
}
