package com.sdy.fileSystem;

import com.sdy.fileSystem.controller.DiskController;

import java.util.Scanner;

/**
 * @author 孙东宇
 * 创建时间：2022/11/04
 * 介绍：
 */
public class Test implements Runnable {
    public DiskController disk;

    public Test(DiskController disk) {
        this.disk = disk;
    }

    public Test() {
    }

    public static void mean() {
        System.out.println("1、create");
        System.out.println("2、type");
        System.out.println("3、edit");
        System.out.println("4、delete");
        System.out.println("5、copy");
        System.out.println("6、ls");
        System.out.println("7、move");
        System.out.println("8、exit");
    }

    public void go() {
        System.out.println("--------------文件管理系统-----------------");
        Scanner scanner = new Scanner(System.in);
        disk.getDiskService().getDisk().init();
        while (true) {
            mean();
            int idx = scanner.nextInt();
            String path, src, dest, data;
            switch (idx) {
                case 1: {
                    path = scanner.next();
                    disk.create(path);
                    System.out.println("文件创建成功");
                    break;
                }
                case 2: {
                    path = scanner.next();
                    System.out.println(disk.type(path));
                    break;
                }
                case 3: {
                    path = scanner.next();
                    data = scanner.next();
                    disk.edit(path, data);
                    break;
                }
                case 4: {
                    path = scanner.next();
                    disk.delete(path);
                    break;
                }
                case 5: {
                    src = scanner.next();
                    dest = scanner.next();
                    disk.copy(src, dest);
                    break;
                }
                case 6: {
                    path = scanner.next();
                    disk.ls(path);
                    break;
                }
                case 7: {
                    src = scanner.next();
                    dest = scanner.next();
                    disk.move(src, dest);
                    break;
                }
                case 8: {
                    System.exit(0);
                }
            }
        }
    }

    @Override
    public void run() {
        go();
    }
}
