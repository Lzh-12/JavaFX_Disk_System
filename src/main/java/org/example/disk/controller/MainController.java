package org.example.disk.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.example.disk.entity.*;
import org.example.disk.service.DirectoryManager;
import org.example.disk.service.FileManager;
import org.example.disk.constants.CmdConstants;
import org.example.disk.constants.DirConstants;
import org.example.disk.constants.FileConstants;
import org.example.disk.utils.DirectoryUtil;
import org.example.disk.utils.DiskUtil;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.disk.service.FileManager.DISK;


public class MainController implements Initializable {
    @FXML
    public SplitPane splitPane;
    @FXML
    public FlowPane flowPane; // 文件列表

    private String currentPath; // 当前路径

    private FileManager fileManager;
    private DirectoryManager directoryManager;


    @FXML
    private TextField commandInput = new TextField(); // 输入命令

    @FXML
    private TextArea commandOutput = new TextArea(); // 输出结果
    private String commandPath; // 当前命令行的路径

    private Stage stage;

    private final ExecutorService executor = Executors.newFixedThreadPool(4); // 根据需要调整线程池大小

    public void init(Stage stage) {
        // 文件管理器
        fileManager = new FileManager();
        // 目录管理器
        directoryManager = new DirectoryManager();
        // 根目录
        this.currentPath = "~";
        // 命令行
        initCommandLine();
        this.stage = stage;
        // 创建图标
        addIcon("~");
        // 设置窗口关闭事件处理器
        stage.setOnCloseRequest(event -> DiskManager.saveDiskInstance());
    }

    // 初始化命令行
    private void initCommandLine() {
        this.commandOutput.setEditable(false);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        this.commandOutput.appendText("欢迎使用模拟磁盘文件系统!\n当前时间 " + LocalDateTime.now().format(formatter) + "\n\n" + "root@localhost" + this.currentPath + "> ");
        // 初始化为根目录
        this.commandPath = this.currentPath;
        // 输入命令
        this.commandInput.setPromptText("输入命令...");
        // 获取键盘输入
        this.commandInput.setOnKeyReleased(keyEvent -> updateOutput());
        // 回车处理命令
        this.commandInput.setOnAction(actionEvent -> executeCommand(this.commandInput.getText()));
    }

    // 实时更新输出区域
    private void updateOutput() {
        String inputText = this.commandInput.getText();
        this.commandOutput.setText(this.commandOutput.getText().substring(0, this.commandOutput.getText().lastIndexOf("\n") + 1) + "root@localhost" + this.commandPath+ "> " + inputText);
        this.commandOutput.setScrollTop(Double.MAX_VALUE);
    }

    // 执行命令
    private void executeCommand(String command) {
        // 清空输入框
        this.commandInput.clear();
        // 换行
        this.commandOutput.appendText("\n");
        // 根据空格分割
        String[] args = command.split(" ");
        // 分配任务
        CommandTask task = new CommandTask(args);
        executor.submit(task);
    }

    class CommandTask implements Runnable {
        private final String[] args;

        public CommandTask(String[] args) {
            this.args = args;
        }

