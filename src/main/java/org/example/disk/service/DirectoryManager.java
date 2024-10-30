package org.example.disk.service;

import org.example.disk.constants.CmdConstants;
import org.example.disk.entity.DirectoryItem;
import org.example.disk.utils.DirectoryUtil;
import org.example.disk.utils.DiskUtil;
import org.example.disk.utils.FATUtil;

import java.util.Arrays;

import static org.example.disk.service.FileManager.DISK;
import static org.example.disk.service.FileManager.readWriteLock;

/**
 * 目录管理
 */
public class DirectoryManager {
    /**
     * 1、建立目录（md）
     */
    public int createDirectory(String path, String name){
        // 获取写锁
        readWriteLock.writeLock().lock();
        try {
            // 建立目录首先要找到建立目录的位置（父目录），然后查找该目录是否存在
            int index = DirectoryUtil.findParentDisk(path); // 父目录的磁盘号
            if(index == -1)
                return -1; // 父目录不存在

            // 如果存在，查找是否存在同名目录，存在，不能建立
            boolean is_same = DirectoryUtil.findSameDirectory(name, index);
            // 存在不能建立
            if(is_same)
                return 0;

            // 存在，则查找一个空目录项，为该目录申请一个盘块，并填写目录内容
            int freeBlock = FATUtil.findFreeBlock();
            // 修改文件分配表
            FATUtil.addFATByte(freeBlock, (byte) -1);
            DirectoryItem newDirectoryItem = new DirectoryItem(name, '8', freeBlock, path);

            // 填写目录登记表
            byte[] bt = new byte[8];
            for(int j = 0; j < 3; j++) {
                if(j < newDirectoryItem.getDirectoryName().length())
                    bt[j] = (byte) newDirectoryItem.getDirectoryName().charAt(j); // 目录名：3 个字节（实验中合法目录名仅可以使用字母、数字和除“$”、“.”、“/”以外的字符，第一个字节的值为“$”时表示该目录为空目录项）
                else
                    bt[j] = (byte) ' ';
            }

            bt[3] = (byte) ' '; // 保留 2 字节未使用（在实验中填写空格）
            bt[4] = (byte) ' ';
            bt[5] = (byte) Character.getNumericValue(newDirectoryItem.getAttribute()); // 目录属性：1 个字节
            bt[6] = (byte) (newDirectoryItem.getNumber()); // 起始盘块号：1 个字节
            bt[7] = (byte) '0'; // 保留 1 字节未使用（在实验中填写“0”）

            // 文件信息写入对应目录登记表中的目录
            DiskUtil.addByte(index, bt);

            for(int j = 0; j < 6; j++){
                System.out.println(Arrays.toString(DISK.bt[j]));
            }
            // 创建成功返回磁盘号
            return freeBlock;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 2、显示目录内容（~  ~/tmp）
     */
    public String showDirectory(String path, String name){
        readWriteLock.readLock().lock();
        try {
            // 显示目录内容首先要找到该目录，如果目录不存在，指令执行失败
            if(path.equals("~") && name.equals("~"))
                return DirectoryUtil.showDirectoryBt(2);

            int index = DirectoryUtil.findDirDisk(path, name);
            if(index == -1)
                return CmdConstants.DIRECTORY_IS_NOT_EXIT;

            // 如果存在，一项一项显示目录内容（文件名和目录名）
            return DirectoryUtil.showDirectoryBt(index);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * 3、删除空目录(rd)
     */
    public int deleteDirectory(String path, String name){
        readWriteLock.writeLock().lock();
        // 如果存在，但是根目录或非空目录，显示不能删除
        try {
            if((path.equals("~") && name.equals("~")))
                return 0;

            // 删除空目录首先要找到该目录，如果目录不存在，指令执行失败
            int i = DirectoryUtil.findDirDisk(path, name); // 目录的磁盘号
            if(i == -1)
                return -1;

            // 如果存在，判断是否为空目录，为空则删除，否则显示不能删除
            if(DISK.bt[i][0] == (byte) 0){
                // 父目录所在磁盘号（index），子目录磁盘号（i）
                int index = DirectoryUtil.findParentDisk(path);
                // 在目录所在的父目录的目录登记项中删除该目录登记项（对于目录文件类型没有用到）
                DirectoryUtil.deleteByte(name, "8", index);
                // 在目录登记表中的文件分配表中删除（磁盘号， 字节数组）
                FATUtil.deleteFATByte(i);
                // 清空文件目录项的内容（文件所在的目录登记项）
                DirectoryUtil.deleteDirByte(i);

                for(int j = 0; j < 8; j++)
                    System.out.println(Arrays.toString(DISK.bt[j]));

                // 删除成功返回磁盘号
                return i;
            } else {
                // 非空目录，显示不能删除，操作失败
                return 0;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 切换当前目录（cd）
     */
    public int changeDirectory(String path, String name){
        readWriteLock.readLock().lock();
        try {
            if(name.equals("~"))
                return 1;

            // 查看路径是否存在
            return DirectoryUtil.findDirDisk(path, name);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
