package com.sdy.fileSystem.controller;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TextAreaTabAndEnterHandler extends Application {
    final Label status = new Label();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        final TextArea textArea1 = new TabAndEnterIgnoringTextArea();

        final Button defaultButton = new Button("OK");
        defaultButton.setDefaultButton(true);
        defaultButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                status.setText("Default Button Pressed");
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 10px;");
        layout.getChildren().setAll(
                textArea1,
                defaultButton,
                status
        );

        stage.setScene(
                new Scene(layout)
        );
        stage.show();
    }


    class TabAndEnterIgnoringTextArea extends TextArea {
        final TextArea myTextArea = this;

        TabAndEnterIgnoringTextArea() {
            addEventFilter(KeyEvent.KEY_PRESSED, new TabAndEnterHandler());
        }

        class TabAndEnterHandler implements EventHandler<KeyEvent> {
            private KeyEvent recodedEvent;

            @Override
            public void handle(KeyEvent event) {
                if (recodedEvent != null) {
                    recodedEvent = null;
                    return;
                }

                Parent parent = myTextArea.getParent();
                if (parent != null) {
                    switch (event.getCode()) {
                        case ENTER:
                            if (event.isControlDown()) {
                                recodedEvent = recodeWithoutControlDown(event);
                                myTextArea.fireEvent(recodedEvent);
                            } else {
                                Event parentEvent = event.copyFor(parent, parent);
                                myTextArea.getParent().fireEvent(parentEvent);
                            }
                            event.consume();
                            break;
                    }
                }
            }

            private KeyEvent recodeWithoutControlDown(KeyEvent event) {
                return new KeyEvent(
                        event.getEventType(),
                        event.getCharacter(),
                        event.getText(),
                        event.getCode(),
                        event.isShiftDown(),
                        false,
                        event.isAltDown(),
                        event.isMetaDown()
                );
            }
        }
    }
}