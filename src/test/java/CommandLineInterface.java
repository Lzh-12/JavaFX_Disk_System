import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CommandLineInterface extends Application {

    private TextArea outputArea;
    private TextField inputField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 创建输出区域
        outputArea = new TextArea();
        outputArea.setEditable(false); // 让输出区域不可编辑

        // 创建输入区域
        inputField = new TextField();
        inputField.setOnKeyReleased(e -> updateOutputArea()); // 实时更新输出区域

        // 在回车时处理命令
        inputField.setOnAction(e -> processCommand(inputField.getText()));

        // 创建布局
        VBox layout = new VBox();
        layout.getChildren().addAll(outputArea, inputField);

        // 创建场景
        Scene scene = new Scene(layout, 600, 400);

        // 设置舞台
        primaryStage.setTitle("命令行界面");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 默认提示信息
        outputArea.appendText("欢迎使用命令行界面！\n");
        outputArea.appendText("请输入命令：\n> ");
    }

    // 实时更新输出区域
    private void updateOutputArea() {
        String inputText = inputField.getText();
        // 更新文本框，只显示当前输入的内容
        outputArea.setText(outputArea.getText().substring(0, outputArea.getText().lastIndexOf("\n") + 1) + "> " + inputText);
    }

    // 处理输入的命令
    private void processCommand(String command) {
        // 清空输入框
        inputField.clear();

        // 在输出区域显示用户输入的命令
//        outputArea.appendText("\n> " + command + "\n");

        // 根据输入的命令执行相应的操作
        switch (command.toLowerCase()) {
            case "help":
                outputArea.appendText("\n可用命令：help, hello, clear\n> ");
                break;
            case "hello":
                outputArea.appendText("你好！\n> ");
                break;
            case "clear":
                outputArea.clear(); // 清空输出区域
                outputArea.appendText("命令行已清空。\n> ");
                break;
            default:
                outputArea.appendText("未知命令： " + command + "\n> ");
                break;
        }

        // 确保在处理命令后再次添加提示符
//        outputArea.appendText("> ");
    }
}
