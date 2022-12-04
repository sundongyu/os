package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

/**
 * @author 孙东宇
 * 创建时间：2022/11/15
 * 介绍：
 */
public class NewFile {

    @FXML
    public static Stage stage;
    public static String name;
    public final Image block = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/sdy/fileSystem/resource/block.png")));
    public final Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/sdy/fileSystem/resource/icon.png")));
    public TextField newFileName;

    public void saveNewFile(ActionEvent actionEvent) throws IOException {
        CharSequence characters = newFileName.getCharacters();
        name = FileManage.PATH;
        if (!"/".equals(name)) name += "/";
        name += characters.toString();
        CreateExists.path = name;
        int[] fileBlock = Index.disk.getDiskService().getDisk().getFileBlock(name);
        if (fileBlock[1] == 0 && fileBlock[0] == 0) {
            Index.disk.create(name);
            Index index = new Index();
            index.updataFileManage(Index.root);
            index.updateBlockUser();
            stage.close();
        } else createExistsFile();
    }

    public void createExistsFile() {
        try {
            CreateExists.stage = new Stage();
            CreateExists.stage.setResizable(false);
            CreateExists.stage.setTitle("提示");
            CreateExists.stage.initStyle(StageStyle.UNIFIED);
            AnchorPane load = FXMLLoader.load(getClass().getResource("../layout/createExistsFile.fxml"));
            CreateExists.stage.setScene(new Scene(load));
            CreateExists.stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void returnNewFile(ActionEvent actionEvent) {
        stage.close();
    }
}
