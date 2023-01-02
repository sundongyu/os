package com.sdy.fileSystem.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
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
    public static int idx = 0, tempIdx = 0;
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

    public static void lastCommand() {
        tempIdx--;
        if (tempIdx < 0) tempIdx += 100;
        tempIdx %= 100;
        if (dataArr[tempIdx] == null) {
            tempIdx++;
            return;
        }
        String data = textArea.getText();
        data = data.substring(0, data.lastIndexOf("#") + 3);
        textArea.clear();
        textArea.setText(data + dataArr[tempIdx]);
        textArea.positionCaret(textArea.getLength());
    }

    public static void nextCommand() {
        tempIdx++;
        tempIdx %= 100;
        if (dataArr[tempIdx] == null) {
            tempIdx--;
            return;
        }
        String data = textArea.getText();
        data = data.substring(0, data.lastIndexOf("#") + 3);
        textArea.clear();
        textArea.setText(data + dataArr[tempIdx]);
        textArea.positionCaret(textArea.getLength());
    }

    // 处理命令
    public void updateButton(String temp) throws IOException {
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
            if (arr[0].equals("")) textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
            else if (arr[0].equals("clear")) {
                textArea.clear();
                textArea.insertText(textArea.getLength(), itermHead + curPath + itermTail);
                idx %= 100;
                dataArr[idx++] = "clear";
            } else if (arr[0].equals("ls")) {
                Index.showAllToFileManage = false;
                List<String> list = Index.disk.ls(curPath);
                for (String s : list) {
                    textArea.insertText(textArea.getLength(), '\n' + s);
                }
                textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                idx %= 100;
                dataArr[idx++] = "ls";
            } else if (arr[0].equals("cd..")) {
                if (curPath.equals("/")) {
                    textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                    idx %= 100;
                    dataArr[idx++] = "cd..";
                    return;
                } else if (curPath.indexOf("/") == curPath.lastIndexOf("/")) curPath = "/";
                else curPath = curPath.substring(0, curPath.lastIndexOf("/"));
                textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                idx %= 100;
                dataArr[idx++] = "cd..";
            } else if (arr[0].equals("exit")) {
                Iterm.stage.close();
                curPath = "/";
            } else if (arr[0].equals("format")) {
                Index.disk.getDiskService().getDisk().format();
                showItermData("格式化成功");
                idx %= 100;
                dataArr[idx++] = "format";

            } else showItermData("命令错误");

        } else if (arr.length == 2) {
            String path = arr[1];
            path = pathProcessing(path);
            switch (arr[0]) {
                case "create":
                    Index.disk.create(path);
                    showItermData("创建成功");
                    idx %= 100;
                    dataArr[idx++] = temp;
                    break;
                case "deldir":
                    if (Index.disk.getDiskService().isDir(path)) {
                        Index.disk.getDiskService().dfsDel(path);
                    } else Index.disk.delete(path);
                    textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                    idx %= 100;
                    dataArr[idx++] = temp;
                    break;
                case "rdir":
                    if (Index.disk.getDiskService().isDir(path)) {
                        int[] fileBlock = Index.disk.getDiskService().getDisk().getFileBlock(path);
                        List<String> list = Index.disk.ls(path);
                        if (list.size() != 0) {
                            showItermData("非空目录不能删除");
                            break;
                        }
                    }
                    Index.disk.delete(path);
                    textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                    idx %= 100;
                    dataArr[idx++] = temp;
                    break;
                case "type":
                    String type = Index.disk.type(path);
                    showItermData(type);
                    idx %= 100;
                    dataArr[idx++] = temp;
                    break;
                case "edit":
                    boolean readOnly = Index.disk.getDiskService().getDisk().isReadOnly(path);
                    if (readOnly) {
                        showItermData("只读文件无修改权限");
                        return;
                    }
                    editLock = true;
                    String fileData = Index.disk.type(path);
                    positionCarets = textArea.getLength();
                    textArea.insertText(positionCarets, '\n' + fileData);
                    idx %= 100;
                    dataArr[idx++] = temp;
                    break;
                case "cd":
                    if (arr[1].equals("..")) {
                        if (curPath.equals("/")) {
                            textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                            idx %= 100;
                            dataArr[idx++] = "cd..";
                            return;
                        } else if (curPath.indexOf("/") == curPath.lastIndexOf("/")) curPath = "/";
                        else curPath = curPath.substring(0, curPath.lastIndexOf("/"));
                        textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                        idx %= 100;
                        dataArr[idx++] = "cd..";
                        return;
                    }
                    if (curPath.equals("/") || curPath.charAt(0) == '/') curPath = path;
                    else curPath += "/" + path;
                    textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                    idx %= 100;
                    dataArr[idx++] = temp;
                    break;
                case "ls":
                    if (arr[1].equals("-a")) {
                        Index.showAllToFileManage = true;
                        List<String> list = Index.disk.ls(curPath);
                        for (String s : list) {
                            textArea.insertText(textArea.getLength(), '\n' + s);
                        }
                        textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
                        idx %= 100;
                        dataArr[idx++] = "ls -a";
                    }
                    break;
                default:
                    showItermData("命令错误");
            }
        } else if (arr.length == 3) {
            String src = arr[1], desc = arr[2];
            src = pathProcessing(src);
            desc = pathProcessing(desc);
            System.out.println(src);
            System.out.println(desc);

            if (arr[0].equals("move") || arr[0].equals("mv")) {
                Index.disk.move(src, desc);
                idx %= 100;
                dataArr[idx++] = temp;
            } else if (arr[0].equals("copy") || arr[0].equals("cp")) {
                Index.disk.copy(src, desc);
                idx %= 100;
                dataArr[idx++] = temp;
            } else if (arr[0].equals("change")) {
                String path = arr[1];
                path = pathProcessing(path);
                Index.disk.change(path, arr[2]);
                idx %= 100;
                dataArr[idx++] = temp;
            } else {
                showItermData("命令错误");
                return;
            }
            textArea.insertText(textArea.getLength(), '\n' + itermHead + curPath + itermTail);
        } else showItermData("命令错误");
        tempIdx = idx;
        dataArr[tempIdx % 100] = "";
    }

    public String pathProcessing(String path) {
        String curPath = Iterm.curPath;
        // 绝对路径
        if (path.charAt(0) == '/') return path;
        // 相对路径 + 当前路径
        if ("./".equals(path.substring(0, 2)) && !path.contains("/")) {
            if ("/".equals(curPath)) return "/" + path;
            return curPath + "/" + path;
        } else if ("./".equals(path.substring(0, 2))) {
            path = path.substring(2);
        }
        // 相对路径 + 上层路径 ../../abc.txt   /aaa/bbb/ccc
        if (path.contains("../")) {
            while (path.contains("/")) {
                path = path.substring(path.indexOf("/") + 1);
                if (curPath.indexOf("/") != curPath.lastIndexOf("/"))
                    curPath = curPath.substring(0, curPath.lastIndexOf("/"));
                else {
                    if (curPath.equals("/")) return "";
                    else curPath = "/";
                }
            }
            if ("/".equals(curPath)) return "/" + path;
            return curPath + "/" + path;
        }
        // 相对路径 + 下层路径 abc/def/....
        if ("/".equals(curPath)) return "/" + path;
        return curPath + "/" + path;
    }


    // 更新终端内容
    public void showItermData(String data) {
        textArea.insertText(textArea.getLength(), '\n' + data + '\n' + itermHead + curPath + itermTail);
    }

    // 回车事件,绑定了按钮
    public void onActionButton(ActionEvent actionEvent) throws IOException {
        String text = textArea.getText();
        String substring = text.substring(text.lastIndexOf("#") + 1);
        substring = substring.trim();
        updateButton(substring);
    }
}

