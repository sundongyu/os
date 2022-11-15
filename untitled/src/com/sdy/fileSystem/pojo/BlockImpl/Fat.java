package com.sdy.fileSystem.pojo.BlockImpl;

import com.sdy.fileSystem.pojo.Block;

/**
 * @author 孙东宇
 * 创建时间：2022/10/23
 * 介绍：文件分配表FAT
 */
public class Fat implements Block {
    /**
     * 该磁盘块是否被使用
     */
    private boolean state = false;

    public Fat() {
    }

    public Fat(boolean state) {
        this.state = state;
    }
}
