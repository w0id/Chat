module ru.gb.chat {
    requires javafx.controls;
    requires javafx.fxml;

    exports ru.gb.chat.client;
    opens ru.gb.chat.client to javafx.fxml;
}