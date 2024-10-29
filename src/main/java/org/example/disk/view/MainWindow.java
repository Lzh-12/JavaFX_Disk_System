package org.example.disk.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.example.disk.controller.MainController;

import java.io.IOException;
import java.util.Objects;

public class MainWindow extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Pane root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(root);

        // 得到控制类
        MainController mainController = fxmlLoader.getController();

        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/磁盘.png")).toExternalForm()));
        stage.setTitle("磁盘文件管理系统");
        stage.show();

        // 初始化界面
        mainController.init(stage);
    }
}
