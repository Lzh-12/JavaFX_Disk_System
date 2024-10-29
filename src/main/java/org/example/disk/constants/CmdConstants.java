package org.example.disk.constants;

public class CmdConstants {
    /**
     * 其他命令
     */

    /* 查看所有命令 */
    public static final String HELP = "help";
    /* 清屏 */
    public static final String CLEAR = "clear";
    /* 退出 */
    public static final String QUIT = "quit";
    /* 显示文件系统的磁盘空间使用情况 */
    public static final String DF = "df";
    /* 格式化 磁盘 */
    public static final String MKFS = "mkfs";


    /**
     * 命令行输出
     */

    /* 缺少参数 */
    public static final String MISSING_PARAMETER = "错误：缺少参数";
    /* 目录名不合法 */
    public static final String DIRECTORY_NAME_ERROR = "目录名不合法";
    /* 文件名不合法 */
    public static final String FILE_NAME_ERROR = "文件名不合法";
    /* 文件不存在 */
    public static final String FILE_IS_NOT_EXIT = "文件不存在";
    /* 目录不存在 */
    public static final String DIRECTORY_IS_NOT_EXIT = "目录不存在";
}
