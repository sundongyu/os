package com.sdy.fileSystem.pojo.DiskImpl;

import com.sdy.fileSystem.pojo.Block;
import com.sdy.fileSystem.pojo.Disk;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
            fileReader = new FileReader(Disk.URL);
            fileReader.read(buf);
            for (int j = 3; j < 128; j++) state[j] = !Disk.NULL.equals(new String(buf, 8 * j, 8));
            System.out.println("初始化成功");
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void format() {
//        FileWriter fileWriter = null;
        OutputStreamWriter fileWriter = null;
        try {
            File file = new File(Disk.URL);
            if (file.exists()) file.delete();
            file.createNewFile();
            fileWriter = new OutputStreamWriter(new FileOutputStream(Disk.URL), StandardCharsets.UTF_8);
            for (int i = 0; i < 64 * 128; i++) fileWriter.write(Disk.NULL);
            init();
//            System.out.println("格式化成功");
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            RandomAccessFile randomAccessFile = new RandomAccessFile(Disk.URL, "rw");
            randomAccessFile.seek(idx);
            randomAccessFile.write(input.getBytes(StandardCharsets.UTF_8));
            randomAccessFile.close();
//            System.out.println("文件修改成功");
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
            fileReader = new InputStreamReader(new FileInputStream(Disk.URL), StandardCharsets.UTF_8);
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
        }
//        if (!sb.toString().equals("")) System.out.println("将二进制" + binaryStr + "转化为：" + sb.toString());
        return sb.toString();
    }


    public String binaryToNum(String binaryStr) {
        int len = binaryStr.length();
        StringBuilder sb = new StringBuilder();
        char[] chars = binaryStr.toCharArray();
        int count = 0;
        int idx = chars.length - 1;
        for (int j = 0; j < chars.length; j++) {
            count += (chars[idx] - '0') * Math.pow(2, j);
            idx--;
        }
        if (count != 0) sb.append(count);
        else sb.append("0");
        return sb.toString();
    }

    @Test
    public void test1() {
        String s = Integer.toBinaryString(3);
        System.out.println(s);
        String s1 = binaryToAscll("01100001");
        System.out.println(s1);
    }

    public String ascllToBinary(Integer num) {
        StringBuilder sb = new StringBuilder();
        String bin = Integer.toBinaryString(num);
        int idx = bin.length() >= 8 ? 16 : 8;
        for (int i = 0; i < idx - bin.length(); i++) sb.append("0");
        return sb.append(bin).toString();
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
//        System.out.println("将字符" + ascllStr + "转化为：" + sb.toString());
        return sb.toString();
    }

    public String updateFileLength(int[] fileBlock, int len) {
        StringBuilder sb = new StringBuilder();
        String binary = ascllToBinary(len);
        if (binary.length() == 8) sb.append(Disk.NULL);
        sb.append(binary);
        write(fileBlock[3], fileBlock[4] * 8 + 6, sb.toString());
        return sb.toString();
    }

    // a to b
    public void updateFat(int a, int b) {
        write(a / 64, a - (a / 64) * 64, ascllToBinary(b));
    }

    public int fileLengthToBlockNum(int len) {
        return len / 64 + (len % 64 == 0 ? 0 : 1);
    }

    public int getFileLength(int[] fileBlock) {
        return Integer.parseInt(binaryToNum(read(fileBlock[3], fileBlock[4] * 8 + 6, 2)));
    }

    public int getStartBlock(int[] fileBlock) {
        return fileBlock[1];
    }

    public int getFatNextBlock(int root) {
        return Integer.parseInt(binaryToNum(read(root / 64, root - (root / 64) * 64, 1)));
    }

    // 回收空闲磁盘块
    public void removeBlock(int id) {
        int root = id;
        int pre = 0;
        while (root != 0) {
            state[root] = false;
            pre = getFatNextBlock(root);
            write(root / 64, root - (root / 64) * 64, Disk.NULL);
            root = pre;
        }
    }

    /**
     * 返回root编号的磁盘块中保存的所有目录项
     */
    public void getList(List<String> list, int root) {
        List<Integer> fatList = new ArrayList<>();
        getFatList(fatList, root);
        // 遍历所有磁盘块中的所有的项
        for (int q : fatList) {
            for (int i = 0; i < 8; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(binaryToAscll(read(q, i * 8, 3)));
                // 判断是否是空
                if ("".equals(sb.toString())) continue;
                String type = binaryToAscll(read(q, i * 8 + 4, 1));
                if ("f".equals(type)) {
                    sb.append(".");
                    sb.append(binaryToAscll(read(q, i * 8 + 3, 1)));
                }
                list.add(sb.toString());
            }
        }
    }

    // 获取fat表中 root -> null的所以磁盘块
    public void getFatList(List<Integer> list, int root) {
        while (root != 0) {
            list.add(root);
            root = getFatNextBlock(root);
        }
    }

    public int getFatList(int root) {
        int res = root;
        while (root != 0) {
            res = root;
            root = getFatNextBlock(root);
        }
        return res;
    }

    /**
     * 返回指定文件或者目录的所在目录及其数据所在目录
     * res[0]：父目录所在磁盘块
     * res[1]：文件所在的磁盘块
     * res[2]：该文件在父目录项中的索引
     */
    public int[] getFileBlock(String path) {
        String[] split = path.split("/");
        ArrayList<String> list = new ArrayList<>();
        int[] res = new int[5];
        int idx = 0;
        int root = 2;
        res[0] = res[1] = res[3] = root;
        for (int i = 1; i <= split.length - 1; i++) {
            idx = 0;
            list.clear();
            getList(list, root);
            boolean b = false;
            for (String s : list) {
                if (split[i].equals(s)) {
                    b = true;
                    break;
                }
                idx++;
            }
            if(!b) return new int[]{0, 0, 0, 0, 0};
            // 统一更新索引
            res[2] = res[4] = idx;
            // 获取下一层目录块号
            if(idx > 7) {
                int j = (idx + 1) / 8 + ((idx + 1) % 8 == 0 ? 0 : 1);
                int fatNextBlock = res[0];
                for (int k = 0; k < j - 1; k++) {
                    fatNextBlock = getFatNextBlock(fatNextBlock);
                    idx -= 8;
                }
                // 实际父目录
                res[4] = idx;
                res[3] = fatNextBlock;
            }
            String read = binaryToNum(read(res[3], res[4] * 8 + 5, 1));
//            System.out.println(read);
             if ("0".equals(read)) return new int[]{0, 0, 0, 0, 0};
            res[0] = root;
            root = Integer.parseInt(read);
            res[1] = root;
        }
        return res;
    }

    /**
     * 回收root磁盘块下的idx个目录文件，将其后面文件覆盖至当前位置
     */
    public void reccyleBlock(int[] block) {
        List<String> list = new ArrayList<>();
        getList(list, block[0]);
        int size = list.size();
        // 如果改数据刚刚好是某磁盘块的第一个数据，且是最后一个数据，删除后磁盘块就应该回收
        if (block[2] == size - 1) {
            write(block[3], block[4] * 8, Disk.NULLBIT);
            if(block[4] % 8 == 0) state[block[3]] = false;
            return;
        }
        int lastBlock = getFatList(block[0]);
        String read = read(lastBlock, 8 * (size % 8), 8);
        write(block[3], block[4] * 8, read);
        write(lastBlock, 8 * (size % 8), Disk.NULLBIT);
    }

    public int getNullBlock() {
        boolean[] state = getState();
        for (int i = 3; i < 128; i++)
            if (!state[i]) {
                state[i] = true;
                usedNum++;
                return i;
            }
        return 0;
    }

    @Test
    public void test() {
    }
}
