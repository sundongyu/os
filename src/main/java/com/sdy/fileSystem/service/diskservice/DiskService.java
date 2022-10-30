package com.sdy.fileSystem.service.diskservice;

import com.sdy.fileSystem.pojo.DiskImpl.DiskImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 孙东宇
 * 创建时间：2022/10/24
 * 介绍：
 */
public class DiskService {
    private DiskImpl disk = new DiskImpl();

    /**
     * 返回root编号的磁盘块中保存的所有目录项
     */
    public void getList(List<String> list, int root) {

        // 文件名：3个字节   扩展名：1个字节  目录、文件属性：1字节；起始盘块号：1个字节； 文件长度：2字节（目录没有长度）
        for (int i = 0; i < 8; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(disk.binaryToAscll(disk.read(root, i * 8, 3)));
            // 判断是否是空
            if ("".equals(sb.toString())) continue;
            String type = disk.binaryToAscll(disk.read(root, i * 8 + 4, 1));
            if ("f".equals(type)) sb.append(".");
            sb.append(disk.binaryToAscll(disk.read(root, i * 8 + 3, 1)));
            list.add(sb.toString());
        }
    }

    /**
     * 获取根目录下的所有fcb
     */
    public void getRootList(List<String> list) {
        // 根目录下的所有目录项统一放在第2磁盘块
        getList(list, 2);
    }

    /**
     * 在根目录中添加项
     * 一个字节的后缀，3个字节的名字，最大长度为5
     * 在根目录添加项比较特殊，
     *
     * @param fileName
     */
    public void inRootCreateFile(String fileName) {
        int len = fileName.length();
        if (len > 5 || len == 0) {
            System.out.println("输入错误");
            return;
        }
        if (disk.getNumOfRootDir() >= 8) {
            System.out.println("稍后处理");
            return;
        }
        String name;
        String type;
        String suffix;
        // 有且只有一个分隔符 .
        if (isFile(fileName)){
            System.out.println(fileName.indexOf("."));
            System.out.println("待创建的是文件");
            name = fileName.substring(0, fileName.indexOf("."));
            type = "f";
            suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        } else if (isDir(fileName)) {
            System.out.println("待创建的是目录");
            name = fileName;
            type = "d";
            suffix = "";
        } else {
            System.out.println("输入错误");
            return;
        }

        disk.write(2, disk.getNumOfRootDir() * 8, disk.ascllToBinary(name));
        disk.write(2, disk.getNumOfRootDir() * 8 + 3, disk.ascllToBinary(suffix));
        disk.write(2, disk.getNumOfRootDir() * 8 + 4, disk.ascllToBinary(type));
        int id = disk.getNullBlock();
        if(id == 0) {
            System.out.println("空间不足");
            return;
        }
        disk.write(2, disk.getNumOfRootDir() * 8 + 5, disk.ascllToBinary(id));
        disk.setNumOfRootDir(disk.getNumOfRootDir() + 1);
    }

    /**
     * 返回指定文件或者目录的所在目录及其数据所在目录
     */
    public int[] getFileBlock(String path) {
        String[] split = path.split("/");
        ArrayList<String> list = new ArrayList<>();
        int[] res = new int[3];
        int idx = 0;
        int root = 2;
        for (int i = 1; i <= split.length - 1; i++) {
            if (i == 1) getRootList(list);
            else {
                list.clear();
                getList(list, root);
            }
            for (String s : list) {
                if (split[i].equals(s)) break;
                idx++;
            }
            res[2] = idx;
            System.out.println("下标为：" + idx);
            // idx指向列表中位次
            String read = disk.binaryToNum(disk.read(root, idx * 8 + 5, 1));
            System.out.println(read);
            if ("".equals(read)) return new int[]{0, 0, 0};
            root = Integer.parseInt(read);
            if (i == split.length - 2) res[0] = root;
        }
        res[1] = root;
        return res;
    }


    /**
     * 为文件中填充内容
     *
     * @param path 文件路径
     * @param data 填充的内容
     */
    public void editFile(String path, String data) {
        int fileLength = data.length();
        int[] startId = getFileBlock(path);
        // 该数据所需的磁盘块空间
        int size = data.length() / 64 + (data.length() % 64 != 0 ? 1 : 0);
        // 默认开辟一个磁盘块空间
        size -= 1;
        ArrayList<Integer> block = new ArrayList<>();
        block.add(startId[1]);
        if (128 - disk.getUsedNum() < size) {
            System.out.println("磁盘空间不足");
            return;
        }
        int pre = 0;
        // 数据写入fat表
        for (int i = 0; i < size; i++) {
            int id = disk.getNullBlock();
            disk.write(id / 64, pre, disk.ascllToBinary(id));
            block.add(id);
            pre = id;
        }
        // 写入数据磁盘块
        for (int i = 0; i < block.size(); i++) {
            disk.write(block.get(i), 0, disk.ascllToBinary(data.substring(i * 64, Math.min((i + 1) * 64, data.length()))));
        }
        System.out.println("数据写入成功：" + data);
        // 将文件长度更新
        disk.write(startId[0], startId[2] * 8 + 6, disk.ascllToBinary(fileLength));
        // 2、文件已经分配空间，新添加内容需要分配空间
        // 3、文件已经分配空间，内容减少，需要回收空间
    }
//
//    /**
//     * 更新fat表中的内容
//     *
//     * @param cur 当前磁盘块
//     * @param pre 指向的下一个磁盘块
//     */
//    public void updateFat(int cur, int pre) {
//        disk.write(cur / 64, cur - (cur / 64) * 64, disk.ascllToBinary(pre + ""));
//        boolean[] state = disk.getState();
//        state[cur] = state[pre] = true;
//    }

