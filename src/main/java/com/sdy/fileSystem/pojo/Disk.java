package com.sdy.fileSystem.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 孙东宇
 * 创建时间：2022/10/23
 * 介绍：磁盘
 */
public interface Disk {

    /**
     * 文件路径
     */
    String url = "src/main/resources/disk.txt";

    /**
     * 一个磁盘默认有128个磁盘块
     */
    int diskSize = 128;

    /**
     * 文件分配表的数量
     */
    int fatNum = 2;

    /**
     * 保存根目录的磁盘块的数量
     */
    int saveRootBlockNum = 1;

    int saveDataBlockNum = 125;

}
