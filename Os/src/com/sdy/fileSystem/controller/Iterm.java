package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.List;

/**
 * @author 孙东宇
 * 创建时间：2022/11/17
 * 介绍：
 */
public class Iterm {

    public static final String itermHead = "[root@localhost  ";
    public static final String itermTail = "]#  ";
    public static String curPath = "/";
    public static Stage stage;
    public static String[] dataArr = new String[100];
    public static int idx = 0;
    public static TextArea textArea;
    public static String temp;
    public static boolean editLock;
    public static int positionCarets;

    // 去除命令中的多余空格
    public static String delManySpace(String str) {
        int len = str.length();
        int pre = 0;
        int next = 0;
        char[] arr = str.toCharArray();
        boolean b = false;
        while (next != len) {
            if (arr[next] != ' ') {
                if (b) b = false;
                arr[pre++] = arr[next++];
                continue;
            }
            if (b) {
                next++;
            } else {
                b = true;
                arr[pre++] = arr[next++];
            }
        }
        String s = new String(arr, 0, pre);
        System.out.println(s);
        return s;
    }

    // 处理命令
    public void updateButton(String temp) {
        if (temp == null) return;
        if (editLock) {
            String name = textArea.getText().substring(positionCarets - 5, positionCarets);
            if (curPath.equals("/")) name = curPath + name;
            else name = curPath + "/" + name;
            System.out.println("编辑的文件名字为：" + name);
            String data = textArea.getText().substring(positionCarets + 1, textArea.getLength());
            System.out.println("编辑的文件内容为：" + data);
            Index.disk.edit(name, data);
            showItermData("文件修改成功");
            editLock = false;
            return;
        }
        temp = temp.trim();
        temp = delManySpace(temp);
        String[] arr = temp.split(" ");
        if (arr.length == 1) {
            if(arr[0].equals("")) textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
            if(arr[0].equals("clear")) {
                textArea.clear();
                textArea.insertText(textArea.getLength(), itermHead + curPath + itermTail);
            }
            if (arr[0].equals("ls")) {
                List<String> list = Index.disk.ls(curPath);
                for (String s : list) {
                    textArea.insertText(textArea.getLength(), '\n' + s);
                }
                textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
            }
            if (arr[0].equals("cd..")) {
                if (curPath.equals("/")) {
                    textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                    return;
                } else if (curPath.indexOf("/") == curPath.lastIndexOf("/")) curPath = "/";
                else curPath = curPath.substring(0, curPath.lastIndexOf("/"));
                textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
            }
            if (arr[0].equals("exit")) Iterm.stage.close();
            if (arr[0].equals("format")) {
                Index.disk.getDiskService().getDisk().format();
                showItermData("格式化成功");
            }
        } else if (arr.length == 2) {
            String path = arr[1];
            if (arr[1].charAt(0) != '/' && !curPath.equals("/")) path = curPath + "/" + arr[1];
            else if (curPath.equals("/")) path = "/" + arr[1];
            switch (arr[0]) {
                case "create":
                    Index.disk.create(path);
                    showItermData("创建成功");
                    break;
                case "delete":
                    Index.disk.delete(path);
                    textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                    break;
                case "type":
                    String type = Index.disk.type(path);
                    showItermData(type);
                    break;
                case "edit":
                    editLock = true;
                    String fileData = Index.disk.type(path);
                    positionCarets = textArea.getLength();
                    textArea.insertText(positionCarets, '\n' + fileData);
//                    String editedFileData = textArea.getText().substring(len, textArea.getLength());
//                    Index.disk.edit(path, editedFileData);
//                    textArea.insertText(textArea.getLength(), '\n' + "文件修改成功" + itermHead + curPath + itermTail);
                    break;
                case "change":
                case "cd":
                    if (curPath.equals("/") || curPath.charAt(0) == '/') curPath = path;
                    else curPath += "/" + path;
                    textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                    break;
                default:
                    showItermData("命令错误");
            }
        } else if (arr.length == 3) {
            String src = arr[1], desc = arr[2];
            if (arr[1].charAt(0) != '/' && !curPath.equals("/")) src = curPath + "/" + arr[1];
            else if (arr[1].charAt(0) != '/' && curPath.equals("/")) src = "/" + arr[1];

            if (arr[2].charAt(0) != '/' && !curPath.equals("/")) desc = curPath + "/" + arr[2];
            else if (arr[2].charAt(0) != '/' && curPath.equals("/")) desc = "/" + arr[2];

            if (arr[0].equals("move") || arr[0].equals("mv")) {
                Index.disk.move(src, desc);
            } else if (arr[0].equals("copy") || arr[0].equals("cp")) {
                Index.disk.copy(src, desc);
            }
            textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
        }

    }

    // 更新终端内容
    public void showItermData(String data) {
        textArea.insertText(textArea.getLength(), '\n' + data + '\n' + itermHead + curPath + itermTail);
    }

    // 回车事件,绑定了按钮
    public void onActionButton(ActionEvent actionEvent) {
        String text = textArea.getText();
        String substring = text.substring(text.lastIndexOf("#  ") + 3);
        updateButton(substring);
    }
}

