package com.sdy.fileSystem.controller;

import com.sdy.fileSystem.Test;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Index extends Application {

    public static DiskController disk;
    public static Stage blockUserStage;
    public static Stage fileManageStage;
    public final javafx.scene.image.Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../icon.png")));
    public final javafx.scene.image.Image file = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../file.png")));

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
        VBox root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../layout/index.fxml")));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 600, 400));
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
        Node imageView = new ImageView(icon);
        TreeItem<String> rootItem = new TreeItem<String>("/", imageView);
        disk.dfsPath("/", rootItem, icon);
        TreeView lookup = (TreeView) root.lookup("#treeView");
        System.out.println(lookup == null);
        lookup.setRoot(rootItem);
    }

    private class TextFieldTreeCellImpl extends TreeCell<String> {

        private TextField textField;
        private ContextMenu addMenu = new ContextMenu();

        public TextFieldTreeCellImpl() {
            MenuItem addMenuItem = new MenuItem("Add");
            addMenu.getItems().add(addMenuItem);
            addMenuItem.setOnAction((ActionEvent t) -> {
                TreeItem newEmployee =
                        new TreeItem<>("New");
                getTreeItem().getChildren().add(newEmployee);
            });
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(getTreeItem().getGraphic());
                    if (
                            !getTreeItem().isLeaf() && getTreeItem().getParent() != null
                    ) {
                        setContextMenu(addMenu);
                    }
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased((KeyEvent t) -> {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });

        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    public void formatBlock() {
        disk.getDiskService().getDisk().format();
    }
}
