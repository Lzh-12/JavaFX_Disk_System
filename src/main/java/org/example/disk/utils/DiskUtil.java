package org.example.disk.utils;

import org.example.disk.constants.DiskConstants;

import java.util.Arrays;

import static org.example.disk.service.FileManager.DISK;


public class DiskUtil {
    public static void addByte(int index, byte[] newData){
        if(index < 0 || index > DiskConstants.DISK_SIZE-1)
            throw new IllegalArgumentException("index out of range");

        if(newData.length > 64|| DISK.bt[index].length > 64)
            throw new IllegalArgumentException("data size too long");
        // 得到原本数组的长度
        int currentLength = 0;
        for(int i = 0; i < 8; i++){
            // 默认为0，则为空，如果写入文件或者目录信息则为空格或者大于0的数（7， 15，...）
            if(DISK.bt[index][i * 8] != 0)
                currentLength = i * 8 + 8; // 下一个空闲的位置
        }

        if(currentLength + newData.length > 64)
            throw new IllegalArgumentException("data size too long");

        // 添加新数据
        for (int i = 0; i < newData.length; i++) {
            DISK.bt[index][currentLength + i] = newData[i];
        }
    }

    // 查看磁盘剩余空间
    public static int freeSpace(){
        int freeSpace = 0;
        for(int i = 0; i < DiskConstants.DISK_SIZE; i++){
            if(DISK.bt[i / 64][i % 64] == 0)
                freeSpace += 1;
        }
        return freeSpace * 64;
    }

    /**
     * 格式化磁盘
     */
    public static void formatDisk(){
        for(int i = 0; i < DiskConstants.DISK_SIZE; i++){
            Arrays.fill(DISK.bt[i], (byte) 0);
        }

        // 文件分配表和根目录
        for(int i = 0; i < 3; i++)
            DISK.bt[0][i] = -1;

        // 已损坏磁盘
        DISK.bt[0][23] = -2;
        DISK.bt[0][49] = -2;

        System.out.println("格式化磁盘");
        for(int i = 0; i < 8; i++)
            System.out.println(i + " " + Arrays.toString(DISK.bt[i]));
    }
}
