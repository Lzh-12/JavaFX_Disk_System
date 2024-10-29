module org.example.javafxdisksystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.prefs;
    requires org.junit.jupiter.api;


    exports org.example.disk;
    opens org.example.disk to javafx.fxml;

    exports org.example.disk.utils;
    opens org.example.disk.utils to javafx.fxml;

    exports org.example.disk.entity;
    opens org.example.disk.entity to javafx.fxml;

    exports org.example.disk.controller;
    opens org.example.disk.controller to javafx.fxml;

    exports org.example.disk.view;
    opens org.example.disk.view to javafx.fxml;

    exports org.example.disk.service;
    opens org.example.disk.service to javafx.fxml;

    exports org.example.disk.constants;
    opens org.example.disk.constants to javafx.fxml;

}