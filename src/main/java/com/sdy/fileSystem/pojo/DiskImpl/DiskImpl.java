package com.sdy.fileSystem.pojo.DiskImpl;

import com.sdy.fileSystem.pojo.Block;
import com.sdy.fileSystem.pojo.Disk;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author 孙东宇
 * 创建时间：2022/10/23
 * 介绍：
 */
public class DiskImpl implements Disk {

    private boolean[] state;
    private int numOfRootDir = 0;
    private List<Block> blocks;
    /**
     * 已经被使用过得磁盘块数量
     */
    private int usedNum = 3;

    public DiskImpl() {
//        format();
    }

    public int getNumOfRootDir() {
        return numOfRootDir;
    }

    public void setNumOfRootDir(int numOfRootDir) {
        this.numOfRootDir = numOfRootDir;
    }

    public boolean[] getState() {
        return state;
    }

    public void setState(boolean[] state) {
        this.state = state;
    }


    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public int getUsedNum() {
        return usedNum;
    }

    public void setUsedNum(int usedNum) {
        this.usedNum = usedNum;
    }

    /**
     * 初始化fat表，系统开机时执行，将fat表写入内存，并常驻内存
     */
    public void init() {
        FileReader fileReader = null;
        try {
            state = new boolean[Disk.diskSize];
            state[0] = state[1] = state[2] = true;
            // 两个fat和一个根目录
            // 一个磁盘块一个磁盘块读取
            char[] buf = new char[64 * 8 * 2];
            // 初始化fat
            fileReader = new FileReader(Disk.url);
            fileReader.read(buf);
            for (int j = 3; j < 128; j++) state[j] = !"00000000".equals(new String(buf, 8 * j, 8));
            System.out.println("初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void format() {
//        FileWriter fileWriter = null;
        OutputStreamWriter fileWriter = null;
        try {
            File file = new File(Disk.url);
            if (file.exists()) file.delete();
            file.createNewFile();
            fileWriter = new OutputStreamWriter(new FileOutputStream(Disk.url), StandardCharsets.UTF_8);
            for (int i = 0; i < 64 * 128; i++) fileWriter.write("00000000");
            init();
            System.out.println("格式化成功");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 无论是创建文件、目录实际上都是对数据的写入
     * 文件输入流使用的是RandomAcessFile，即文件以覆盖形式写入！
     *
     * @param blockNum 待写入的磁盘块号
     * @param idx      从文件头开始，带插入的位置
     * @param input    写入的内容
     */
    public void write(int blockNum, int idx, String input) {
        try {
            idx += blockNum * 64;
            idx *= 8;
            RandomAccessFile randomAccessFile = new RandomAccessFile(Disk.url, "rw");
            randomAccessFile.seek(idx);
            randomAccessFile.write(input.getBytes(StandardCharsets.UTF_8));
            randomAccessFile.close();
            System.out.println("文件修改成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从磁盘块blockNun的第start个字节开始，读取长度为len字节的内容
     */
    public String read(int blockNum, int start, int len) {
        start += blockNum * 64;
        StringBuilder sb = new StringBuilder();
        InputStreamReader fileReader;
        try {
            fileReader = new InputStreamReader(new FileInputStream(Disk.url), StandardCharsets.UTF_8);
            char[] buf = new char[8];
            for (int i = 0; i < start; i++) fileReader.read(buf);
            for (int i = 0; i < len; i++) {
                fileReader.read(buf);
                sb.append(new String(buf));
            }
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 将一个二进制字符串转化为字符，注意：该字符串长度应该为8的整数倍
     *
     * @param binaryStr 输入的带转化的二进制串
     * @return 转化结果
     */
    public String binaryToAscll(String binaryStr) {
        int len = binaryStr.length();
        StringBuilder sb = new StringBuilder();
        if (len % 8 != 0) {
            System.out.println("输入长度错误，不符合8的倍数");
            return sb.toString();
        }
        char[] chars = binaryStr.toCharArray();
        for (int i = 0; i < len / 8; i++) {
            int count = 0;
            int idx = (i + 1) * 8 - 1;
            for (int j = 0; j < 8; j++) {
                count += (chars[idx] - '0') * Math.pow(2, j);
                idx--;
            }
            if (count != 0) sb.append((char) count);
            else sb.append("");
        }
        if (!sb.toString().equals("")) System.out.println("将二进制" + binaryStr + "转化为：" + sb.toString());
        return sb.toString();
    }


    public String binaryToNum(String binaryStr) {
        int len = binaryStr.length();
        StringBuilder sb = new StringBuilder();
        if (len % 8 != 0) {
            System.out.println("输入长度错误，不符合8的倍数");
            return sb.toString();
        }
        char[] chars = binaryStr.toCharArray();
        int count = 0;
        int idx = 7;
        for (int j = 0; j < 8; j++) {
            count += (chars[idx] - '0') * Math.pow(2, j);
            idx--;
        }
        if (count != 0) sb.append(count);
        else sb.append("");
        return sb.toString();
    }

    public String ascllToBinary(Integer num) {
        StringBuilder sb = new StringBuilder("0");
        return sb.append(Integer.toBinaryString(num)).toString();
    }

    public String ascllToBinary(String ascllStr) {
        StringBuilder sb = new StringBuilder("");
        try {
            // 转化为二进制
            byte[] bytes = ascllStr.getBytes(StandardCharsets.UTF_8);
            for (byte b : bytes) {
                String binary = Integer.toBinaryString(b);
                for (int i = 0; i < 8 - binary.length(); i++) sb.append("0");
                sb.append(binary);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("将字符" + ascllStr + "转化为：" + sb.toString());
        return sb.toString();
    }

    public int getNullBlock() {
        boolean[] state = getState();
        for (int i = 3; i < 128; i++) {
            if (!state[i]) {
                state[i] = true;
                return i;
            }
        }
        return 0;
    }

    @Test
    public void test() {
//        String s = ascllToBinary("qwer");
        String s = ascllToBinary(10);
        String s1 = binaryToNum(s);
//        System.out.println(s);
        System.out.println(s1);
    }
}
