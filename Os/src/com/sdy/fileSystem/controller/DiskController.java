package com.sdy.fileSystem.controller;

import com.sdy.fileSystem.pojo.DiskImpl.DiskImpl;
import com.sdy.fileSystem.service.diskservice.DiskService;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sun.misc.Resource;
import sun.tools.java.ClassPath;

import javax.swing.text.Element;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * @author 孙东宇
 * 创建时间：2022/11/04
 * 介绍：
 */
public class DiskController {
    private DiskService diskService;
    private String curPath;
    public static Map<Integer, String> map;
//    public final javafx.scene.image.Image file = new Image(getClass().getResourceAsStream("../resource/file.png"));
    public final Image file = new Image("/com/sdy/fileSystem/resource/file.png");

    public DiskController() {
        diskService = new DiskService();
        curPath = "/";
    }

    public String getFileProperty(String path) {
        return diskService.getFileProperty(path);
    }

    public DiskService getDiskService() {
        return diskService;
    }

    public void setDiskService(DiskService diskService) {
        this.diskService = diskService;
    }

    public String getCurPath() {
        return curPath;
    }

    public void setCurPath(String curPath) {
        this.curPath = curPath;
    }

    public List<String> ls() {
        return ls(getCurPath());
    }

    public List<String> ls(String path) {
        List<String> res = diskService.ls(path);
        for (String idx : res) System.out.println(idx);
        return res;
    }

    public void dfsPath(String path, TreeItem<String> rootItem, Image icon) {
        List<String> ls = ls(path);
        for (String s : ls) {
            TreeItem<String> curRoot;
            Node imageView = new ImageView(icon);
            if(diskService.isDir(s)) {
                curRoot = new TreeItem<String>(s, imageView);
                curRoot.setExpanded(true);
            }
            else curRoot = new TreeItem<>(s, new ImageView(file));
            rootItem.getChildren().add(curRoot);
            if(diskService.isDir(s)) dfsPath(path + ("/".equals(path) ? "" : "/") + s, curRoot, icon);
        }
    }

    public void dfsDir(String path, TreeItem<String> rootItem, Image icon) {
        List<String> ls = ls(path);
        for (String s : ls) {
            TreeItem<String> curRoot;
            Node imageView = new ImageView(icon);
            if(diskService.isDir(s)) {
                curRoot = new TreeItem<String>(s, imageView);
                rootItem.getChildren().add(curRoot);
                dfsDir(path + ("/".equals(path) ? "" : "/") + s, curRoot, icon);
            }
        }
    }

    public void chadir(String path) {
        curPath = path;
    }

    public void create(String path) throws IOException {
        System.out.println("创建文件" + path);
        String[] split = path.split("/");
        int len = split.length;
        for (int i = 1; i < len - 1; i++) {
            if (!diskService.isDir(split[i])) {
                System.out.println("路径错误，存在文件于目录位置");
                return;
            }
            if(split[i].contains(".")) {
                System.out.println("文件下不能有目录文件");
                return;
            }
        }
        if(!split[len - 1].contains(".")) diskService.createFile(path);
        else {
            if(split[len - 1].indexOf(".") == split[len - 1].lastIndexOf(".") && split[len - 1].indexOf(".") == split[len - 1].length() - 2) diskService.createFile(path);
            else System.out.println("文件名格式异常");
            System.out.println("--------------------------------------------------");
            System.out.println(split[len - 1].indexOf("."));
            System.out.println(split[len - 1].lastIndexOf("."));
            System.out.println(len - 2);
            System.out.println("--------------------------------------------------");
        }
        Index index = new Index();
        index.updateBlockUser();
    }

    public void copy(String src, String dest) throws IOException {
        diskService.copy(src, dest);
        if(!src.substring(src.lastIndexOf("/")).equals(dest.substring(dest.lastIndexOf("/")))){
            String name = dest.substring(dest.lastIndexOf("/") + 1, dest.lastIndexOf("."));
            String suffix = dest.substring(dest.lastIndexOf(".") + 1);
            diskService.reName(dest.substring(0, dest.lastIndexOf("/")) + src.substring(src.lastIndexOf("/")), name, suffix);
        }
        Index index = new Index();
        index.updateBlockUser();
    }

    public void edit(String path, String data) throws IOException {
        boolean readOnly = diskService.getDisk().isReadOnly(path);
        if(readOnly) return;
        diskService.editFile(path, data);
        Index index = new Index();
        index.updateBlockUser();
    }

    public void delete(String path) throws IOException {
        diskService.deleteFile(path);
        Index index = new Index();
        index.updateBlockUser();
    }

    public void move(String src, String dest) throws IOException {
        diskService.moveFile(src, dest);
        System.out.println(src);
        System.out.println(dest);
        if(!src.substring(src.lastIndexOf("/")).equals(dest.substring(dest.lastIndexOf("/")))){
            String name = dest.substring(dest.lastIndexOf("/") + 1, dest.lastIndexOf("."));
            String suffix = dest.substring(dest.lastIndexOf(".") + 1);
            diskService.reName(dest.substring(0, dest.lastIndexOf("/")) + src.substring(src.lastIndexOf("/")), name, suffix);
        }
        Index index = new Index();
        index.updateBlockUser();
    }

    public String type(String path) {
        return diskService.readFile(path);
    }

    public void mkdir(String path) throws IOException {
        diskService.createFile(path);
        Index index = new Index();
        index.updateBlockUser();
    }

    public void change(String path, String attribute) {
        if(attribute.length() != 2) return;
        diskService.change(path, attribute);
    }

    public void init() {
        diskService.getDisk().init();
    }
}
