package com.almasb.jarchiver;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Popup extends Parent {

    private Text text = new Text();

    public Popup() {
        Rectangle background = new Rectangle(150, 50);
        background.setFill(Color.AQUA);
        background.setStroke(Color.BLUEVIOLET);
        background.setArcHeight(30);
        background.setArcWidth(30);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(background, text);
        getChildren().add(stack);
    }

    public void setMessage(String message) {
        text.setText(message);
    }
}
