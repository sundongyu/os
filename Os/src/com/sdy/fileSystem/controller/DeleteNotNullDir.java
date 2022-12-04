package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author 孙东宇
 * 创建时间：2022/11/21
 * 介绍：
 */
public class DeleteNotNullDir {

    public static Stage stage;

    @FXML
    public Button noButton;
    @FXML
    public Button yesButton;

    public void onActionNoButton(ActionEvent actionEvent) {
        stage.close();
    }

    public void onActionYesButton(ActionEvent actionEvent) throws IOException {
        stage.close();
        Index.disk.getDiskService().dfsDel(FileManage.PATH);
        Index index = new Index();
        index.updataFileManage(Index.root);
        index.updateBlockUser();
    }
}