        @Override
        public void run() {
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    // 1、创建文件（文件名，文件属性，当前目录）
                    case FileConstants.CREATE_FILE:
                        if(args.length == 3){
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 文件名
                            if(isLegalFileName(name) && isLegalAttribute(args[2])){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.FILE_NAME_ERROR + "\n"));
                            } else {
                                int is_create = fileManager.createFile(path, name, args[2]);
                                Platform.runLater(() -> {
                                    if(is_create == -1){
                                        commandOutput.appendText("只读文件或错误文件属性, 创建失败" + "\n"); // 输出读取的内容
                                    } else if(is_create == 0) {
                                        commandOutput.appendText("文件已存在，创建失败" + "\n"); // 输出读取的内容
                                    } else if(is_create == 2) {
                                        commandOutput.appendText("父目录不存在，创建失败" + "\n"); // 输出读取的内容
                                    } else {
                                        commandOutput.appendText("文件创建成功" + "\n"); // 输出读取的内容
                                        flowPane.getChildren().clear();
                                        addIcon(path);
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 2、读文件（文件名，读取长度，操作类型）
                    case FileConstants.READ_FILE:
                        if(args.length == 3){
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 文件名
                            if(isLegalFileName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.FILE_NAME_ERROR + "\n"));
                            } else {
                                String content = fileManager.readFile(path, name, Integer.parseInt(args[2]));
                                Platform.runLater(() -> commandOutput.appendText(content + "\n")); // 输出读取的内容
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 3、写文件（文件名，缓冲，写长度） write 1.txt content
                    case FileConstants.WRITE_FILE:
                        if(args.length >= 3){
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 文件名
                            // 从第三个部分开始合并
                            StringBuilder merged = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                merged.append(args[i]);
                                if (i < args.length - 1) {
                                    // 如果不是最后一个部分，添加空格
                                    merged.append(" ");
                                }
                            }
                            args[2] = merged.toString();
                            if(isLegalFileName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.FILE_NAME_ERROR + "\n"));
                            } else {
                                int is_write = fileManager.writeFile(path, name, args[2]);
                                Platform.runLater(() -> {
                                    if(is_write == -1){
                                        commandOutput.appendText("文件读方式打开，写入失败\n");
                                    } else if(is_write == 0) {
                                        commandOutput.appendText("文件无法打开，文件写入失败\n");
                                    } else if(is_write == 2){
                                        commandOutput.appendText(CmdConstants.FILE_IS_NOT_EXIT + "文件写入失败\n");
                                    } else if(is_write == 3){
                                        commandOutput.appendText( "只读文件无法写入\n");
                                    }
                                    else {
                                        commandOutput.appendText("文件写入成功\n");
                                    }
                                });
                            }
                        } else {
                            commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n");
                        }
                        break;
                    // 4、关闭文件（文件名）
                    case FileConstants.CLOSE_FILE:
                        if (args.length == 2) {
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 文件名
                            if(isLegalFileName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.FILE_NAME_ERROR + "\n"));
                            } else {
                                int is_close = fileManager.closeFile(path, name);
                                Platform.runLater(() -> {
                                    if(is_close == -1){
                                        commandOutput.appendText("文件未打开\n");
                                    } else if(is_close == 0){
                                        commandOutput.appendText(CmdConstants.FILE_IS_NOT_EXIT + "\n");
                                    } else {
                                        commandOutput.appendText("文件关闭成功\n");
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 5、删除文件（文件名）
                    case FileConstants.DELETE_FILE:
                        if (args.length == 2) {
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 文件名
                            if(isLegalFileName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.FILE_NAME_ERROR + "\n"));
                            } else {
                                int is_delete = fileManager.deleteFile(path, name);
                                Platform.runLater(() -> {
                                    if(is_delete == -1){
                                        commandOutput.appendText(CmdConstants.FILE_IS_NOT_EXIT + "删除失败\n");
                                    } else if(is_delete == 0){
                                        commandOutput.appendText("文件已打开，删除失败\n");
                                    } else {
                                        commandOutput.appendText("文件删除成功\n");
                                        flowPane.getChildren().clear();
                                        addIcon(path);
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 6、显示文件内容
                    case FileConstants.TYPE_FILE:
                        if (args.length == 2) {
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 文件名
                            if(isLegalFileName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.FILE_NAME_ERROR + "\n"));
                            } else {
                                String is_type = fileManager.typeFile(path, name);
                                Platform.runLater(() -> commandOutput.appendText(is_type + "\n"));
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 7、改变文件属性（文件名）
                    case FileConstants.CHANGE:
                        if (args.length == 3) {
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 文件名
                            if(isLegalFileName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.FILE_NAME_ERROR + "\n"));
                            } else {
                                int is_change = fileManager.change(path, name, args[2].charAt(0));
                                Platform.runLater(() -> {
                                    if(is_change == -1){
                                        commandOutput.appendText(CmdConstants.FILE_IS_NOT_EXIT + "无法改变文件属性\n");
                                    } else if(is_change == 0) {
                                        commandOutput.appendText("文件已打开无法改变文件属性\n");
                                    } else {
                                        commandOutput.appendText("文件属性改变成功\n");
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 1、建立目录
                    case DirConstants.MD:
                        if (args.length == 2) { // ~/test
                            String path = getRealDirPath(args[1]); // 父目录  ~
                            String name = getRealFilePath(args[1]); // 目录名 ~/test   test
                            if(isLegalDirName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.DIRECTORY_NAME_ERROR + "\n"));
                            } else {
                                int is_md = directoryManager.createDirectory(path, name);
                                Platform.runLater(() -> {
                                    if(is_md == -1){
                                        commandOutput.appendText("父目录不存在，不能建立\n");
                                    } else if(is_md == 0){
                                        commandOutput.appendText("目录已存在，不能建立\n");
                                    } else {
                                        commandOutput.appendText("目录建立成功\n");
                                        flowPane.getChildren().clear();
                                        addIcon(path);// ~/tmp  path就是~
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 2、显示目录内容（目录名）（不加目录名就显示当前命令行的目录下的文件和目录）
                    case DirConstants.DIR:
                        if (args.length == 2) {
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 目录名
                            if(isLegalDirName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.DIRECTORY_NAME_ERROR + "\n"));
                            } else {
                                String is_dir = directoryManager.showDirectory(path, name);
                                Platform.runLater(() -> commandOutput.appendText(is_dir + "\n"));
                            }
                        } else if(args.length == 1){
                            int split = commandPath.lastIndexOf("/");
                            // 根目录
                            Platform.runLater(() -> {
                                if(split == -1)
                                    commandOutput.appendText(directoryManager.showDirectory(commandPath, commandPath) + "\n");
                                else {
                                    String is_dir = directoryManager.showDirectory(commandPath.substring(0, split), commandPath.substring(split+1));
                                    commandOutput.appendText(is_dir + "\n");
                                }
                            });
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 3、删除空目录
                    case DirConstants.RD:
                        if (args.length == 2) {
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 目录名
                            System.out.println("输入参数" + path + " " + name); // ~   test
                            if(isLegalDirName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.DIRECTORY_NAME_ERROR + "\n"));
                            } else {
                                int is_rd = directoryManager.deleteDirectory(path, name);
                                Platform.runLater(() -> {
                                    if(is_rd == -1){
                                        commandOutput.appendText(CmdConstants.DIRECTORY_IS_NOT_EXIT + "删除失败\n");
                                    } else if(is_rd == 0){
                                        commandOutput.appendText("根目录或者非空目录，删除失败\n");
                                    } else {
                                        commandOutput.appendText("目录删除成功\n");
                                        flowPane.getChildren().clear();
                                        addIcon(path);
                                        System.out.println("删除节点" + is_rd); // 3
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 切换目录
                    case DirConstants.CD:
                        if (args.length == 2) {
                            String path = getRealDirPath(args[1]); // 父目录
                            String name = getRealFilePath(args[1]); // 目录名
                            System.out.println("输入参数" + path + " " + name); // ~   test
                            if(isLegalDirName(name)){
                                Platform.runLater(() -> commandOutput.appendText(CmdConstants.DIRECTORY_NAME_ERROR + "\n"));
                            } else {
                                int is_cd = directoryManager.changeDirectory(path, name);
                                Platform.runLater(() -> {
                                    if(is_cd == -1){
                                        commandOutput.appendText(CmdConstants.DIRECTORY_IS_NOT_EXIT + "切换目录失败\n");
                                    } else {
                                        if(name.equals("~"))
                                            commandPath = path;
                                        else
                                            commandPath = path + '/' + name;
                                        flowPane.getChildren().clear();
                                        addIcon(commandPath);
                                    }
                                });
                            }
                        } else {
                            Platform.runLater(() -> commandOutput.appendText(CmdConstants.MISSING_PARAMETER + "\n"));
                        }
                        break;
                    // 查看磁盘剩余空间
                    case CmdConstants.DF:
                        Platform.runLater(() -> commandOutput.appendText("磁盘剩余空间：" + DiskUtil.freeSpace() + "bytes\n"));
                        break;
                    case CmdConstants.MKFS:
                        FileManager.formatDisk();
                        flowPane.getChildren().clear();
                        Platform.runLater(() -> commandOutput.appendText("磁盘格式化成功\n"));
                        break;
                    // 查看命令
                    case CmdConstants.HELP:
                        Platform.runLater(() -> commandOutput.appendText("""
                                用户可输入操作指令：
                                文件操作命令：
                                create name attribute      创建文件
                                read  name length           读文件
                                write  name content         写文件
                                close  name       关闭文件
                                delete  name      删除文件
                                cat name      显示文件内容
                                chmod  name attribute     改变文件属性

                                目录操作命令：
                                mkdir   name       建立目录
                                ls      [name]     显示目录内容
                                rmdir   name       删除空目录

                                其他操作命令：
                                help   查看所有命令
                                df     查看磁盘状态
                                mkfs   格式化磁盘
                                clear  清屏
                                quit   退出程序
                                """
                        ));
                        break;
                    // 清屏
                    case CmdConstants.CLEAR:
                        Platform.runLater(() -> {
                            commandOutput.clear();
                            commandOutput.appendText(commandPath + "> ");
                        });
                        break;
                    // 退出程序
                    case CmdConstants.QUIT:
                        Platform.runLater(() -> {
                            DiskManager.saveDiskInstance();
                            commandOutput.appendText("退出程序\n");
                            stage.close();
                            System.exit(0);
                        });
                    break;
                    default:
                        Platform.runLater(() -> commandOutput.appendText("未知命令：" + args[0] + "\n"));
                        break;
                }
            }
        }
    }

    // 判断文件名是否合法（实验中合法文件名仅可以使用字母、数字和除“$”、“.”、“/”以外的字符）
    private boolean isLegalFileName(String name) {
        int spilt = name.lastIndexOf('.');
        // 没有'.'
        if(spilt == -1)
            return true;

        // 文件类型不能大于两个字节, 不能为空, 不能为非法字符
        String type = name.substring(spilt+1);
        if(type.length() > 2 || type.isEmpty() || !type.matches("^[a-zA-Z0-9[^$./]]+$"))
            return true;

        name = name.substring(0, spilt); // 文件名
        // 文件名大于三个字节
        if(name.length() > 3)
            return true;

        return !name.matches("^[a-zA-Z0-9[^$./]]+$");
    }

    // 判断是否合法目录名（实验中合法目录名仅可以使用字母、数字和除“$”、“.”、“/”以外的字符，第一个字节的值为“$”时表示该目录为空目录项）
    private boolean isLegalDirName(String name) {
        if(name.length() > 3)
            return true;
        return !name.matches("^[a-zA-Z0-9[^$./]]+$");
    }

    private boolean isLegalAttribute(String path) {
        return Objects.equals(path, "r") || Objects.equals(path, "rw") || Objects.equals(path, "wr");
    }

    /**
     * 处理绝对路径和相对路径
     * 对文件名进行解析获取父目录（~/a.txt  ~/test/a.txt）  得到（~     ~/test）
     */
    private String getRealDirPath(String path) {
        // 绝对路径
        if(path.charAt(0) == '~'){
            // 根目录
            if(path.equals("~"))
                return path;

            // 记录反斜杠最后出现的位置
            int split = path.lastIndexOf('/');
            return path.substring(0, split); // 父目录
        }

        // 相对路径(~/tmp> md t    ~/tmp> md t/e   ~/tmp> md ~/t)  得到 ~/tmp   ~/tmp/t   ~
        int spilt = path.lastIndexOf('/');
        if(spilt == -1)
            return this.commandPath; // ~/tmp
        return this.commandPath + '/' + path.substring(0, spilt); // ~/tmp/t
    }

    private String getRealFilePath(String name) {
        // 绝对路径
        if(name.charAt(0) == '~'){
            // 根目录
            if(name.equals("~"))
                return name;

            // 记录反斜杠最后出现的位置
            int split = name.lastIndexOf('/');
            name = name.substring(split+1); // 文件名
            return name;
        }

        // 相对路径(~/tmp> md t    ~/tmp> md t/e   )  得到 t   e
        int spilt = name.lastIndexOf('/');
        if(spilt == -1) // t
            return name;
        return name.substring(spilt + 1); // e
    }

    // 添加图标到窗口
    public void addIcon(String path) {
        // 找到父目录的磁盘号
        int index = 2; // 根目录
        if(!Objects.equals(path, "~")){
            String name = path.substring(path.lastIndexOf('/') + 1);
            path = path.substring(0, path.lastIndexOf('/'));
            index = DirectoryUtil.findDirDisk(path, name);
        }

        // 图标数组
        Label[] labels = new Label[8];
        for (int i = 0; i < 8; i++) {
            StringBuilder name = new StringBuilder();
            // 文件
            if((int) DISK.bt[index][i * 8 + 5] == 3 || (int) DISK.bt[index][i * 8 + 5] == 4){
                ImageView image = new ImageView(Objects.requireNonNull(getClass().getResource("/images/文件.png")).toExternalForm());
                image.setFitHeight(25);
                image.setFitWidth(25);
                for(int j = 0; j < 5; j++){
                    if((char) DISK.bt[index][i * 8 + j] != ' '){
                        name.append((char)DISK.bt[index][i * 8 + j]);
                    }
                    if(j == 2){
                        name.append(".");
                    }
                }
                labels[i] = new Label(name.toString(), image);
            } else if((int) DISK.bt[index][i * 8 + 5] == 8){
                ImageView image = new ImageView(Objects.requireNonNull(getClass().getResource("/images/文件夹.png")).toExternalForm());
                image.setFitWidth(25);
                image.setFitHeight(25);
                for(int j = 0; j < 3; j++){
                    if((char) DISK.bt[index][i * 8 + j] != ' '){
                        name.append((char)DISK.bt[index][i * 8 + j]);
                    }
                }
                labels[i] = new Label(name.toString(), image);
            }

            if(labels[i] != null) {
                // 设置图标标签的显示方式和文本换行
                labels[i].setContentDisplay(ContentDisplay.TOP);
                labels[i].setWrapText(true);
                this.flowPane.getChildren().add(labels[i]);
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