    public String readFile(String path) {
        int[] id = getFileBlock(path);
        ArrayList<Integer> block = new ArrayList<>();
        block.add(id[1]);
        while (id[1] != 0) {
            String read = disk.read(id[1] / 64, id[1] - (id[1] / 64) * 64, 1);
            if (!"".equals(disk.binaryToNum(read))) {
                id[1] = Integer.parseInt(disk.binaryToNum(read));
                block.add(id[1]);
            } else id[1] = 0;
        }
        int len = Integer.parseInt(disk.binaryToNum(disk.read(id[0], id[2] * 8 + 6, 2)));
        System.out.println(len);
        StringBuilder sb = new StringBuilder();
        for (int idx : block) {
            sb.append(disk.binaryToAscll(disk.read(idx, 0, Math.min(64, len))));
            len -= 64;
        }
        return sb.toString();
    }

    public boolean isFile(String name) {
        int len = name.length();
        return len > 0 && len <= 5 && name.contains(".") && name.indexOf(".") == name.lastIndexOf(".") && name.indexOf(".") == len - 2;
    }

    public boolean isDir(String name) {
        int len = name.length();
        return !isFile(name) && len > 0 && len <= 3 && !name.contains(".");
    }

    public void createFile(String path) {
        /*
        1. 输入路径
        2. 从根目录开始挨个查找给定路径中的目录或者文件是否存在
        3. 如果目录存在，继续往下判断，如果文件存在给出提示，是否覆盖
        4. 如果目录不存在，则创建此处之后的所有目录包括文件
        5. 如果文件不存在，创建文件即可
        * */
        String[] split = path.split("/");
        for (int i = 1; i < split.length - 1; i++) {
            if(!isDir(split[i])) {
                System.out.println("路径错误，存在文件于目录位置");
                return;
            }
        }
        if(!isFile(split[split.length - 1])) return;

        StringBuilder sb = new StringBuilder("/");
        int root = 2;
        int i = 0;
        for (i = 1; i <= split.length - 1; i++) {
            int[] fileBlock = getFileBlock(sb.append(split[i]).toString());
            if (fileBlock[0] == 0) break;
            root = fileBlock[0];
        }

        if (i == split.length - 1
        ) {
            // 后期补逻辑，只需要将文件的fat表除了第一个磁盘块外全部清空即可
            System.out.println("文件已存在，是否覆盖？");
            return;
        }
        // 在磁盘块root下将剩余文件目录创建完成
        String fileName, name, type, suffix;
        boolean isFile = false;
        List<String> list = new ArrayList<>();
        for (; i <= split.length - 1; i++) {
            if(isFile) {
                System.out.println("文件下不可创建目录或文件");
                return;
            }
            fileName = split[i];
            if (fileName.contains(".") && fileName.indexOf(".") == fileName.lastIndexOf(".")) {
                if (fileName.indexOf(".") != fileName.length() - 2) {
                    System.out.println("后缀超过一个字节");
                    return;
                }
                System.out.println(fileName.indexOf("."));
                System.out.println("待创建的是文件");
                name = fileName.substring(0, fileName.indexOf("."));
                type = "f";
                suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                isFile = true;
            } else if (!fileName.contains(".")) {
                System.out.println("待创建的是目录");
                name = fileName;
                type = "d";
                suffix = "";
            } else {
                System.out.println("输入错误");
                return;
            }
            list.clear();
            getList(list, root);
            int size = list.size();
            disk.write(root, size * 8, disk.ascllToBinary(name));
            disk.write(root, size * 8 + 3, disk.ascllToBinary(suffix));
            disk.write(root, size * 8 + 4, disk.ascllToBinary(type));
            int num = disk.getNullBlock();
            if(num == 0) {
                System.out.println("空间不足");
                return;
            }
            disk.write(root, size * 8 + 5, disk.ascllToBinary(num));
        }
    }

    @Test
    public void test() {
        disk.format();
        createFile("/ab/bc/aaa.c");
        editFile("/ab/bc/bbb.c", "qwers2341234adfa.sdf");
        String s = readFile("/ab/bc/bbb.c");
        System.out.println();
        System.out.println(s);
    }


}
