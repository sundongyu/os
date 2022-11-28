package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 孙东宇
 * 创建时间：2022/11/10
 * 介绍：
 */
public class FileManage {

    public static String PATH = "/";
    public static String fileName;
    public final javafx.scene.image.Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resource/icon.png")));
    public final javafx.scene.image.Image file = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resource/file.png")));
    public final javafx.scene.image.Image block = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resource/block.png")));
    @FXML
    public TreeView treeView;
    @FXML
    public TextArea textArea;
    @FXML
    public TextArea dataProperty;
    public MenuItem newFile;
    public MenuItem saveFile;
    public MenuItem deleteFile;
    public MenuItem moveFile;
    public Text curPath;

    public void onMouseClicked(MouseEvent event) {
        // 设置鼠标左键双击进行节点数据获取
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            // 路径
            StringBuilder sb = new StringBuilder();
            Node node = event.getPickResult().getIntersectedNode();
            TreeItem selectedItem = (TreeItem) treeView.getSelectionModel().getSelectedItem();
            if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
                StringBuilder name = new StringBuilder((String) selectedItem.getValue());
                fileName = "/" + name.toString();
                sb.append(name.reverse());
            }
            TreeItem parent = selectedItem.getParent();
            while (parent != null && !"".equals((String) parent.getValue())) {
                StringBuilder value = new StringBuilder((String) parent.getValue());
                if (!"/".equals(value.reverse().toString())) sb.append("/");
                sb.append(value);
                parent = parent.getParent();
            }
            String path = sb.reverse().toString();
            dataProperty.setText(Index.disk.getFileProperty(path));
            if (Index.disk.getDiskService().isFile(path)) textArea.setText(Index.disk.type(path));
            else textArea.setText("");
            PATH = path;
            // 输出当前路径：上一层目录，无论当前点击的是目录还是文件
            if (PATH.equals("/")) {
                curPath.setText("/");
            } else if (PATH.lastIndexOf("/") == PATH.indexOf("/")) {
                curPath.setText("/");
            } else {
                curPath.setText(PATH.substring(0, PATH.lastIndexOf("/")));
            }
            // 清空字符串，避免重复添加路径
            sb = null;
        }

    }

    public void newFile(ActionEvent actionEvent) throws IOException {
        NewFile.stage = new Stage();
        NewFile.stage.setTitle(PATH);
        NewFile.stage.initModality(Modality.APPLICATION_MODAL);
        AnchorPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../layout/newFile.fxml")));
        NewFile.stage.setScene(new Scene(root));
        NewFile.stage.show();
    }

    public void saveFile(ActionEvent actionEvent) {
        String text = textArea.getText();
        Index.disk.edit(PATH, text);
        int len = text.length();
        dataProperty.setText(Index.disk.getFileProperty(PATH));
    }

    public void deleteNotNullDir() throws IOException {
        DeleteNotNullDir.stage = new Stage();
        DeleteNotNullDir.stage.setResizable(false);
        DeleteNotNullDir.stage.setTitle("提示");
        DeleteNotNullDir.stage.initStyle(StageStyle.UNIFIED);
        AnchorPane load = FXMLLoader.load(getClass().getResource("../layout/deleteNotNullDir.fxml"));
        DeleteNotNullDir.stage.setScene(new Scene(load));
        DeleteNotNullDir.stage.show();
    }

    public void deleteFile() throws IOException {
        // 判断是否是非空目录
        if (Index.disk.getDiskService().isDir(PATH)) {
            int[] fileBlock = Index.disk.getDiskService().getDisk().getFileBlock(PATH);
            List<String> list = new ArrayList<>();
            Index.disk.getDiskService().getDisk().getList(list, fileBlock[1]);
            if (list.size() != 0) {
                deleteNotNullDir();
            }
        } else {
            Index.disk.delete(PATH);
            Index index = new Index();
            Index.fileManageStage.close();
            index.openFileManage();
        }
    }

    public void moveFile(ActionEvent actionEvent) throws IOException, InterruptedException {
        Move.stage = new Stage();
        Move.stage.setTitle("移动文件");
        VBox root = FXMLLoader.load(getClass().getResource("../layout/move.fxml"));
        Move.stage.setScene(new Scene(root));
        Node imageView = new ImageView(block);
        TreeItem<String> rootItem = new TreeItem<String>("/", imageView);
        Index.disk.dfsDir("/", rootItem, icon);
        TreeView lookup = (TreeView) root.lookup("#treeView");
        Move.treeView = lookup;
        lookup.setRoot(rootItem);
        Text src = (Text) root.lookup("#srcDir");
        src.setText(PATH);
        Move.stage.show();
    }

    public void copyFile(ActionEvent actionEvent) throws IOException {
        Copy.stage = new Stage();
        Copy.stage.setTitle("拷贝文件");
        VBox root = FXMLLoader.load(getClass().getResource("../layout/copy.fxml"));
        Copy.stage.setScene(new Scene(root));
        Node imageView = new ImageView(block);
        TreeItem<String> rootItem = new TreeItem<String>("/", imageView);
        Index.disk.dfsDir("/", rootItem, icon);
        TreeView lookup = (TreeView) root.lookup("#treeView");
        Copy.treeView = lookup;
        lookup.setRoot(rootItem);
        Text src = (Text) root.lookup("#srcDir");
        src.setText(PATH);
        Copy.stage.show();
    }
}
