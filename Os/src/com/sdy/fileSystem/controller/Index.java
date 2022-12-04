package com.sdy.fileSystem.controller;

import com.sdy.fileSystem.Test;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Index extends Application {

    public static boolean showAllToFileManage = false;
    public static DiskController disk;
    public static Stage blockUserStage;
    public static Stage fileManageStage;
    public static VBox root;
    public static GridPane gridRoot;
    public final Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/sdy/fileSystem/resource/icon.png")));
    public final Image file = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/sdy/fileSystem/resource/file.png")));
    public final Image block = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/sdy/fileSystem/resource/block.png")));
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
        AnchorPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/sdy/fileSystem/layout/index.fxml")));
        primaryStage.setTitle("菜单");
        ImageView img = (ImageView) root.lookup("#background");
        img.setImage(new Image("/com/sdy/fileSystem/resource/943753.png"));
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 打开位示图
    public void openBlockUserState() throws IOException {
        gridRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/sdy/fileSystem/layout/blockUserState.fxml")));
        Stage primaryStage = new Stage();
        blockUserStage = primaryStage;
        updateBlockUser();
        primaryStage.setTitle("磁盘块使用情况");
        primaryStage.setScene(new Scene(gridRoot, 368, 230));
        primaryStage.show();
    }

    public void updateBlockUser() throws IOException {
        if (gridRoot == null) return;
        boolean[] state = disk.getDiskService().getDisk().getState();
        for (int i = 3; i <= 128; i++) {
            String name = "#state" + i;
            Circle lookup = (Circle) gridRoot.lookup(name);
            String colour = lookup.getFill().toString();
            if (colour.equals(Color.DODGERBLUE.toString()) && !state[i - 1] || colour.equals("#ff1f1f") && state[i - 1])
                continue;
            if (!state[i - 1]) lookup.setFill(Color.DODGERBLUE);
            else lookup.setFill(Color.valueOf("#ff1f1f"));
        }
    }

    // 退出系统
    public void exitSystem() {
        System.exit(0);
    }

    // 打开文件管理器
    public void openFileManage() throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/sdy/fileSystem/layout/fileManage.fxml")));
        fileManageStage = new Stage();
        fileManageStage.setResizable(false);
        fileManageStage.setTitle("文件管理器");
        fileManageStage.setScene(new Scene(root, 900, 600));
        fileManageStage.show();
        MenuBar lookup = (MenuBar) root.lookup("#menu");
        ObservableList<Menu> menus = lookup.getMenus();
        Menu menu = menus.get(2);
        ObservableList<MenuItem> items = menu.getItems();
        CheckMenuItem checkMenuItem = (CheckMenuItem) items.get(0);
        checkMenuItem.setSelected(Index.showAllToFileManage);
        updataFileManage(root);
    }

    public void updataFileManage(VBox root) throws IOException {
        if (root == null) return;
        Node imageView = new ImageView(block);
        TreeItem<String> rootItem = new TreeItem<String>("/", imageView);
        disk.dfsPath("/", rootItem, icon);
        TreeView lookup = (TreeView) root.lookup("#treeView");
        rootItem.setExpanded(true);
        lookup.setRoot(rootItem);
    }

    public void openIterm(ActionEvent actionEvent) throws IOException {
        Iterm.stage = new Stage();
        StackPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/sdy/fileSystem/layout/iterm.fxml")));
        Iterm.textArea = new TabAndEnterIgnoringTextArea();
        root.getChildren().add(Iterm.textArea);
        Iterm.textArea.setFont(Font.font(17));
        Iterm.textArea.setWrapText(true);
        Iterm.textArea.setText(Iterm.itermHead + Iterm.curPath + Iterm.itermTail);
        Iterm.stage.setScene(new Scene(root));
        Iterm.curPath = "/";
        Iterm.dataArr[0] = "";
        Iterm.stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {

            // 监听键盘功能键，control 和 alt 负责向上向下切换历史命令
            if (e.getCode() == KeyCode.CONTROL) {
                Iterm.lastCommand();
            } else if (e.getCode() == KeyCode.ALT) {
                Iterm.nextCommand();
            } else if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.BACK_SPACE) {
                Iterm.tempIdx = Iterm.idx;
            }
        });
        Iterm.stage.show();
        Iterm.textArea.requestFocus();
        Iterm.textArea.positionCaret(Iterm.itermHead.length() + Iterm.curPath.length() + Iterm.itermTail.length());
    }

    public void formatBlock() throws IOException {
        disk.getDiskService().getDisk().format();
        updateBlockUser();
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
