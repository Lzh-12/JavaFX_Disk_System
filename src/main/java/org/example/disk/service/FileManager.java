package org.example.disk.service;

import org.example.disk.constants.CmdConstants;
import org.example.disk.constants.DiskConstants;
import org.example.disk.entity.*;
import org.example.disk.constants.FileConstants;
import org.example.disk.utils.DirectoryUtil;
import org.example.disk.utils.DiskUtil;
import org.example.disk.utils.FATUtil;
import org.example.disk.utils.FileUtil;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.example.disk.entity.FAT.initFAT;

/**
 * 对文件分配表和目录登记表进行管理
 */
public class FileManager {
    public static final Disk DISK = DiskManager.getDiskInstance(); // 目录登记表
    // 缓冲区
    private final char[] buffer1 = new char[DiskConstants.BUFFER_SIZE]; // 写缓冲区
    private final char[] buffer2 = new char[DiskConstants.BUFFER_SIZE]; // 读缓冲区

    // 已打开文件表
    private final OfTle[] ofTle;
    // 已打开文件登记表中登记的文件数量
    private int OfTLeLength = 0;

    // 读写锁
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock() {
        @Override
        public WriteLock writeLock() {
            return super.writeLock();
        }

        @Override
        public ReadLock readLock() {
            return super.readLock();
        }
    };


    public FileManager() {
        // 初始化目录登记表的文件分配表
        initFAT();

        // 测试
        for(int i = 0; i < 6; i++){
            System.out.println(Arrays.toString(DISK.bt[i]));
        }

        this.ofTle = new OfTle[OfTle.OPEN_FILE_TABLE_LENGTH];
    }

    // 得到文件类型（~/file.txt） 得到（txt）
    private String getFileType(String name){
        return name.substring(name.lastIndexOf(".") + 1);
    }


    /**
     * 创建文件
     * 参数：文件名，文件属性
     */
    public int createFile(String path, String name, String FileAttribute) {
        // 获取锁
        readWriteLock.writeLock().lock();
        // 文件类型
        try {
            String fileType = getFileType(name);
            name = name.substring(0, name.lastIndexOf("."));
            char attribute = '4'; // 可读可写的普通文件属性
            if(Objects.equals(FileAttribute, "r"))
                attribute = '3'; // 系统只读文件属性

            // 文件属性如果是只读性质则不能建立
            if ((byte) Character.getNumericValue(attribute) != FileConstants.CAN_WRITE_FILE) {
                System.out.println("只读文件或错误属性的文件不能创建");
                return -1;
            }

            System.out.println("创建文件" + " " +  path + " " + name);

            // 检查文件目录，确认无重名文件后，寻找空闲登记项进行登记
            int isCreate = FileUtil.findFileName(path, name, fileType);

            // 如果存在，查看有无重名
            //文件，如果有，则提示该文件已存在，建立文件失败
            if (isCreate == 2) {
                System.out.println("父目录不存在");
                return 2;
            } else if(isCreate == 0) {
                System.out.println("文件已存在，创建失败");
                return 0; // 文件已存在
            }

            // 找到一个空闲的磁盘块
            int number = FATUtil.findFreeBlock();

            // 更新文件分配表（磁盘号，下一块的值-1）
            FATUtil.addFATByte(number, (byte) -1);

            // 创建文件对象
            FileItem fileItem = new FileItem(name, fileType, attribute, number, path);

            // 测试创建文件
            System.out.println("文件名：" + fileItem.getFileName() + ", 文件类型：" + fileItem.getFileType() + ", 文件属性：" + fileItem.getAttribute() +
                    ", 起始磁盘块：" + fileItem.getNumber() + ", 文件长度：" + fileItem.getLength());

            // 遍历磁盘块，找到所在目录的磁盘号，然后写入目录登记表
            int index = FileUtil.findParentDisk(path); // 父目录的磁盘号

            // 文件名：3 个字节（实验中合法文件名仅可以使用字母、数字和除“$”、“.”、“/”以外的字符，
            //第一个字节的值为“$”时表示该目录为空目录项，文件名和类型名之间用“.”分割，用“/”作为
            //路径名中目录间分隔符）；
            //文件类型名：2 个字节；
            //文件属性：1 个字节；
            //起始盘块号：1 个字节；
            //文件长度：1 个字节
            char[] data = new char[6];

            // 文件名小于三个字节，剩余部分用空格代替
            for (int i = 0; i < 3; i++) {
                if(i < fileItem.getFileName().length())
                    data[i] = fileItem.getFileName().charAt(i);
                else
                    data[i] = ' ';
            }

            data[3] = fileItem.getFileType().charAt(0); // "s"
            if(fileItem.getFileType().length() == 1)
                data[4] = ' '; // 类型名为1位，则空格
            else
                data[4] = fileItem.getFileType().charAt(1);

            // 转为字节存入目录登记表
            byte[] bt = new byte[8];
            for(int i = 0; i < 5; i++){
                bt[i] = (byte) data[i];
            }
            bt[5] = (byte) Character.getNumericValue(fileItem.getAttribute());
            bt[6] = (byte) fileItem.getNumber();
            bt[7] = (byte) fileItem.getLength();

            // 写入文件登记表
            DiskUtil.addByte(index, bt);

            for(int j = 0; j < 6; j++){
                System.out.println(Arrays.toString(DISK.bt[j]));
            }

            return 1;
        } finally {
            // 释放锁
            readWriteLock.writeLock().unlock();
        }
    }


