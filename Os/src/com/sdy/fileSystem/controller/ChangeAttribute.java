package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.IDN;

/**
 * @author 孙东宇
 * 创建时间：2022/12/02
 */
public class ChangeAttribute {
    public CheckBox hidden;
    public CheckBox readonly;
    public Button yesButton;
    public Button noButton;
    public static Stage stage;

    public void hiddenOnAction(ActionEvent actionEvent) {
    }

    public void readonlyOnAction(ActionEvent actionEvent) {
    }

    public void yesButton(ActionEvent actionEvent) {

        if(hidden.isSelected()) {
            Index.disk.change(FileManage.PATH, "-r");
        } else {
            Index.disk.change(FileManage.PATH, "+r");
        }

        if(readonly.isSelected()) {
            Index.disk.change(FileManage.PATH, "-w");
        } else {
            Index.disk.change(FileManage.PATH, "+w");
        }
        stage.close();
        try {
            new Index().updataFileManage(Index.root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void noButton(ActionEvent actionEvent) {
        stage.close();
    }
}
