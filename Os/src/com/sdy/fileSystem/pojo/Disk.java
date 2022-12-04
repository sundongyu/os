package com.sdy.fileSystem.pojo;

/**
 * @author 孙东宇
 * 创建时间：2022/10/23
 * 介绍：磁盘
 */
public interface Disk {

    /**
     * 文件路径
     */

    String NULL = "00000000";
    String NULLBIT = "0000000000000000000000000000000000000000000000000000000000000000";

    // 0：只读且隐藏、1：非只读且隐藏、2：只读且非隐藏、3：非只读且非隐藏
    String READONLYANDHIDDEN = "0";
    String NOTREADONLYANDHIDDEN = "1";
    String READONLYANDNOTHIDDEN = "2";
    String NOTREADNOLYNOTHIDDEN = "3";

    /**
     * 一个磁盘默认有128个磁盘块
     */
    int diskSize = 128;
}
