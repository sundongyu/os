package com.sdy.fileSystem.controller;

import com.sdy.fileSystem.pojo.DiskImpl.DiskImpl;
import com.sdy.fileSystem.service.diskservice.DiskService;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.text.Element;
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
    public final javafx.scene.image.Image file = new Image(getClass().getResourceAsStream("../resource/file.png"));
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
            if(diskService.isDir(s)) curRoot = new TreeItem<String>(s, imageView);
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

    public void create(String path) {
        diskService.createFile(path);
    }

    public void copy(String src, String dest) {
        diskService.copy(src, dest);
    }

    public void edit(String path, String data) {
        boolean readOnly = diskService.getDisk().isReadOnly(path);
        if(readOnly) return;
        diskService.editFile(path, data);
    }

    public void delete(String path) {
        diskService.deleteFile(path);
    }

    public void move(String src, String dest) {
        diskService.moveFile(src, dest);
    }

    public String type(String path) {
        return diskService.readFile(path);
    }

    public void mkdir(String path) {
        diskService.createFile(path);
    }

    public void change(String path, String attribute) {

    }

    public void init() {
        diskService.getDisk().init();
    }
}
