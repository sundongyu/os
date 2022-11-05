package com.sdy.fileSystem.service.diskservice;

import com.sdy.fileSystem.pojo.Disk;
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
    private DiskImpl disk;

    public DiskService() {
        disk = new DiskImpl();
    }

    public DiskImpl getDisk() {
        return disk;
    }


    /**
     * 在根目录中添加项
     * 一个字节的后缀，3个字节的名字，最大长度为5
     * 在根目录添加项比较特殊，
     */
//    public void inRootCreateFile(String fileName) {
//        int len = fileName.length();
//        if (len > 5 || len == 0) {
//            System.out.println("输入错误");
//            return;
//        }
//        if (disk.getNumOfRootDir() >= 8) {
//            System.out.println("稍后处理");
//            return;
//        }
//        String name;
//        String type;
//        String suffix;
//        // 有且只有一个分隔符 .
//        if (isFile(fileName)) {
//            System.out.println(fileName.indexOf("."));
//            System.out.println("待创建的是文件");
//            name = fileName.substring(0, fileName.indexOf("."));
//            type = "f";
//            suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
//        } else if (isDir(fileName)) {
//            System.out.println("待创建的是目录");
//            name = fileName;
//            type = "d";
//            suffix = "";
//        } else {
//            System.out.println("输入错误");
//            return;
//        }
//
//        disk.write(2, disk.getNumOfRootDir() * 8, disk.ascllToBinary(name));
//        disk.write(2, disk.getNumOfRootDir() * 8 + 3, disk.ascllToBinary(suffix));
//        disk.write(2, disk.getNumOfRootDir() * 8 + 4, disk.ascllToBinary(type));
//        int id = disk.getNullBlock();
//        if (id == 0) {
//            System.out.println("空间不足");
//            return;
//        }
//        disk.updateFat(id, 0);
//        disk.write(2, disk.getNumOfRootDir() * 8 + 5, disk.ascllToBinary(id));
//        disk.setNumOfRootDir(disk.getNumOfRootDir() + 1);
//    }

    /**
     * 为文件中填充内容
     *
     * @param path 文件路径
     * @param data 填充的内容
     */
    public void editFile(String path, String data) {
        int fileLength = data.length();
        int[] startId = new int[5];
        int[] fileBlock = disk.getFileBlock(path);
        System.arraycopy(fileBlock, 0, startId, 0, 5);
        // 该数据所需的磁盘块空间
        int size = fileLength / 64 + ((fileLength % 64) != 0 ? 1 : 0);
        System.out.println(path + "      该文件所需要的磁盘块数量为：" + size);
        // 默认开辟一个磁盘块空间
        size -= 1;
        ArrayList<Integer> block = new ArrayList<>();
        block.add(startId[1]);
        if (128 - disk.getUsedNum() < size) {
            System.out.println("磁盘空间不足");
            return;
        }
        int pre = startId[1];
        // 数据写入fat表
        for (int i = 0; i < size; i++) {
            int id = disk.getNullBlock();
            block.add(id);
            disk.updateFat(pre, id);
            pre = id;
            if (i == size - 1) disk.updateFat(id, 0);
        }
        // 写入数据磁盘块
        for (int i = 0; i < block.size(); i++) {
            disk.write(block.get(i), 0, disk.ascllToBinary(data.substring(i * 64, Math.min((i + 1) * 64, fileLength))));
        }
//        System.out.println("数据写入成功：" + data);
        // 将文件长度更新
        disk.updateFileLength(startId, fileLength);
        String read = disk.read(startId[3], startId[4] * 8 + 6, 2);
        System.out.println(disk.binaryToNum(read));
    }

    public void modify(String path, String data) {
        int len = data.length();
        int blockNum = disk.fileLengthToBlockNum(len);
        int[] block = disk.getFileBlock(path);
        int[] fileBlock = new int[5];
        System.arraycopy(block, 0, fileBlock, 0, 5);
        // 起始磁盘块、父目录磁盘块、下标、
        int startBlock = disk.getStartBlock(fileBlock);
        // 旧文件长度
        int oldFileLen = disk.getFileLength(fileBlock);
        // 更新文件长度
        disk.updateFileLength(fileBlock, len);
        // 旧文件所需磁盘块数
        int oldBlockNum = disk.fileLengthToBlockNum(oldFileLen);
        List<Integer> list = new ArrayList<>();
        if (oldBlockNum > blockNum) {
            // 回收，够了就停
            int root = startBlock;
            while (blockNum > 0) {
                list.add(root);
                if (blockNum == 1) {
                    disk.removeBlock(root);
                    break;
                }
                root = disk.getFatNextBlock(root);
                blockNum--;
            }

        } else if (oldBlockNum < blockNum) {
            // 扩容，添加
            int root = startBlock;
            int pre = root;
            while (root != 0) {
                list.add(root);
                pre = root;
                root = disk.getFatNextBlock(root);
            }
            for (int i = 0; i < blockNum - oldBlockNum; i++) {
                root = disk.getNullBlock();
                list.add(root);
                disk.updateFat(pre, root);
                pre = root;
            }
            disk.updateFat(pre, 0);
        }
        // 写入数据磁盘块
        for (int i = 0; i < list.size(); i++) {
            disk.write(list.get(i), 0, disk.ascllToBinary(data.substring(i * 64, Math.min((i + 1) * 64, len))));
        }
    }

    public String readFile(String path) {
        int[] fileBlock = disk.getFileBlock(path);
        int[] id = new int[5];
        System.arraycopy(fileBlock, 0, id, 0, 5);
        if (id[2] == id[4] && id[2] == 0 && id[0] == id[3] && id[0] == 0) {
            System.out.println("该文件不存在");
            return "";
        }
        ArrayList<Integer> block = new ArrayList<>();
        // 该文件的第一个数据磁盘块快号
        while (id[1] != 0) {
            block.add(id[1]);
            id[1] = disk.getFatNextBlock(id[1]);
        }
        // 获取文件长度
//        int len = Integer.parseInt(disk.binaryToNum(disk.read(id[0], id[2] * 8 + 6, 2)));
        int len = disk.getFileLength(id);
        System.out.println(path + "文件长度为：" + len);
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
            if (!isDir(split[i])) {
                System.out.println("路径错误，存在文件于目录位置");
                return;
            }
        }

        StringBuilder sb = new StringBuilder();
        int root = 2;
        int i;
        for (i = 1; i <= split.length - 1; i++) {
            int[] fileBlock = disk.getFileBlock(sb.append("/").append(split[i]).toString());
            if (fileBlock[0] == 0 && fileBlock[1] == 0 && fileBlock[2] == 0) break;
            root = disk.getFatList(fileBlock[1]);
        }

        if (i == split.length) {
            System.out.println("文件已存在，是否覆盖？");
            System.out.println("默认覆盖");
            deleteFile(path);
            i = 1;
        }
        // 在磁盘块root下将剩余文件目录创建完成
        String fileName, name, type, suffix;
        boolean isFile = false;
        List<String> list = new ArrayList<>();
        for (; i <= split.length - 1; i++) {
            if (isFile) {
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
            root = disk.getFatList(root);
            disk.getList(list, root);
            int size = list.size();
            if (size >= 8) {
                int nullBlock = disk.getNullBlock();
                disk.updateFat(root, nullBlock);
                disk.updateFat(nullBlock, 0);
                root = nullBlock;
                size = 0;
            }
            disk.write(root, size * 8, disk.ascllToBinary(name));
            disk.write(root, size * 8 + 3, disk.ascllToBinary(suffix));
            disk.write(root, size * 8 + 4, disk.ascllToBinary(type));

            // 分配数据盘块
            int num = disk.getNullBlock();
            if (num == 0) {
                System.out.println("空间不足");
                return;
            }
            disk.write(root, size * 8 + 5, disk.ascllToBinary(num));
            root = num;
        }
    }

    public void deleteFile(String path) {
        int[] fileBlock = disk.getFileBlock(path);
//        ArrayList<String> list = new ArrayList<>();
//        disk.getList(list, fileBlock[0]);
        System.out.println("----------------fileblock数组------------------");
        System.out.println(fileBlock[0]);
        System.out.println(fileBlock[1]);
        System.out.println(fileBlock[2]);
        System.out.println(fileBlock[3]);
        System.out.println(fileBlock[4]);
        System.out.println("----------------------------------");
        // 该文件就是父目录中最后一个，无需覆盖
//        if (list.size() - 1 == fileBlock[4]) {
//             抹除父目录子项数据
//            disk.write(fileBlock[3], fileBlock[4] * 8, Disk.NULLBIT);
//        } else {
        disk.reccyleBlock(fileBlock);
        // 把最后一项的内容提过来覆盖掉当前目录项
//            disk.write(fileBlock[0], fileBlock[2] * 8, disk.read(fileBlock[0], (list.size() - 1) * 8, 8));
//            disk.write(fileBlock[0], list.size() * 8 - 8, Disk.NULLBIT);
//        }

        // 更新fat表
        disk.removeBlock(fileBlock[1]);
    }

    public void moveFile(String start, String target) {
        // 1、判断target路径是否存在，如果存在存在提示覆盖
        int[] targetFileBlock = disk.getFileBlock(target);
        if (targetFileBlock[0] != 0 || targetFileBlock[1] != 0 || targetFileBlock[2] != 0) {
            System.out.println("提示文件存在，是否覆盖");
        }
        // 2、获取起始文件信息
        int[] startFileBlock = disk.getFileBlock(start);
        String read = disk.read(startFileBlock[3], startFileBlock[4] * 8, 8);
        // 3、使用起始文件信息，创建新文件信息
        String dir = target.substring(0, target.lastIndexOf("/"));
        createFile(dir);
        targetFileBlock = disk.getFileBlock(dir);
        // 最后一个目录项所在盘块
        int lastBlock = disk.getFatList(targetFileBlock[1]);
        ArrayList<String> list = new ArrayList<>();
        disk.getList(list, lastBlock);
        // 目录项个数
        int size = list.size();
        if(size == 8) {
            // 扩容
            int nullBlock = disk.getNullBlock();
            disk.updateFat(lastBlock, nullBlock);
            disk.updateFat(nullBlock, 0);
            lastBlock = nullBlock;
            size = 0;
        }
        disk.write(lastBlock, size * 8, read);
        // 4、删除原目录文件
        disk.reccyleBlock(startFileBlock);
    }

    public List<String> ls(String path) {
        List<String> list = new ArrayList<>();
        int[] fileBlock = disk.getFileBlock(path);
        disk.getList(list, fileBlock[1]);
        return list;
    }

    public void copy(String src, String dest) {
        // 1、读出源文件内容
        String read = readFile(src);
        // 2、写入新文件
        createFile(dest);
        editFile(dest, read);
    }

}
