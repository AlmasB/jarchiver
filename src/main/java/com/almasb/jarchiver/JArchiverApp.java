package com.almasb.jarchiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

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
        stage.setTitle("JArchiver");
        stage.show();
    }

    public static void main(String[] args) {
        Configurator.initialize("default", JArchiverApp.class.getResource("/log4j2.xml").toExternalForm());

        LogManager.getLogger("JArchiverApp").entry(args);

        launch(args);
    }
}
