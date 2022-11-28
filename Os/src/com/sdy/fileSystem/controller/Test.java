package com.sdy.fileSystem.controller;

/**
 * @author 孙东宇
 * 创建时间：2022/11/22
 * 介绍：
 */
public class Test {
    public static void main(String[] args) {
        String str = "t        /aaa/aaa.txt juiwre  owieur    woeir   woieur  weriuq weriuiowqieru wqeroi";
        System.out.println(str.indexOf("t "));
    }

    public static String delManySpace(String str) {
        int len = str.length();
        int pre = 0;
        int next = 0;
        char[] arr = str.toCharArray();
        boolean b = false;
        while(next != len) {
            if(arr[next] != ' ') {
                if(b) b = false;
                arr[pre++] = arr[next++];
                continue;
            }
            if(b) {
                next++;
            } else {
                b = true;
                arr[pre++] = arr[next++];
            }
        }
        String s = new String(arr, 0, pre);
        System.out.println(s);
        return s;
    }
}
