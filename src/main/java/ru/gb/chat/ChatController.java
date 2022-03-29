package ru.gb.chat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController {
    @FXML
    public TextArea messageArea;

    @FXML
    public TextField messageField;

    private final Alert alert = new Alert(Alert.AlertType.WARNING,"Введите текст сообщения", ButtonType.OK);

    @FXML
    protected void sendMessage(ActionEvent actionEvent) {
        if (!(messageField.getText().isEmpty())) {
            messageArea.appendText(messageField.getText() + "\n");
            messageField.clear();
            messageField.requestFocus();
        }else{
            alert.showAndWait();
        }
    }

    public void exitChat(ActionEvent actionEvent) {
        System.exit(0);
    }
}