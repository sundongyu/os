package com.sdy.fileSystem.pojo;

/**
 * @author 孙东宇
 * 创建时间：2022/10/23
 * 介绍：磁盘块
 */
public interface Block {

    /**
     * 一个磁盘块大小默认64字节
     */
    int blockSize = 64;

    /**
     * 磁盘块中保存的数据
     */
    byte[] data = new byte[blockSize];
}
