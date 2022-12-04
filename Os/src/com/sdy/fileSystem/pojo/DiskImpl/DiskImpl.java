package com.sdy.fileSystem.pojo.DiskImpl;

import com.sdy.fileSystem.controller.Index;
import com.sdy.fileSystem.pojo.Disk;

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

    public static String URL;
    public static String path = Disk.class.getResource("/disk.txt").getPath();
    private boolean[] state;
    private int numOfRootDir = 0;
    /**
     * 已经被使用过得磁盘块数量
     */
    private int usedNum = 3;

    public DiskImpl() {
        // mac打jar包后用这种路径
//        path = path.substring(0, path.lastIndexOf("/"));
//        if(path.charAt(0) != '/') URL = path.substring(5, path.lastIndexOf("/")) + "/disk.txt";
//        else URL = path.substring(0, path.lastIndexOf("out") + 3) + "/disk.txt";
        // ide上使用这种
//        URL = "src/disk.txt";
        // win平台下路径
        path = path.substring(path.indexOf("/") + 1, path.lastIndexOf("Os.jar!"));
        URL = path + "/disk.txt";
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
            System.out.println(URL);
            // 两个fat和一个根目录
            // 一个磁盘块一个磁盘块读取
            char[] buf = new char[64 * 8 * 2];
            // 初始化fat
            File file = new File(URL);
            if (!file.exists()) format();
            fileReader = new FileReader(file);
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
            File file = new File(URL);
//            File file = new File(getClass().getResource("/disk.txt").getFile());
            if (file.exists()) file.delete();
            file.createNewFile();
//            fileWriter = new OutputStreamWriter(new FileOutputStream(getClass().getResource("/disk.txt").getFile()), StandardCharsets.UTF_8);
            fileWriter = new OutputStreamWriter(new FileOutputStream(URL), StandardCharsets.UTF_8);

            for (int i = 3; i < 64 * 128; i++) fileWriter.write(Disk.NULL);
            for (int i = 0; i < 3; i++) updateFat(i, 1);
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
            RandomAccessFile randomAccessFile = new RandomAccessFile(URL, "rw");
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
            fileReader = new InputStreamReader(new FileInputStream(URL), StandardCharsets.UTF_8);
//            fileReader = new InputStreamReader(getClass().getResourceAsStream("/disk.txt"), StandardCharsets.UTF_8);
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
        // 逻辑上a磁盘块指向b磁盘块，当b为0的时候表示a就是最后一个磁盘块，但是由于我们使用了0来作为磁盘块是否被使用的标志，所以，我们把fat的磁盘块1号块作为终止条件
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
        while (root != 1) {
            state[root] = false;
            pre = getFatNextBlock(root);
            updateFat(root, 0);
            formatBlock(root);
            root = pre;
        }
    }

    public void formatBlock(int id) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(Disk.NULLBIT);
        write(id, 0, sb.toString());
    }

    public boolean isReadOnly(String path) {
        int[] fileBlock = getFileBlock(path);
        String read = binaryToAscll(read(fileBlock[3], fileBlock[4] * 8 + 4, 1));
        return read.equals("0") || read.equals("2");
    }

    public boolean isReadOnly(int[] fileBlock) {
        String read = binaryToAscll(read(fileBlock[3], fileBlock[4] * 8 + 4, 1));
        return read.equals("0") || read.equals("2");
    }

    public boolean isHidden(String path) {
        int[] fileBlock = getFileBlock(path);
        String read = binaryToAscll(read(fileBlock[3], fileBlock[4] * 8 + 4, 1));
        return read.equals("1") || read.equals("3");
    }

    public boolean isHidden(int[] fileBlock) {
        String read = binaryToAscll(read(fileBlock[3], fileBlock[4] * 8 + 4, 1));
        return read.equals("1") || read.equals("3");
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
                String type = binaryToAscll(read(q, i * 8 + 4, 1));
                if (type.equals(Disk.NOTREADONLYANDHIDDEN) || type.equals(Disk.READONLYANDHIDDEN)) {
                    if (!Index.showAllToFileManage) continue;
                }
                sb.append(binaryToAscll(read(q, i * 8, 3)));
                // 判断是否是空
                if ("".equals(sb.toString())) continue;
                String suffix = binaryToAscll(read(q, i * 8 + 3, 1));
                if (!"".equals(suffix)) {
                    sb.append(".");
                    sb.append(suffix);
                }
                list.add(sb.toString());
            }
        }
    }

    // 获取fat表中 root -> null的所以磁盘块
    public void getFatList(List<Integer> list, int root) {
        while (root != 1) {
            list.add(root);
            root = getFatNextBlock(root);
        }
    }

    public int getFatList(int root) {
        int res = root;
        while (root != 1) {
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
        for (int i = 1; i < split.length; i++) {
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
            if (!b) return new int[]{0, 0, 0, 0, 0};


            // 获取实际目录
            res[2] = idx;
            if (idx < 8) res[4] = idx;
            else {
                int x = idx / 8;
                for (int u = 0; u < x; u++) res[3] = getFatNextBlock(res[3]);
                res[4] = idx % 8;
            }
            // 获取该目录或者文件的起始数据块号
            String read = binaryToNum(read(res[3], res[4] * 8 + 5, 1));
            if ("0".equals(read)) return new int[]{0, 0, 0, 0, 0};

            root = Integer.parseInt(read);
            res[1] = root;
            if (i != split.length - 1) {
                res[0] = root;
                res[3] = root;
            }
        }
        return res;
    }

    /**
     * 将内存中的磁盘使用情况（fat)，同步到磁盘中
     */
    public void stateToBlock(int blockNum, boolean state, int next) {
        String data = state ? ascllToBinary(next) : Disk.NULL;
        write(blockNum / 64, blockNum - (blockNum / 64) * 64, data);
    }

    /**
     * 回收root磁盘块下的idx个目录文件，将其后面文件覆盖至当前位置
     */
    public void oldrecycleBlock(int[] block) {
        // 目录：删除父目录中的数据，删除目录数据块中数据
        // 文件：删除父目录中的数据，删除数据块
        List<String> list = new ArrayList<>();
        getList(list, block[0]);
        int size = list.size();
        // 如果该数据刚刚好是某磁盘块的第一个数据，且是最后一个数据，删除后磁盘块就应该回收
        if (block[2] == size - 1) {
            write(block[3], block[4] * 8, Disk.NULLBIT);
            if (block[0] != 2 && block[4] % 8 == 0 && block[0] != block[3]) {
                state[block[3]] = false;
                updateFat(block[3], 0);
            }
            return;
        }
        int lastBlock = getFatList(block[0]);
        String read = read(lastBlock, 8 * (size % 8) - 8, 8);
        write(block[3], block[4] * 8, read);
        write(lastBlock, 8 * (size % 8) - 8, Disk.NULLBIT);
    }

    public void recycleBlock(int[] block) {
        List<String> list = new ArrayList<>();
        List<Integer> fatList = new ArrayList<>();
        getList(list, block[0]);
        int size = list.size();
        getFatList(fatList, block[0]);
        int lastBlock = fatList.get(fatList.size() - 1);
        /**
         * 1、最后一个
         *      1、%=8：回收文件、回收文件所在目录
         *      2、%！=8：回收文件
         * 2、不是最后一个
         *      1、%=8：将最后一个数据转移到当前位置，回收最后一个文件所在目录
         *      2、%!=8：将最后一个数据转移到当前目录，清空最后一个文件所在位置数据
         *
         * 回收过程中要回收数据块，目录也有，文件也有，还要保证
         */

        // 目标文件不是该目录最后一个
        if (block[2] != size - 1) {
            String read = read(lastBlock, ((size - 1) % 8) * 8, 8);
            write(block[3], block[4] * 8, read);
            write(lastBlock, ((size - 1) % 8) * 8, Disk.NULLBIT);
        } else {
            write(block[3], block[4] * 8, Disk.NULLBIT);
        }
        if (size > 1 && size % 8 == 1 && lastBlock != 2) {
            state[lastBlock] = false;
            updateFat(lastBlock, 0);
            updateFat(fatList.get(fatList.size() - 2), 1);
        }
    }


    public int getNullBlock() {
        boolean[] state = getState();
        for (int i = 3; i < 128; i++)
            if (!state[i]) {
                state[i] = true;
                updateFat(i, 1);
                usedNum++;
                return i;
            }
        return 0;
    }
}
