module github.com.youknow2509.battleship {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires okhttp3;
    requires com.google.gson;


    opens github.com.youknow2509.battleship to javafx.fxml;
    exports github.com.youknow2509.battleship;
    exports github.com.youknow2509.battleship.controller;
    opens github.com.youknow2509.battleship.controller to javafx.fxml;
}