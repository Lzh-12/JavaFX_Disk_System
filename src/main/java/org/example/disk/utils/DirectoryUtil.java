package org.example.disk.utils;

import java.util.Arrays;

import static org.example.disk.service.FileManager.DISK;

public class DirectoryUtil {
    // 在目录表中删除指定的目录信息或者文件信息（文件名，目录所在磁盘，数据），返回删除后的字节数组
    public static void deleteByte(String name, String fileType, int index){
        // 查找文件名
        for(int i = 0; i < 8; i++){
            StringBuilder stringBuilder = new StringBuilder();
            // 文件
            if(DISK.bt[index][i * 8 + 5] == 3 || DISK.bt[index][i * 8 + 5] == 4){
                for (int j = 0; j < 5; j++) {
                    // 不为空格
                    if((char) DISK.bt[index][j + i * 8] != ' ')
                        stringBuilder.append((char) DISK.bt[index][j + i * 8]);
                }

                // 文件名相同
                if(stringBuilder.toString().equals(name + fileType)){
                    // 删除数据
                    for (int j = 0; j < 8; j++) {
                        // 清空数据，将数据赋值为0
                        DISK.bt[index][i * 8 + j] = (byte) 0;
                    }
                    return;
                }
            } else {
                for(int j = 0; j < 3; j++) {
                    if ((char) DISK.bt[index][j + i * 8] != ' ')
                        stringBuilder.append((char) DISK.bt[index][j + i * 8]);
                }
                // 目录名相同
                if(stringBuilder.toString().equals(name)){
                    // 删除数据
                    for (int j = 0; j < 8; j++) {
                        DISK.bt[index][i * 8 + j] = (byte) 0;
                    }
                    return;
                }
            }
        }
    }

    // 找到文件的父目录的磁盘号
    public static int findParentDisk(String path){
        // 计算多少层目录
        String[] paths = path.split("/"); // ~/tmp/var   3
        if(paths.length == 1)
            return 2;

        // 多级目录
        int index = 2; // 根目录起始盘块号
        for(int i = 0; i < paths.length - 1; i++){
            for(int j = 0; j < 8; j++){
                StringBuilder stringBuilder = new StringBuilder();
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

    // 查找当前目录下是否存在同名目录
    public static boolean findSameDirectory(String name, int index){
        for(int i = 0; i < 8; i++){
            // 目录三个字节
            StringBuilder stringBuilder1 = new StringBuilder();
            if((int) DISK.bt[index][i * 8 + 5] == 8) {
                // 得到目录名（字节转换成字符串）
                for (int j = 0; j < 3; j++) {
                    if((char) DISK.bt[index][i * 8 + j] != ' ')
                        stringBuilder1.append((char) DISK.bt[index][i * 8 + j]);
                }

                // 目录名相同
                if(stringBuilder1.toString().equals(name)){
                    return true;
                }
            }
        }
        return false;
    }

    // 显示目录信息中的子目录名和文件名（磁盘号， 字节数组）
    public static String showDirectoryBt(int number) {
        StringBuilder result = new StringBuilder();
        // 查找目录名和文件名
        for(int i = 0; i < 8; i++){
            // 目录名和文件名三个字节
            StringBuilder stringBuilder = new StringBuilder();
            // 得到文件名
            if(DISK.bt[number][i * 8] == 0)
                break;

            stringBuilder.append("- ");
            for (int j = 0; j < 3; j++){
                if((char) DISK.bt[number][i * 8 + j] != ' ')
                    stringBuilder.append((char) DISK.bt[number][i * 8 + j]);
            }

            // 不为空格表示文件要加上文件类型
            if((char) DISK.bt[number][i * 8 + 3] != ' '){
                stringBuilder.append(".");
                stringBuilder.append((char) DISK.bt[number][i * 8 + 3]);
                stringBuilder.append((char) DISK.bt[number][i * 8 + 4]);
            }
            // 换行输出
            result.append(stringBuilder).append("\n");
        }
        return result.toString();
    }

    // 找到目录的磁盘号（路径 目录名）
    public static int findDirDisk(String path, String name) {
        // 计算多少层目录
        String[] paths = path.split("/"); // ~/tmp/var   3

        // 根目录起始盘块号
        int index = 2;
        for(int i = 0; i < paths.length; i++){
            for(int j = 0; j < 8; j++){
                StringBuilder stringBuilder = new StringBuilder();
                if((int) DISK.bt[index][j * 8 + 5] == 8){
                    for(int k = 0; k < 3; k++){
                        // 下一级目录
                        if((char) DISK.bt[index][j * 8 + k] != ' ')
                            stringBuilder.append((char) DISK.bt[index][j * 8 + k]);
                    }

                    // 上一级目录只有根目录或者到了最后一级目录
                    if(i == paths.length - 1 || paths.length == 1){
                        if(stringBuilder.toString().equals(name)) {
                            return DISK.bt[index][j * 8 + 6]; // 返回起始磁盘号
                        }
                    }

                    // 目录名相同
                    if(stringBuilder.toString().equals(paths[i+1]))
                        index = DISK.bt[index][j * 8 + 6]; // 更新起始盘块号
                }
            }
        }
        return -1;
    }


    // 删除目录中全部内容
    public static void deleteDirByte(int index){
        // 删除数据
        Arrays.fill(DISK.bt[index], (byte) 0);
        System.out.println("删除成功");
    }

}
