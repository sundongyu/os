package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author 孙东宇
 * 创建时间：2022/11/15
 * 介绍：
 */
public class NewFile {

    @FXML
    public static Stage stage;
    public TextField newFileName;
    public static String name;
    public void saveNewFile(ActionEvent actionEvent) throws IOException {
        CharSequence characters = newFileName.getCharacters();
        name = FileManage.PATH;
        name += characters.toString();
        Index.disk.create(name);
        stage.close();
        Index index = new Index();
        Index.fileManageStage.close();
        index.openFileManage();
    }

    public void returnNewFile(ActionEvent actionEvent) {
        stage.close();
    }
}