    /**
     * 3、读文件
     * 使用读写锁可以让多个读操作并行执行，
     * 但如果有写操作发生，则会阻塞所有的读操作和写操作，
     * 直到写操作完成。
     */
    public String readFile(String path, String name, int length) {
        // 获取读锁
        readWriteLock.readLock().lock();

        // 实验中，读文件操作的主要工作是查找已打开文件表中是否存在该文件；如果不存在，则打开再
        try {
            String fileType = getFileType(name);
            name = name.substring(0, name.lastIndexOf(".")); // 文件名
            int index = FileUtil.findFileDisk(path, name, fileType); // 文件起始盘块
            // 文件不存在
            if(index == -1)
                return CmdConstants.FILE_IS_NOT_EXIT;

            for (int i = 0; i < OfTle.OPEN_FILE_TABLE_LENGTH; i++) {
                // 文件路径相同
                if (this.ofTle[i] != null && Objects.equals(this.ofTle[i].getName(), path + '/' + name)) {
                    // 从已打开文件表中读出读指针，从这个位置上读出所需要长度，若所需长度没有读完已经遇到文件结束符，就终止操作
                    // 然后检查是否是以读方式打开文件，如果是以写方式打开文件，则不允许读；
                    if(this.ofTle[i].getFlag() == 1)
                        return "写方式打开文件不能读出内容";
                    // 从已打开文件表中读出读指针，从这个位置上读出所需要长度
                    return read(index, i, length);
                }
            }

            if (OfTLeLength == OfTle.OPEN_FILE_TABLE_LENGTH)
                return "文件不能打开";
            // 不在已打开文件表，插入已打开文件表
            int parentIndex = FileUtil.findParentDisk(path); // 父目录的磁盘号
            int j = FileUtil.findFileIndex(path, name ,fileType); // 在父目录中的位置
            int i = insertOfTle(path, name, fileType, parentIndex, j, 0);
            return read(index, i, length);
        } finally {
            // 释放读锁
            readWriteLock.readLock().unlock();
        }
    }

