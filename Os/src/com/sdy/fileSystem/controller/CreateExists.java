package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.stage.Stage;


/**
 * @author 孙东宇
 * 创建时间：2022/12/02
 */
public class CreateExists {

    public static Stage stage;

    public static String path;

    public void onActionYesButton(ActionEvent actionEvent) {
        stage.close();
        Index.disk.getDiskService().createFile(path);
        NewFile.stage.close();
    }

    public void onActionNoButton(ActionEvent actionEvent) {
        stage.close();
    }
}
