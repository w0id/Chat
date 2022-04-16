package ru.gb.chat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.gb.chat.Command;

import java.io.IOException;
import java.util.Optional;

public class ClientController {

    private final ChatClient client;
    @FXML
    public TextArea messageArea;
    @FXML
    public TextField messageField;
    @FXML
    public HBox loginBox;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button authButton;
    @FXML
    public VBox messageBox;
    @FXML
    public Button sendButton;

    private final Alert emptyMessageField = new Alert(Alert.AlertType.WARNING,"Введите текст сообщения", ButtonType.OK);

    public void setErrorText(String[] errorText) {
        Platform.runLater(() -> {
            Alert authError = new Alert(Alert.AlertType.ERROR, errorText[0], ButtonType.OK);
            authError.showAndWait();
        });
    }

    public ClientController() {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (IOException e) {
                connectionError();
            }
        }
    }

    @FXML
    protected void sendMessage() {
        String text = messageField.getText();
        if (!(text.trim().isEmpty())) {
            client.sendMessage(text);
            messageField.clear();
            messageField.requestFocus();
        }else{
            emptyMessageField.showAndWait();
        }
    }

    public void initialize() {
        messageArea.setStyle("-fx-control-inner-background: #efd0a5");
    }

    public void exitChat() {
        client.sendMessage(Command.END);
        Platform.exit();
        System.exit(0);
    }

    public void authButtonClick() {
        client.sendMessage(Command.AUTH, loginField.getText(), passwordField.getText());
    }

    public void addMessage(final String message) {
        messageArea.appendText(message + "\n");
    }

    public void toggleBoxesVisibility(final boolean isSuccess) {
        loginBox.setVisible(!isSuccess);
        messageBox.setVisible(isSuccess);
        loginField.clear();
        passwordField.clear();
    }

    private void connectionError() {
        final Alert alert = new Alert(Alert.AlertType.ERROR,"Сервер недоступен.",
                new ButtonType("Повторить попытку", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle("Ошибка подключения");
        final Optional<ButtonType> buttonType = alert.showAndWait();
        final Boolean isExit = buttonType.map(btn -> btn.getButtonData().isCancelButton()).orElse(false);
        if (isExit) {
            System.exit(0);
        }
    }
}