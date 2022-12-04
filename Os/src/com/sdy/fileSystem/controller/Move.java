package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

/**
 * @author 孙东宇
 * 创建时间：2022/11/16
 * 介绍：
 */
public class Move {

    @FXML
    public Text srcDir;

    @FXML
    public TextField tarDir;

    public static Stage stage;

    public static TreeView treeView;

    public void saveMove(ActionEvent actionEvent) throws IOException, InterruptedException {
        Move.stage.close();
        System.out.println(srcDir.getText().toString() + tarDir.getText().toString());
        Index.disk.move(srcDir.getText().toString(), tarDir.getText().toString());
        Index index = new Index();
        index.updataFileManage(Index.root);
        index.updateBlockUser();
    }

    public void onMouseClicked(MouseEvent event) {
        // 设置鼠标左键双击进行节点数据获取
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            // 路径
            StringBuilder sb = new StringBuilder();
            Node node = event.getPickResult().getIntersectedNode();
            TreeItem selectedItem = (TreeItem) treeView.getSelectionModel().getSelectedItem();
            if (node instanceof Text || (node instanceof TreeCell && ((TreeCell) node).getText() != null)) {
                StringBuilder name = new StringBuilder((String) selectedItem.getValue());
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
            if(path.equals("/")) tarDir.setText(FileManage.fileName);
            else tarDir.setText(path + FileManage.fileName);
        }

    }

    public void finishMove(ActionEvent actionEvent) {
        stage.close();
    }
}
