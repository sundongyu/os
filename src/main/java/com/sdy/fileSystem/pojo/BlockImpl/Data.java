package com.sdy.fileSystem.pojo.BlockImpl;

import com.sdy.fileSystem.pojo.Block;

/**
 * @author 孙东宇
 * 创建时间：2022/10/23
 * 介绍：保存数据的磁盘块
 */
public class Data implements Block {
    /**
     * 该磁盘块是否被使用
     */
    private boolean state = false;



    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public Data() {
    }

    public Data(boolean state) {
        this.state = state;
    }
}