    // 起始磁盘号，所在已打开文件分配表块号，读取长度
    private String read(int index, int i, int length){
        StringBuilder content = new StringBuilder();

        // 文件打开时 dNum 为文件起始盘块号，bNum 为“0”
        this.ofTle[i].getRead().setbNum(index);
        this.ofTle[i].getRead().setdNum(0);
        this.ofTle[i].setFlag(0); // 读操作方式打开文件

        int addr = 0; // 块内地址
        for (int k = 0; k < length; k++){
            // 寻找下一块磁盘块
            if(k % 64 == 0 && k != 0) {
                index = DISK.bt[index / 64][index % 64];
                this.ofTle[i].getRead().setbNum(index);
                this.ofTle[i].getRead().setdNum(0);
                addr = 0; // 块内地址设置为0
            }

            // 若所需长度没有读完已经遇到文件结束符，就终止操作，实验中用“#”表示文件结束
            if((char) DISK.bt[index][addr] != '#'){
                this.ofTle[i].getRead().setdNum(addr);
                content.append((char) DISK.bt[index][addr]);
                addr++;
            } else {
                return content.toString();
            }
        }
        return content.toString();
    }


    // 插入已打开文件表（文件路径 父目录的磁盘号 文件在目录中的起始位置， 打开方式）
    private int insertOfTle(String path, String name, String fileType, int parentIndex, int index, int flag) {
        for (int i = 0; i < OfTle.OPEN_FILE_TABLE_LENGTH; i++) {
            if (this.ofTle[i] == null) {
                // 读文件的位置，文件打开时 dNum 为文件起始盘块号，bNum 为“0”
                Pointer readPointer = new Pointer(DISK.bt[parentIndex][index + 6], 0);
                Pointer writePointer = new Pointer(DISK.bt[parentIndex][index + 6], 0);

                this.ofTle[i] = new OfTle(path + '/' + name,
                        (char) DISK.bt[parentIndex][index+5],
                        DISK.bt[parentIndex][index+6],
                        FileUtil.getLength(path, name, fileType),
                        flag, readPointer, writePointer);
                // 已打开文件数量增加
                OfTLeLength++;
                // 返回插入的位置
                return i;
            }
        }
        return -1;
    }

    /**
     * 4、写文件（文件路径，文件名，写入的内容）
     */
    public int writeFile(String path, String name, String content) {
        // 获取写锁
        readWriteLock.writeLock().lock();
        /* 实验中，写文件操作的主要工作是查找已打开文件表中是否存在该文件，如果不存在，则打开
           后再写；如果存在，还要检查是否以写方式打开文件；如果不是写方式打开文件，不能写；最后从
           已打开文件表中读出写指针，从这个位置上写入缓冲中的数据。
         */
        try {
            String fileType = getFileType(name); // 文件类型
            name = name.substring(0, name.lastIndexOf(".")); // 文件名
            int number = FileUtil.findFileDisk(path, name, fileType); // 起始磁盘号
            System.out.println("起始磁盘号" + " " + number);
            // 文件不存在
            if(number == -1)
                return 2;

            for(int i = 0; i < OfTle.OPEN_FILE_TABLE_LENGTH; i++){
                // 文件路径名 起始磁盘号 相同
                if(this.ofTle[i] != null && Objects.equals(this.ofTle[i].getName(), path + '/' + name)){
                    // 不是写方式打开，不能写入
                    if(this.ofTle[i].getFlag() == 0)
                        return -1;

                    // 找到存放文件的结束磁盘
                    int index = FATUtil.findLastBlock(number);
                    System.out.println("找到存放文件的结束磁盘" + " " + index); // 3

                    // 打开文件时 dNum 和 bNum 为文件的末尾位置
                    this.ofTle[i].getWrite().setdNum(index);
                    // 一块磁盘的大小是64个字节
                    // 文件长度
                    int n = this.ofTle[i].getLength();
                    // 计算起始盘块的块内地址
                    int bNum = n - (n / 64) * 64; // 3 - (3/64) * 64   结果为3
                    this.ofTle[i].getWrite().setbNum(bNum - 1);
                    // 对磁盘文件进行写操作时，要写满缓冲后才写入磁盘
                    writeContentToDisk(content, buffer1, index, i);

                    for(int k = 0; k < 6; k++){
                        System.out.println(Arrays.toString(DISK.bt[k]));
                    }
                    return 1;
                }
            }

            System.out.println("文件不在已打开文件表中");
            // 不在已打开文件表中
            int parentIndex = FileUtil.findParentDisk(path); // 父目录的磁盘号
            int i = FileUtil.findFileIndex(path, name, fileType); // 在父目录中的位置
            if(DISK.bt[parentIndex][i + 5] == FileConstants.ONLY_READ_FILE)
                return 3; // 只读文件不能写入

            // 插入已打开文件表中的位置
            int is_insert = insertOfTle(path, name, fileType, parentIndex, i, 2);
            // 插入失败
            if(is_insert == -1)
                return 0;

            // 插入成功
            int index = FATUtil.findLastBlock(number); // 找到文件的结束盘块
            System.out.println("文件在文件分配表的位置" + number + "文件的结束盘块" + index);
            // 写入缓冲区
            writeContentToDisk(content, buffer1, index, is_insert);

            for(int k = 0; k < 6; k++){
                System.out.println(Arrays.toString(DISK.bt[k]));
            }
            return 1;
        } finally {
            // 释放写锁
            readWriteLock.writeLock().unlock();
        }
    }

