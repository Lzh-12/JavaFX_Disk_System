package org.example.disk.constants;

public class FileConstants {
    /**
     * 文件操作命令
     */

    /* 1、创建文件 */
    public static final String CREATE_FILE = "create";
    /* 2、打开文件 */
    public static final String OPEN_FILE = "open";
    /* 3、读文件 */
    public static final String READ_FILE = "read";
    /* 4、写文件 */
    public static final String WRITE_FILE = "write";
    /* 5、关闭文件 */
    public static final String CLOSE_FILE = "close";
    /* 6、删除文件 */
    public static final String DELETE_FILE = "delete";
    /* 7、显示文件内容 */
    public static final String TYPE_FILE = "cat";
    /* 8、改变文件属性 */
    public static final String CHANGE = "chmod";


    /* 只读系统文件的属性 */
    public static final byte ONLY_READ_FILE = 0b00000011;  // 二进制
    /* 可读可写的普通文件 */
    public static final byte CAN_WRITE_FILE = 0b00000100; // 二进制

}
