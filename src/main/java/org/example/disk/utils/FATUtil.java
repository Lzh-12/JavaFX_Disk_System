package org.example.disk.utils;

import org.example.disk.constants.DiskConstants;

import static org.example.disk.service.FileManager.DISK;


/**
 * 文件分配表的操作
 */
public class FATUtil {
    // 添加到文件分配表（磁盘号number）
    public static void addFATByte(int number, byte newData){
        if(number < 0 || number >= DiskConstants.DISK_SIZE)
            throw new IllegalArgumentException("index out of range");

    // 添加新数据
    DISK.bt[number / 64][number % 64] = newData;
}

    // 删除文件分配表的项（磁盘号）
    public static void deleteFATByte(int number){
        if(number < 0 || number >= DiskConstants.DISK_SIZE)
            throw new IllegalArgumentException("index out of range");

        // 删除数据
        int next = number;
        while(true){
            if(DISK.bt[next / 64][next % 64] == -1){
                DISK.bt[next / 64][next % 64] = 0;
                break;
            }

            // 下一块磁盘号
            next = DISK.bt[number / 64][number % 64];
            DISK.bt[number / 64][number % 64] = 0;
        }
    }

    // 查找当前文件的最后一块磁盘（起始盘块号）
    public static int findLastBlock(int number){
        while(true){
            if(DISK.bt[number / 64][number % 64] == -1)
                return number;

            // 下一块磁盘号
            number = DISK.bt[number / 64][number % 64];
        }
    }

    // 查找空闲块
    public static int findFreeBlock() {
        for (int i = 3; i < DiskConstants.DISK_SIZE; i++) {
            if(DISK.bt[i / 64][i % 64] == 0)
                return i;
        }
        return -1; // 没有找到
    }
}
