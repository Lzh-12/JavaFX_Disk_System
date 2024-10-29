package org.example.disk.constants;

public class DirConstants {
    /**
     * 目录操作命令
     */

    /* 1、创建目录 */
    public static final String MD = "mkdir";
    /* 2、显示目录内容 */
    public static final String DIR = "ls";
    /* 3、删除目录 */
    public static final String RD = "rmdir";
    /* 4、切换目录 */
    public static final String CD = "cd";

    /* 目录的登记项 */
    public static final byte DIRECTORY = 0b00001000; // 二进制
}
