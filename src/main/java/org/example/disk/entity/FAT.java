package org.example.disk.entity;


import static org.example.disk.service.FileManager.DISK;

/**
 * 文件分配表
 */
public class FAT {
    // 初始化目录的登记表
    public static void initFAT(){
        // 文件分配表和根目录
        for(int i = 0; i < 3; i++)
            DISK.bt[0][i] = -1;

        // 已损坏磁盘
        DISK.bt[0][23] = -2;
        DISK.bt[0][49] = -2;
    }
}
