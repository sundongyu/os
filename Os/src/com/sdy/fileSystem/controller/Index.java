package com.sdy.fileSystem.controller;

import com.sdy.fileSystem.Test;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Index extends Application {

    public static DiskController disk;
    public static Stage blockUserStage;
    public static Stage fileManageStage;
    public final Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resource/icon.png")));
    public final Image file = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resource/file.png")));
    public final Image block = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resource/block.png")));
    public Button iterm;

    public static void main(String[] args) {
        Test test = new Test(new DiskController());
        disk = test.disk;
        Controller.disk = test.disk;
        Thread thread = new Thread(test);
        thread.start();
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../layout/index.fxml")));
        primaryStage.setTitle("菜单");
        ImageView img = (ImageView) root.lookup("#background");
        img.setImage(new Image("file:/Volumes/data/Os/os/Os/src/com/sdy/fileSystem/resource/943753.png"));
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 打开位示图
    public void openBlockUserState() throws IOException {
        Stage primaryStage = new Stage();
        blockUserStage = primaryStage;
        GridPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../layout/blockUserState.fxml")));
        boolean[] state = disk.getDiskService().getDisk().getState();
        for (int i = 3; i <= 128; i++) {
            String name = "#state" + i;
            Circle lookup = (Circle) root.lookup(name);
            String colour = lookup.getFill().toString();
            if (colour.equals(Color.DODGERBLUE.toString()) && !state[i - 1] || colour.equals("#ff1f1f") && state[i - 1])
                continue;
            if (!state[i - 1]) lookup.setFill(Color.DODGERBLUE);
            else lookup.setFill(Color.valueOf("#ff1f1f"));
        }
        primaryStage.setTitle("磁盘块使用情况");
        primaryStage.setScene(new Scene(root, 368, 230));
        primaryStage.show();
    }

    // 退出系统
    public void exitSystem() {
        System.exit(0);
    }

    // 打开文件管理器
    public void openFileManage() throws IOException {
        fileManageStage = new Stage();
        fileManageStage.setResizable(false);
        VBox root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../layout/fileManage.fxml")));
        fileManageStage.setTitle("文件管理器");
        fileManageStage.setScene(new Scene(root, 900, 600));
        fileManageStage.show();
        Node imageView = new ImageView(block);
        TreeItem<String> rootItem = new TreeItem<String>("/", imageView);
        disk.dfsPath("/", rootItem, icon);
        TreeView lookup = (TreeView) root.lookup("#treeView");
        rootItem.setExpanded(true);
        lookup.setRoot(rootItem);
    }

    public void openIterm(ActionEvent actionEvent) throws IOException {
        Iterm.stage = new Stage();
        StackPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../layout/iterm.fxml")));
        Iterm.textArea = new TabAndEnterIgnoringTextArea();
        root.getChildren().add(Iterm.textArea);
        Iterm.textArea.setFont(Font.font(17));
        Iterm.textArea.setWrapText(true);
        Iterm.textArea.setText(Iterm.itermHead + Iterm.curPath + Iterm.itermTail);
        Iterm.stage.setScene(new Scene(root));
        Iterm.stage.show();
        Iterm.textArea.requestFocus();
        Iterm.textArea.positionCaret(Iterm.itermHead.length() + Iterm.curPath.length() + Iterm.itermTail.length());
    }

    public void formatBlock() {
        disk.getDiskService().getDisk().format();
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
