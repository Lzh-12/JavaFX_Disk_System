package org.example.disk.utils;

import java.util.Arrays;

import static org.example.disk.service.FileManager.DISK;


/**
 * 文件操作的工具类
 */
public class FileUtil {
    // 检查文件名是否重复（文件路径 文件名 文件类型）
    public static int findFileName(String path, String name, String fileType) {
        // 计算多少层目录
        String[] paths = path.split("/"); // ~/tmp   3
        // 根目录
        if(paths.length == 1){
            for(int j = 0; j < 8; j++){
                StringBuilder stringBuilder = new StringBuilder();
                for(int k = 0; k < 5; k++){
                    // 下一级目录
                    if((char) DISK.bt[2][j * 8 + k] != ' ')
                        stringBuilder.append((char) DISK.bt[2][j * 8 + k]);
                }
                if(stringBuilder.toString().equals(name + fileType))
                    return 0; // 文件已存在
            }
            // 文件不存在可以创建
            return 1;
        }

        boolean flag = false;
        boolean flag2 = false;
        // 根目录起始盘块号
        int index = 2;
        for(int i = 0; i < paths.length; i++){
            for(int j = 0; j < 8; j++){
                StringBuilder stringBuilder = new StringBuilder();
                for(int k = 0; k < 3; k++){
                    // 下一级目录
                    if((char) DISK.bt[index][j * 8 + k] != ' ')
                        stringBuilder.append((char) DISK.bt[index][j * 8 + k]);
                }

                // 最后一次查找文件
                if(i == paths.length - 1){
                    for(int k = 3; k < 5; k++){
                        if((char) DISK.bt[index][j * 8 + k] != ' ')
                            stringBuilder.append((char) DISK.bt[index][j * 8 + k]);
                    }
                    if(stringBuilder.toString().equals(name + fileType)){
                        // 文件已存在
                        return 0;
                    }
                } else {
                    // 目录名相同
                    if((int) DISK.bt[index][j * 8 + 5] == 8 && stringBuilder.toString().equals(paths[i+1])) {
                        index = DISK.bt[index][j * 8 + 6]; // 更新起始盘块号
                        flag = true;
                    }
                }
            }
            // 父目录不存在
            if(!flag)
                return 2;
        }
        // 文件不存在可以创建
        return 1;
    }


    // 找到文件的父目录的磁盘号
    public static int findParentDisk(String path){
        // 计算多少层目录
        String[] paths = path.split("/"); // ~/tmp   2
        if(paths.length == 1)
            return 2;

        // 多级目录
        int index = 2; // 根目录起始盘块号
        for(int i = 0; i < paths.length - 1; i++){
            for(int j = 0; j < 8; j++){
                StringBuilder stringBuilder = new StringBuilder();
                // 目录的登记项
                if((int) DISK.bt[index][j * 8 + 5] == 8){
                    for(int k = 0; k < 3; k++){
                        // 下一级目录
                        if((char) DISK.bt[index][j * 8 + k] != ' ')
                            stringBuilder.append((char) DISK.bt[index][j * 8 + k]);
                    }
                    // 目录名相同
                    if(stringBuilder.toString().equals(paths[i+1])) {
                        index = DISK.bt[index][j * 8 + 6]; // 更新起始盘块号
                        // 最下级的目录
                        if(i == paths.length - 2){
                            return index; // 返回var的磁盘号
                        }
                    }
                }
            }
        }
        return -1;
    }

    // 查找在父目录中的位置
    public static int findFileIndex(String path, String name, String fileType) {
        int index = findParentDisk(path); // 父目录的磁盘号

        for(int i = 0; i < 8; i++){
            StringBuilder stringBuilder = new StringBuilder();
            for(int j = 0; j < 5; j++){
                // 下一级目录
                if((char) DISK.bt[index][i * 8 + j] != ' ')
                    stringBuilder.append((char) DISK.bt[index][i * 8 + j]);
            }

            if(stringBuilder.toString().equals(name + fileType))
                return i * 8;
        }
        return -1;
    }


    // 找到文件的起始磁盘号
    public static int findFileDisk(String path, String name, String fileType) {
        // 计算多少层目录
        String[] paths = path.split("/"); // ~/tmp/var   3
        // 根目录
        if(paths.length == 1){
            for(int j = 0; j < 8; j++){
                StringBuilder stringBuilder = new StringBuilder();
                for(int k = 0; k < 5; k++){
                    // 下一级目录
                    if((char) DISK.bt[2][j * 8 + k] != ' ')
                        stringBuilder.append((char) DISK.bt[2][j * 8 + k]);
                }
                if(stringBuilder.toString().equals(name + fileType))
                    return DISK.bt[2][j * 8 + 6]; // 返回起始磁盘号
            }
            return -1;
        }

        // 根目录起始盘块号
        int index = 2;
        for(int i = 0; i < paths.length; i++){
            for(int j = 0; j < 8; j++){
                StringBuilder stringBuilder = new StringBuilder();
                for(int k = 0; k < 3; k++){
                    // 下一级目录
                    if((char) DISK.bt[index][j * 8 + k] != ' ')
                        stringBuilder.append((char) DISK.bt[index][j * 8 + k]);
                }

                // 最后一次查找文件
                if(i == paths.length - 1){
                    for(int k = 3; k < 5; k++){
                        if((char) DISK.bt[index][j * 8 + k] != ' ')
                            stringBuilder.append((char) DISK.bt[index][j * 8 + k]);
                    }
                    if(stringBuilder.toString().equals(name + fileType)){
                        return DISK.bt[index][j * 8 + 6]; // 返回起始磁盘号
                    }
                } else {
                    // 目录名相同
                    if((char) DISK.bt[index][j * 8 + 3] != ' ' && stringBuilder.toString().equals(paths[i+1])) {
                        index = DISK.bt[index][j * 8 + 6]; // 更新起始盘块号
                    }
                }
            }
        }
        return -1;
    }

    // 获取文件长度
    public static int getLength(String path, String name, String fileType) {
        // 查找文件的起始磁盘号
        int number = findFileDisk(path, name, fileType);
        int count = 0;
        while(true){
            for(int i = 0; i < 64; i++){
                if(DISK.bt[number][i] != 0)
                    count++;
            }

            if(DISK.bt[number / 64][number % 64] != -1)
                number = DISK.bt[number / 64][number % 64]; // 查找下一块磁盘
            else
                break;
        }
        return count;
    }

    // 删除文件内容（文件起始磁盘号）
    public static void deleteFile(int number) {
        while(true){
            Arrays.fill(DISK.bt[number], (byte) 0); // 删除文件内容

            if(DISK.bt[number / 64][number % 64] != -1){
                number = DISK.bt[number / 64][number % 64]; // 查找下一块磁盘
            } else {
                break;
            }
        }
    }

    // 修改文件属性（文件名，磁盘号，数组，文件属性）
    public static void changeFile(String name, String fileType, int index, char attribute){
        // 查找文件名
        for(int i = 0; i < 8; i++){
            // 文件名三个字节
            StringBuilder stringBuilder = new StringBuilder();
            // 得到文件名
            for (int j = 0; j < 5; j++) {
                if((char) DISK.bt[index][i * 8 + j] != ' ')
                    stringBuilder.append((char) DISK.bt[index][i * 8 + j]);
            }
            // 文件名相同
            if(stringBuilder.toString().equals(name + fileType)){
                DISK.bt[index][i * 8 + 5] = (byte) attribute;
                return;
            }
        }
    }

}
