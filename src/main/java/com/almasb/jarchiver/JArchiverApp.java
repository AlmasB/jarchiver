package com.almasb.jarchiver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class JArchiverApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.getIcons().add(new Image(JArchiverApp.class.getResourceAsStream("/icon.png")));
        stage.setScene(new Scene(FXMLLoader.load(JArchiverApp.class.getResource("/ui_main.fxml"))));
        stage.show();
    }
}
