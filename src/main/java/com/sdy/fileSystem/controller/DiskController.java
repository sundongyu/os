package com.sdy.fileSystem.controller;

import com.sdy.fileSystem.service.diskservice.DiskService;

import java.util.List;

/**
 * @author 孙东宇
 * 创建时间：2022/11/04
 * 介绍：
 */
public class DiskController {
    private DiskService diskService;
    private String curPath;

    public DiskController() {
        diskService = new DiskService();
        curPath = "/";
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
        return diskService.ls(path);
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
