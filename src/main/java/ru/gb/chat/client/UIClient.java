package ru.gb.chat.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/*
* TODO
*  Корректное закрытие сокетов при закрытии окна
*
* */

public class UIClient extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(UIClient.class.getResource("client-ui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 540, 380);
//        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
//        stage.setMaximized(true);
        stage.setTitle("My Cool Chat");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            /**
             * никак не получается организовать нормальное закрытие сокета при закрытии окна.
             */
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}