package com.sdy.fileSystem.pojo.DiskImpl;

import com.sdy.fileSystem.pojo.Disk;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author 孙东宇
 * 创建时间：2022/10/24
 * 介绍：
 */
public class DiskImplTest extends TestCase {

    // 格式化磁盘，所有数据清空
    public void testInit() throws IOException {
        File file = new File(Disk.url);
        file.delete();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(Disk.url);
        for(int i = 0;i < 64 * 128; i++) fileWriter.write("00000000");
        fileWriter.close();
    }
}