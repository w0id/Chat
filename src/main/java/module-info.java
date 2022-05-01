module ru.gb.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports ru.gb.chat.client;
    opens ru.gb.chat.client to javafx.fxml;
}