    private void writeContentToDisk(String content, char[] buffer1, int index, int tableIndex) {
        int count = content.length()  / DiskConstants.BUFFER_SIZE + 1; // 计算要写满缓冲区的次数

        for (int k = 0; k < count; k++) {
            int startIndex = k * DiskConstants.BUFFER_SIZE;
            int charsToRead = Math.min(DiskConstants.BUFFER_SIZE, content.length() - startIndex);

            try {
                for (int m = 0; m < charsToRead; m++) {
                    buffer1[m] = content.charAt(startIndex + m);
                    System.out.print(buffer1[m]);
                }
            } catch (StringIndexOutOfBoundsException e) {
                System.err.println("Error: Index out of bounds while reading content.");
                return;
            }

            writeBufferToFile(index, buffer1, tableIndex);
            buffer1 = new char[DiskConstants.BUFFER_SIZE]; // 更高效地清空缓冲区
            index = FATUtil.findLastBlock(index);
        }
    }


    // 写入磁盘
    private void writeBufferToFile(int index, char[] buffer1, int i) {
        // 原本的磁盘空间，注意原本的空字节0也会作为字符串的长度
        int origin = 0;
        for(int j = 0; j < 64; j++){
            if(DISK.bt[index][j] != 0)
                origin++;
        }

        int len = origin;
        // 文件建立从下标为0的位置开始写入 0 0
        // 文件已被写入过覆盖之前的文件结束符‘#’的位置012  123   4-1 == ’#‘ 3  123#  2
        if(origin != 0)
            // 写入的文件关闭之后再次写入要覆盖之前的文件结束符‘#’，写入没有关闭就直接追加
            if((char) DISK.bt[index][origin-1] == '#')
                origin -= 1;


        System.out.println("写入文件前的文件分配表的下标" + " " + origin);

        int remain = 64 - len; // 目录登记表计算结束磁盘的剩余空间
        int count = 0;// 真正的长度
        for (char c : buffer1) {
            System.out.print(c);
            if (c != '\u0000')
                count++;
        }

        System.out.println("真正的长度count" + " " + count);

        // 缓冲区写满
        if(count == 64){
            for (int m = 0; m < remain; m++) {
                // 更新块内地址
                this.ofTle[i].getWrite().setdNum(origin + m);
                // 写入目录登记表（转为字节数据）
                DISK.bt[index][origin + m] = (byte) buffer1[m];
            }
            // 更新文件长度
            this.ofTle[i].setLength(this.ofTle[i].getLength() + remain);

            // 需要分配新的磁盘作为文件的结束磁盘
            if (count > remain) {
                // 查找空闲磁盘块
                int freeBlock = FATUtil.findFreeBlock();
                if (freeBlock == -1)
                    throw new RuntimeException("无法找到空闲磁盘块");

                // 更新文件分配表（当前磁盘下一块磁盘号-1改为freeBlock）
                FATUtil.addFATByte(index, (byte) freeBlock);
                index = freeBlock;
                // 新分配的磁盘写入目录登记表（下一块磁盘号255）
                FATUtil.addFATByte(index, (byte) -1);
                // 更新写文件指针，指向文件结束块
                this.ofTle[i].getWrite().setbNum(index);

                // 缓冲区剩余内容写入新的磁盘块
                for (int m = 0; m < 64 - remain; m++) {
                    this.ofTle[i].getWrite().setdNum(m);
                    // 转为字节数据
                    DISK.bt[index][m] = (byte) buffer1[remain + m];
                }
                this.ofTle[i].setLength(this.ofTle[i].getLength() + 64 - remain);
            }
        } else {
            // 缓冲区没有写满而且长度小于剩余长度
            if(count <= remain){
                for(int j = 0; j < count; j++){
                    this.ofTle[i].getWrite().setdNum(origin + j);
                    DISK.bt[index][origin + j] = (byte) buffer1[j];
                }
                this.ofTle[i].setLength(this.ofTle[i].getLength() + count);
            } else {
                // 先将剩余空间写满
                for(int j = 0; j < remain; j++){
                    this.ofTle[i].getWrite().setdNum(origin + j);
                    DISK.bt[index][origin + j] = (byte) buffer1[j];
                }
                this.ofTle[i].setLength(this.ofTle[i].getLength() + remain);
                // 剩余内容写入新的磁盘块
                // 查找空闲磁盘块
                int freeBlock = FATUtil.findFreeBlock();
                if (freeBlock == -1)
                    throw new RuntimeException("无法找到空闲磁盘块");

                // 更新文件分配表
                FATUtil.addFATByte(index, (byte) freeBlock);
                index = freeBlock;
                // 更新文件结束块
                FATUtil.addFATByte(index, (byte) -1);
                // 更新写文件指针，指向文件结束块
                this.ofTle[i].getWrite().setbNum(index);
                // 缓冲区剩余内容写入新的磁盘块
                for (int m = 0; m < count - remain; m++) {
                    this.ofTle[i].getWrite().setdNum(m);
                    DISK.bt[index][m] = (byte) buffer1[remain + m];
                }
                this.ofTle[i].setLength(this.ofTle[i].getLength() + count - remain);
            }
        }
    }


