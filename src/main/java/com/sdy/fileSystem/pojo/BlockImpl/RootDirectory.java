package com.sdy.fileSystem.pojo.BlockImpl;

import com.sdy.fileSystem.pojo.Block;

/**
 * @author 孙东宇
 * 创建时间：2022/10/24
 * 介绍：
 */
public class RootDirectory implements Block {
    /**
     * 该磁盘块是否被使用
     */
    private boolean state = false;

    private int num = 0;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public RootDirectory(boolean state) {
        this.state = state;
    }

    public RootDirectory() {
    }
}