    /**
     * 5、关闭文件
     * 用户对文件读写完毕后需要调用文件系统的“关闭文件”操作
     * 实验中关闭文件，首先要看该文件是否打开，如果没有打开，就不用关闭；如果已经打开，则
     * 检查打开方式，如果是写方式打开的，要追加文件结束符，修改目录项；最后从已打开文件表中删
     * 除对应项
     */
    public int closeFile(String path, String name){
        // 获取写锁
        readWriteLock.writeLock().lock();
        try {
            String fileType = getFileType(name);
            name = name.substring(0, name.lastIndexOf(".")); // 文件名
            // 找到文件
            int i = FileUtil.findFileDisk(path, name, fileType); // 文件磁盘号
            // 文件不存在
            if(i == -1)
                return 0;

            int parentIndex = FileUtil.findParentDisk(path); // 父目录的磁盘号
            int number = FileUtil.findFileIndex(path, name, fileType); // 文件在父目录中的位置
            // 首先要看该文件是否打开，如果没有打开，就不用关闭
            for(int j = 0; j < OfTle.OPEN_FILE_TABLE_LENGTH; j++){
                // 文件绝对路径相同
                if (this.ofTle[j] != null && this.ofTle[j].getName().equals(path + '/' + name)) {
                    // 如果已经打开，则检查打开方式，如果是写方式打开的，要追加文件结束符，修改目录项
                    if (DISK.bt[parentIndex][number + 5] == FileConstants.CAN_WRITE_FILE) {
                        // 查找文件的结束磁盘号
                        int endBlock = FATUtil.findLastBlock(i);
                        // 判断结束磁盘号的目录登记表是否写满
                        int origin = 0;
                        // 从末尾开始查找，找到第一个不是0的字节数据
                        for (int k = 63; k >= 0; k--) {
                            if (DISK.bt[endBlock][k] == 0)
                                origin++;
                        }

                        origin = 64 - origin;
                        // 已经写满
                        if (origin == 64) {
                            // 再分配一块磁盘
                            int freeBlock = FATUtil.findFreeBlock();
                            // 修改磁盘号的值
                            FATUtil.addFATByte(endBlock, (byte) freeBlock);
                            // 新结束磁盘设置为255
                            endBlock = freeBlock;
                            FATUtil.addFATByte(endBlock, (byte) -1);
                            // 目录登记表中追加文件结束符
                            DISK.bt[endBlock][0] = (byte) '#';
                        } else {
                            // 目录登记表有空余空间直接追加文件结束符
                            DISK.bt[endBlock][origin] = (byte) '#';
                        }
                    }
                    // 最后从已打开文件表中删除对应项（文件路径）(只读文件直接关闭)
                    removeOpenFile(path, name);

                    for(int k = 0; k < 8 ; k++)
                        System.out.println(Arrays.toString(DISK.bt[k]));
                    return 1;
                }
            }

            // 文件没有打开
            return -1;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    // 删除已打开文件表的项
    private void removeOpenFile(String path, String name){
        for(int i = 0; i < OfTle.OPEN_FILE_TABLE_LENGTH; i++){
            // 文件绝对路径和起始磁盘号相同
            if (this.ofTle[i] != null && Objects.equals(this.ofTle[i].getName(), path  + '/' + name)){
                this.ofTle[i] = null; // 删除此登记项
                break;
            }
        }
    }

    /**
     * 6、删除文件
     * 用户认为文件没有必要保存时需要调用文件系统的“删除文件”操作。实验中，删除文件时参
     * 数只要文件名，delete_file（文件名）。
     * 实验中，删除文件操作的主要工作是检查文件是否存在；不存在，操作失败；如存在，查找该
     * 文件是否打开，如果打开不能删除；如果没有打开，则删除文件目录项并归还文件所占磁盘空间。
     */
    public int deleteFile(String path, String name) {
        // 使用写锁，确保在删除文件时没有其他线程可以读取或写入文件，从而避免数据不一致的问题
        readWriteLock.writeLock().lock();
        // 文件名
        try {
            String fileType = getFileType(name);
            name = name.substring(0, name.lastIndexOf("."));
            // 检查文件是否存在
            int i = FileUtil.findFileDisk(path, name, fileType);
            if(i == -1)
                return -1; // 文件不存在

            // 在文件的磁盘块中删除文件内容
            FileUtil.deleteFile(i);

            // 文件打开，不能删除
            for(int j = 0; j < OfTle.OPEN_FILE_TABLE_LENGTH; j++){
                // 文件绝对路径和起始磁盘号相同
                if(this.ofTle[j] != null && this.ofTle[j].getName().equals(path + '/' + name))
                    return 0;
            }

            // 如果没有打开，则删除文件目录项并归还文件所占磁盘空间
            int index = FileUtil.findParentDisk(path); // 父目录的磁盘号
            // 在目录登记表中的目录删除（文件名，所在目录的磁盘号，字节数组）
            DirectoryUtil.deleteByte(name, fileType, index);
            // 更新文件分配表
            FATUtil.deleteFATByte(i);

            System.out.println("删除文件后的目录登记表");
            for(int j = 0; j < 8; j++)
                System.out.println(j + " " + Arrays.toString(DISK.bt[j]));

            // 删除成功
            return 1;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 7、显示文件内容（文件路径， 文件名）
     */
    public String typeFile(String path, String name){
        readWriteLock.readLock().lock();
        try {
            String fileType = getFileType(name); // 文件类型
            name = name.substring(0, name.lastIndexOf(".")); // 文件名

            // 文件磁盘号
            int index = FileUtil.findFileDisk(path, name, fileType);
            if(index == -1)
                return "文件不存在";

            // 如果存在，查看文件是否打开，打开则不能显示文件内容
            for(int i = 0; i < OfTle.OPEN_FILE_TABLE_LENGTH; i++){
                if(this.ofTle[i] != null && this.ofTle[i].getName().equals(path + '/' + name)){
                    return "文件已打开，无法显示文件内容";
                }
            }

            // 若没有打开，从目录中取出文件的起始盘块号，块一块显示文件内容
            StringBuilder result = new StringBuilder();
            while (true) {
                if(DISK.bt[index / 64][index % 64] == -1){
                    int count = 0;
                    for(int k = 63; k >= 0; k--){
                        if(DISK.bt[index][k] != 0)
                            count++;
                    }
                    for(int j = 0; j < count; j++)
                        result.append((char) DISK.bt[index][j]);

                    return result.toString();
                } else {
                    // 读取当前目录登记表的全部内容
                    for(int j = 0; j < 64; j++)
                        result.append((char) (DISK.bt[index][j]));

                    // 下一块磁盘块号
                    index = DISK.bt[index / 64][index % 64];
                }
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * 8、改变文件属性
     */
    public int change(String path, String name, char attribute){
        readWriteLock.writeLock().lock();
        try {
            String fileType = getFileType(name);
            name = name.substring(0, name.lastIndexOf("."));
            // 改变文件属性，首先查找该文件，如果不存在，结束
            int i = FileUtil.findFileDisk(path, name, fileType); // 文件起始磁盘号
            if(i == -1)
                return -1;

            // 如果存在，检查文件是否打开，打开不能改变属性
            for(int j = 0; j < OfTle.OPEN_FILE_TABLE_LENGTH; j++){
                // 在已打开文件表中查找
                if(this.ofTle[j] != null && Objects.equals(this.ofTle[j].getName(), path + '/' + name))
                    return 0;
            }

            int parentIndex = FileUtil.findParentDisk(path); // 父目录的磁盘号
            int number = FileUtil.findFileIndex(path, name, fileType); // 文件在父目录中的位置
            // 没有打开，根据要求改变目录项中属性值
            DISK.bt[parentIndex][number + 5] = (byte)Character.getNumericValue(attribute);
            // 在父目录中修改文件的信息
            FileUtil.changeFile(name, fileType, parentIndex, attribute); // 修改文件属性

            for(int j = 0; j < 8; j++)
                System.out.println(Arrays.toString(DISK.bt[j]));

            // 修改成功
            return 1;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 1、建立目录（md）
     */
    public int createDirectory(String path, String name){
        // 建立目录首先要找到建立目录的位置（父目录），然后查找该目录是否存在
        System.out.println("建立目录：" + path + " " + name); // ~    test      ~/tmp  test
        // 获取写锁
        readWriteLock.writeLock().lock();

        try {
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

            System.out.println("空闲磁盘号" + freeBlock); // 3

            // 修改文件分配表
            FATUtil.addFATByte(freeBlock, (byte) -1);

            DirectoryItem newDirectoryItem = new DirectoryItem(name, '8', freeBlock, path);

//            System.out.println("建立目录：" + path + '/' + name);
//            System.out.println("建立目录的目录名" + name); // tmp

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

            System.out.println("删除空目录" + path + ' ' + name); // ~   test

            // 删除空目录首先要找到该目录，如果目录不存在，指令执行失败
            int i = DirectoryUtil.findDirDisk(path, name); // 目录的磁盘号
            if(i == -1)
                return -1;

            System.out.println("目录的磁盘号" + i); // 3
            // 如果存在，判断是否为空目录，为空则删除，否则显示不能删除
            if(DISK.bt[i][0] == (byte) 0){
                // 父目录所在磁盘号（index），子目录磁盘号（i）
                int index = DirectoryUtil.findParentDisk(path);

                System.out.println("父目录的磁盘号" + index); // 2
                System.out.println("index" + index); // 2

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
    }
}